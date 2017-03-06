package edu.tamu.tcat.vwise.impl.memory;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.tamu.tcat.vwise.WorkspaceRepository;
import edu.tamu.tcat.vwise.internal.IdFactory;
import edu.tamu.tcat.vwise.model.WorkspaceMeta;

public class WorkspaceRepoImpl implements WorkspaceRepository
{
   private final Properties config;
   private final IdFactory wsIds;
   private final ConcurrentHashMap<String, WorkspaceMediator> workspaces = new ConcurrentHashMap<>();

   public WorkspaceRepoImpl(Properties config)
   {
      this.config = config;
      this.wsIds = new IdFactory(config, "workspaces");
   }

   @Override
   public Collection<WorkspaceMeta> listAll()
   {
      return workspaces.values().stream()
         .map(WorkspaceMediator::get)
         .filter(Optional::isPresent)
         .map(Optional::get)
         .sorted((a, b) -> wsIds.compare(a.id, b.id))
         .collect(toList());
   }

   @Override
   public Optional<WorkspaceMeta> get(String id)
   {
      WorkspaceMediator mediator = workspaces.get(id);
      return mediator != null ? mediator.get() : Optional.empty();
   }

   @Override
   public Optional<WorkspaceMeta> get(String id, String version)
   {
      WorkspaceMediator mediator = workspaces.get(id);
      return mediator != null ? mediator.get(version) : Optional.empty();
   }

   @Override
   public WorkspaceMeta create(WorkspaceMeta data)
   {
      WorkspaceMediator mediator = new WorkspaceMediator(wsIds.get(), data, config);
      workspaces.put(mediator.wsId, mediator);

      return mediator.get()
            .orElseThrow(() -> new IllegalStateException("Failed to create new workspace."));
   }

   @Override
   public WorkspaceMeta update(WorkspaceMeta data)
   {
      String notFoundErr = "Cannot update the workspace, {0} [{1}]. No workspace with this id exists.";

      if (data.id == null || data.id.trim().isEmpty())
         throw new IllegalArgumentException("Cannot update workspace. No id supplied.");

      WorkspaceMediator mediator = workspaces.get(data.id);
      if (mediator == null)
         throw new IllegalArgumentException(format(notFoundErr, data.name, data.id));

      return mediator.update(data);
   }

   @Override
   public void remove(String id)
   {
      WorkspaceMediator mediator = workspaces.get(id);
      if (mediator != null)
         mediator.remove();
   }

   @Override
   public void purge(String id)
   {
      workspaces.remove(id);
   }

   @Override
   public void close() throws Exception
   {
      workspaces.clear();
   }

   /**
    *  Governs access to versioned history of a single workspace.
    */
   private static class WorkspaceMediator
   {
      private final Lock lock = new ReentrantLock();

      public final String wsId;
      private boolean removed = false;
      private final IdFactory versionIds;
      private TreeMap<String, WorkspaceMeta> wsVersions;

      public WorkspaceMediator(String wsId, WorkspaceMeta data, Properties props)
      {
         this.wsId = wsId;
         this.versionIds = new IdFactory(props, "workspace_version");

         WorkspaceMeta meta = WorkspaceMeta.copy(data);
         meta.id = wsId;
         meta.version = versionIds.get();
         wsVersions = new TreeMap<>(this.versionIds);

         this.wsVersions.put(meta.version, meta);
      }

      public void remove()
      {
         lock.lock();
         this.removed = true;
         lock.unlock();
      }

      public Optional<WorkspaceMeta> get()
      {
         lock.lock();
         try {
            if (this.removed)
               return Optional.empty();

            return getSafe(wsVersions.lastEntry());
         } finally {
            lock.unlock();
         }
      }

      public Optional<WorkspaceMeta> get(String version)
      {
         lock.lock();
         try {
            // NOTE: for removed entries, we can still retrieve a workspace given a specific version id
            return getSafe(wsVersions.floorEntry(version));
         } finally {
            lock.unlock();
         }
      }

      /**
       *
       * @param data
       * @return
       * @throws IllegalArgumentException If the supplied metadata is not valid. The most likely
       *       cause is that the supplied version id is not valid.
       * @throws IllegalStateException If the current state of the mediator prevents it from
       *       being updated. This is most like due to it having been deleted.
       */
      public WorkspaceMeta update(WorkspaceMeta data)
      {
         String wsDeletedErr = "Cannot update the workspace, {0} [{1}]. It has been deleted.";                      // 404
         String noVersionErr = "Cannot update the workspace, {0} [{1}]. "
               + "The version of the workspace to modify was not referenced or is invalid.";
         String notExistsErr = "Cannot update the workspace, {0} [{1}]. Failed to retrieve current state.";

         if (data.version == null || data.version.trim().isEmpty())
            throw new IllegalArgumentException(format(noVersionErr, data.name, this.wsId));

         lock.lock();
         try {
            if (this.removed)
               throw new IllegalStateException(format(wsDeletedErr, data.name, this.wsId));

            // get the referenced version
            WorkspaceMeta ref = this.get(data.version)
                  .orElseThrow(() -> new IllegalArgumentException(format(noVersionErr, data.name, this.wsId)));
            WorkspaceMeta current = this.get()
                  .orElseThrow(() -> new IllegalStateException(format(notExistsErr, data.name, this.wsId)));

            WorkspaceMeta updated = update(data, ref, current);
            wsVersions.put(updated.version, updated);

            return WorkspaceMeta.copy(updated);
         } finally {
            lock.unlock();
         }
      }

      // copies the internal object to return a detached version
      private Optional<WorkspaceMeta> getSafe(Entry<String, WorkspaceMeta> entry)
      {
         if (entry == null)
            return Optional.empty();

         WorkspaceMeta meta = entry.getValue();
         return Optional.of(WorkspaceMeta.copy(meta));
      }

      private WorkspaceMeta update(WorkspaceMeta data, WorkspaceMeta ref, WorkspaceMeta current)
      {
         WorkspaceMeta updated = WorkspaceMeta.copy(current);
         updated.version = versionIds.get();

         if (!Objects.equals(ref.scope, data.scope))
            updated.scope = data.scope;

         if (!Objects.equals(ref.key, data.key))
            updated.key = data.key;

         if (!Objects.equals(ref.name, data.name))
            updated.name = data.name;

         if (!Objects.equals(ref.description, data.description))
         updated.description = data.description;
         return updated;
      }
   }

}
