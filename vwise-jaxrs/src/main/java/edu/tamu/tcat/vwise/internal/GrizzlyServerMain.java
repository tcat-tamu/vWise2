package edu.tamu.tcat.vwise.internal;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Responsible for initializing and starting the Grizzly server for testing and
 * demonstration purposes.
 */
public class GrizzlyServerMain
{
   private static final String[] RESOURCE_PKGS = {
      "edu.tamu.tcat.vwise.jaxrs"
   };

   private static final Logger logger = Logger.getLogger(GrizzlyServerMain.class.getName());

   private static final String CFG_HOST = "grizzly.server.host";
   private static final String CFG_APPROOT = "grizzly.server.approot";

   private static final String HOST_DEFAULT = "http://localhost:8080";
   private static final String APPROOT_DEFAULT = "/ex";

   private static GrizzlyServerMain mgr;

   public static synchronized GrizzlyServerMain getInstance()
   {
      if (mgr == null)
      {
         Properties cfg = getConfig();
         mgr = new GrizzlyServerMain(cfg);
         mgr.start();

         String startup_msg = "Jersey app started with WADL available at {0}/application.wadl"
               + "\nHit enter to stop it...";
         logger.info(format(startup_msg, mgr.getBaseUri()));
      }

      return mgr;
   }

   private static Properties getConfig()
   {
      String cfgLocation = System.getProperty("config.file");
      Path cfgPath = Paths.get(cfgLocation);

      Properties cfg = new Properties();
      try (InputStream is = Files.newInputStream(cfgPath, StandardOpenOption.READ))
      {
         cfg.load(is);
      }
      catch (Exception ex)
      {
         throw new IllegalStateException(format("Failed to load config file {0}", cfgPath), ex);
      }

      return cfg;
   }

   private final Properties cfg;

   private final URI baseUri;
   private final String host;
   private final String appRoot;

   private HttpServer server;

   public GrizzlyServerMain(Properties cfg)
   {
      this.cfg = cfg;

      host = cfg.getProperty(CFG_HOST, HOST_DEFAULT);
      appRoot = cfg.getProperty(CFG_APPROOT, APPROOT_DEFAULT);

      try
      {
         // create and start a new instance of grizzly http server exposing the Jersey application at BASE_URI
         baseUri = new URI(host).resolve(appRoot);
      }
      catch (URISyntaxException e)
      {
         String msg = "Configuration error: The supplied host name [{0}] is not a valid URI.";
         throw new IllegalStateException(format(msg, host));
      }
   }

   public URI getBaseUri()
   {
      return baseUri;
   }

   public void start()
   {
      try
      {
         ResourceConfig rc = new ResourceConfig().packages(RESOURCE_PKGS);
         server = GrizzlyHttpServerFactory.createHttpServer(getBaseUri(), rc);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to start server", e);
      }
   }

   public void shutdown()
   {
      try
      {
         server.shutdown(10, TimeUnit.SECONDS).get();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, format("Failed to cleanly shutdown Grizzly HTTP server: {0}", e));
      }
   }

   /**
    * Main method.
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      try
      {
         GrizzlyServerMain mgr = getInstance();
         System.in.read();

         mgr.shutdown();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Fatal error launching Grizzly Server", ex);
      }
   }
}

