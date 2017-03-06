package edu.tamu.tcat.vwise;

import java.util.Collection;
import java.util.Optional;

import edu.tamu.tcat.vwise.model.WorkspaceMeta;

/**
 *  Maintains a collection of workspaces.
 */
public interface WorkspaceRepository extends AutoCloseable
{

   // IMPL NOTES
   //   * As we add support for users this repo will be scoped to an authenticated user account
   //   * Add filters/query to list all
   //   * May need to provide asynchronous API for updates at some point.
   //

   /**
    * List all workspaces associated with this repository.
    *
    * @return All workspaces associated with this repository. May be empty.
    */
   public Collection<WorkspaceMeta> listAll();

   /**
    * Get information about a workspace.
    *
    * @param id The id of the workspace to retrieve.
    * @return The requested workspace. Will be empty if the requested workspace
    *       does not exist.
    */
   public Optional<WorkspaceMeta> get(String id);

   /**
    * A specific version of a workspace.
    *
    * @param id The id of the workspace to retrieve.
    * @param version The version of the workspace to retrieve.
    * @return The requested version of the workspace. Will be empty if the requested
    *       workspace does not exist.
    */
   public Optional<WorkspaceMeta> get(String id, String version);

   /**
    * Creates a new workspace based on the supplied data. The id and version values,
    * if supplied, will be ignored.
    *
    * @param data initial values for the workspace to be created.
    * @return The created workspace.
    */
   public WorkspaceMeta create(WorkspaceMeta data);

   /**
    * Updates the workspace. Will use the supplied id as the unique value of the
    * workspace to be created. Any (and only) data values that have changed from the
    * referenced version of the workspace will be applied to the most recent version
    * of the workspace.
    *
    * @param data The updated workspace data.
    * @return The updated workspace. Note that the returned workspace may incorporate
    *       intermediate changes from updates that happened subsequent to the version
    *       of the workspace being modified.
    */
   public WorkspaceMeta update(WorkspaceMeta data);

   /**
    * Deletes the selected workspace. Historical information about the workspace
    * will be retained and can be retrieved by requesting the appropriate version,
    * but the workspace will not be listed through {@link #listAll()}.
    *
    * @param id The id of the workspace to be deleted.
    */
   public void remove(String id);

   /**
    * Permanently and irrevocably removes the workspace and all associated data.
    *
    * @param id The id of the workspace to purge.
    */
   public void purge(String id);
}
