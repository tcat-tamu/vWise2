package edu.tamu.tcat.vwise.client;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.vwise.WorkspaceRepository;
import edu.tamu.tcat.vwise.model.WorkspaceMeta;

public class RestClientWsRepoImpl implements WorkspaceRepository
{

   private final WebTarget target;
   private final Monitor monitor;

   public RestClientWsRepoImpl(WebTarget target, Monitor monitor)
   {
      this.target = target;
      this.monitor = monitor;
   }

   @Override
   public Collection<WorkspaceMeta> listAll()
   {
      Response resp = target.path("workspaces")
            .request(MediaType.APPLICATION_JSON)
            .get();

      switch (resp.getStatus())
      {
         case 200:
            List<WorkspaceMeta> workspaces = resp.readEntity(new GenericType<List<WorkspaceMeta>>() {});
            return workspaces;
         default:
            throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
      }
   }

   @Override
   public Optional<WorkspaceMeta> get(String wsId)
   {
      if (wsId == null || wsId.trim().isEmpty())
         throw new IllegalArgumentException("The workspace id must be supplied.");

      Response resp = target.path("workspaces").path(wsId)
            .request(MediaType.APPLICATION_JSON)
            .get();

      switch (resp.getStatus())
      {
         case 200:
            return Optional.of(resp.readEntity(WorkspaceMeta.class));
         case 404:
            return Optional.empty();
         default:
            throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
      }
   }

   @Override
   public Optional<WorkspaceMeta> get(String wsId, String version)
   {
      if (wsId == null || wsId.trim().isEmpty())
         throw new IllegalArgumentException("The workspace id must be supplied.");
      if (version == null || version.trim().isEmpty())
         throw new IllegalArgumentException("The workspace version must be supplied.");

      Response resp = target.path("workspaces").path(wsId)
            .queryParam("v", version)
            .request(MediaType.APPLICATION_JSON)
            .get();

      switch (resp.getStatus())
      {
         case 200:
            return Optional.of(resp.readEntity(WorkspaceMeta.class));
         case 404:
            return Optional.empty();
         default:
            throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
      }
   }

   @Override
   public WorkspaceMeta create(WorkspaceMeta data)
   {
      Response resp = target.path("workspaces")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(data, MediaType.APPLICATION_JSON));

      switch (resp.getStatus())
      {
         case 200:
            return resp.readEntity(WorkspaceMeta.class);
         default:
            throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
      }
   }

   @Override
   public WorkspaceMeta update(WorkspaceMeta data)
   {
      if (data.id == null || data.id.trim().isEmpty())
         throw new IllegalArgumentException("The id of the workspace to be updated must be supplied.");
      if (data.version == null || data.version.trim().isEmpty())
         throw new IllegalArgumentException("The version of the workspace to be updated must be supplied.");

      Response resp = target.path("workspaces").path(data.id)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(data, MediaType.APPLICATION_JSON));

      switch (resp.getStatus())
      {
         case 200:
            return resp.readEntity(WorkspaceMeta.class);
         default:
            throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
      }
   }

   @Override
   public void remove(String id)
   {
      Response resp = target.path("workspaces").path(id)
            .request(MediaType.APPLICATION_JSON)
            .delete();

      if (resp.getStatus() != 204)
         throw new IllegalStateException();  // TODO  throw the correct exception or otherwise handle response
   }

   @Override
   public void purge(String id)
   {
      throw new UnsupportedOperationException("Workspaces cannot currently be purged via the REST API.");
   }

   @Override
   public void close() throws Exception
   {
      monitor.finish();
   }

}
