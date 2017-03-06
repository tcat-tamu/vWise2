package edu.tamu.tcat.vwise.client;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import edu.tamu.tcat.vwise.VwiseApplicationContext;
import edu.tamu.tcat.vwise.WorkspaceRepository;

public class RestClientAppContext implements VwiseApplicationContext
{
   private final static Logger logger = Logger.getLogger(RestClientAppContext.class.getName());

   private final Client client;
   private final URI apiEndpoint;

   public RestClientAppContext(URI apiEndpoint)
   {
      this.apiEndpoint = apiEndpoint;
      this.client = ClientBuilder.newClient();
   }

   @Override
   public WorkspaceRepository getRepository()
   {
      // TODO need better monitoring logic -- need to identify which specific resource
      //      monitor this is (so that we can close them). Probably need a master plus
      //      per-instance inner-classes.

      WebTarget target = client.target(apiEndpoint);
      MonitorImpl monitor = new MonitorImpl(UUID.randomUUID());

      return new RestClientWsRepoImpl(target, monitor);
   }

   public void shutdown()
   {
      // TODO monitor status and throw on calls made after the context has been shutdown.
      logger.log(Level.INFO, "Shutting down vWise REST Client Application Context for " + this.apiEndpoint);

      try
      {
         client.close();
      }
      catch (Exception ex)
      {
         String msg = "Failed to shutdown vWise REST Client Application Context for {0}. Reason: {1}";
         logger.log(Level.SEVERE, format(msg, this.apiEndpoint, ex.getMessage()), ex);
      }
   }

   private class MonitorImpl implements Monitor
   {

      private final UUID randomUUID;

      public MonitorImpl(UUID randomUUID)
      {
         this.randomUUID = randomUUID;
      }

      @Override
      public void finish()
      {
         // TODO Auto-generated method stub

      }

   }
}
