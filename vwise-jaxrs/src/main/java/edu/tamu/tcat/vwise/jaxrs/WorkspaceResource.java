package edu.tamu.tcat.vwise.jaxrs;

import static edu.tamu.tcat.vwise.internal.ApiUtils.raise;
import static java.text.MessageFormat.format;

import java.util.Optional;
import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.vwise.UnauthorizedActionException;
import edu.tamu.tcat.vwise.WorkspaceRepository;
import edu.tamu.tcat.vwise.model.WorkspaceMeta;

public class WorkspaceResource
{

   private final WorkspaceRepository repo;
   private final String wsId;

   public WorkspaceResource(WorkspaceRepository repo, String wsId)
   {
      this.repo = repo;
      this.wsId = wsId;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public WorkspaceMeta getWorkspace(@QueryParam("v") @DefaultValue("") String version)
   {
      Optional<WorkspaceMeta> result;
      try
      {
         result = version == null || version.trim().isEmpty()
               ? repo.get(wsId)
               : repo.get(wsId, version);
      }
      catch (UnauthorizedActionException uae)
      {
         throw forbidden("access", uae);
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, format("Unexpected error attempting to access workspace [id: {0}]", wsId), Level.SEVERE, ex);
      }

      String notFoundMsg = "No workspace available for [id: {0}]";
      return result.orElseThrow(
            () -> raise(Status.NOT_FOUND, format(notFoundMsg, wsId), Level.FINE, null));
   }

   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   public WorkspaceMeta update(WorkspaceMeta data)
   {
      try
      {
         return repo.update(data);
      }
      catch (UnauthorizedActionException uae)
      {
         throw forbidden("access", uae);
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, format("Unexpected error attempting to access workspace [id: {0}]", wsId), Level.SEVERE, ex);
      }
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   public Response remove(@QueryParam("purge") @DefaultValue("false") boolean purge)
   {
      try
      {
         if (purge)
            repo.purge(wsId);
         else
            repo.remove(wsId);
         return Response.noContent().build();
      }
      catch (UnauthorizedActionException uae)
      {
         throw forbidden("delete", uae);
      }
      catch (Exception ex)
      {
         throw raise(Status.INTERNAL_SERVER_ERROR, format("Unexpected error attempting to access workspace [id: {0}]", wsId), Level.SEVERE, ex);
      }
   }

   private WebApplicationException forbidden(String action, UnauthorizedActionException uae)
   {
      String msg = format("You do not have permission to {0} this workspace [id: {1}]", action, wsId);
      return raise(Status.FORBIDDEN, msg, Level.WARNING, uae);
   }
}
