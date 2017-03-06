package edu.tamu.tcat.vwise.jaxrs;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

import edu.tamu.tcat.vwise.WorkspaceTest;
import edu.tamu.tcat.vwise.client.RestClientAppContext;
import edu.tamu.tcat.vwise.internal.GrizzlyServerMain;

public class RestApiWorkspaceTest extends WorkspaceTest
{
   private final static Logger logger = Logger.getLogger(RestApiWorkspaceTest.class.getName());

   private GrizzlyServerMain server;

   @Before
   public void setUp() throws Exception
   {
      // start the server
      try
      {
         server = GrizzlyServerMain.getInstance();

         URI baseUri = server.getBaseUri();
         logger.log(Level.INFO, "Creating client for: " + baseUri.toString());
         ctx = new RestClientAppContext(baseUri);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to initialize unit test", ex);
         throw ex;
      }
   }

   @After
   public void tearDown() throws Exception
   {
      try
      {
         GrizzlyServerMain.shutdown();
         ((RestClientAppContext)ctx).shutdown();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to clean up after unit test", ex);
         throw ex;
      }
   }
}
