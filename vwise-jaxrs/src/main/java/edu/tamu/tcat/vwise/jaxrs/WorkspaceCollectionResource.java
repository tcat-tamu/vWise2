package edu.tamu.tcat.vwise.jaxrs;

import static edu.tamu.tcat.vwise.internal.ApiUtils.raise;
import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.vwise.UnauthorizedActionException;
import edu.tamu.tcat.vwise.VwiseApplicationContext;
import edu.tamu.tcat.vwise.WorkspaceRepository;
import edu.tamu.tcat.vwise.impl.memory.InMemoryApplicationContext;
import edu.tamu.tcat.vwise.internal.ApiUtils;
import edu.tamu.tcat.vwise.model.WorkspaceMeta;

@Path("workspaces")
public class WorkspaceCollectionResource
{
   public final VwiseApplicationContext ctx;

   public WorkspaceCollectionResource()
   {
      // in production, we would use some form of dependency injection to create this
      this.ctx = new InMemoryApplicationContext();
   }

   /**
    * Lists all defined workspaces.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<WorkspaceMeta> listWorkspaces()
   {
      try
      {
         WorkspaceRepository repository = ctx.getRepository();
         return repository.listAll();
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage(), Level.SEVERE, ex);
      }
   }

   /**
    * Creates a new workspace with the supplied data.
    *
    * @param data The workspace data to create.
    * @return The created workspace. The returned workspace will have its id and version
    *       number initialized
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   public WorkspaceMeta createWorkspaces(WorkspaceMeta data)
   {
      try
      {
         // null invalid data values for clarity
         data.id = null;
         data.version = null;

         WorkspaceRepository repository = ctx.getRepository();
         // TODO need to handle possible conflicts (e.g., scope, key already in use)
         return repository.create(data);
      }
      catch (UnauthorizedActionException uae)
      {
         ObjectMapper mapper = ApiUtils.getObjectMapper();
         String dataJson = "Failed to serialize workspace data.";
         try {
            dataJson = mapper.writeValueAsString(data);
         } catch (Exception e) { /* no-op */}

         String msg = format("You do not have permission to create this workspace:\n{0}", dataJson);
         throw ApiUtils.raise(Status.FORBIDDEN, msg, Level.WARNING, uae);
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage(), Level.SEVERE, ex);
      }
   }

   /**
    * Obtain a sub-resource for a workspace
    *
    * @param wsId The id of the workspace to retrieve.
    */
   @Path("{wsId}")
   public WorkspaceResource getWorkspace(@PathParam("wsId") String wsId)
   {
      try
      {
         WorkspaceRepository repository = ctx.getRepository();
         return new WorkspaceResource(repository, wsId);
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage(), Level.SEVERE, ex);
      }
   }
}
