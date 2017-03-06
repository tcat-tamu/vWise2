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
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import edu.tamu.tcat.vwise.VwiseApplicationContext;
import edu.tamu.tcat.vwise.impl.memory.InMemoryApplicationContext;

/**
 * Responsible for initializing and starting the Grizzly server for testing and
 * demonstration purposes.
 */
public class GrizzlyServerMain
{
   private static final String CONFIG_FILE_PROP = "config.file";

   private final static Logger logger = Logger.getLogger(GrizzlyServerMain.class.getName());

   private static final String[] RESOURCE_PKGS = {
      "edu.tamu.tcat.vwise.jaxrs"
   };

   private static final String CFG_HOST = "grizzly.server.host";
   private static final String CFG_APPROOT = "grizzly.server.approot";

   private static final String HOST_DEFAULT = "http://localhost:8080";
   private static final String APPROOT_DEFAULT = "/ex";

   private static final Lock mgrLock = new ReentrantLock();
   private static GrizzlyServerMain mgr;

   public static synchronized GrizzlyServerMain getInstance()
   {
      try (AutoCloseable lck = initLock())
      {
         if (mgr == null)
            initServer();

         return mgr;
      } catch (Exception ex) {
         String msg = "Lock management failed.";
         logger.log(Level.SEVERE, msg, ex);
         throw new IllegalStateException(msg, ex);
      }
   }

   public static void shutdown()
   {
      try (AutoCloseable lck = initLock())
      {
         if (mgr != null) {
            mgr.shutdownServer();
            mgr = null;
         }

      } catch (Exception ex) {
         String msg = "Failed to shutdown server.";
         logger.log(Level.SEVERE, msg, ex);
         throw new IllegalStateException(msg, ex);
      }
   }

   /**
    * Attempts to obtain a lock on actions to the server instance. Uses a timeout to prevent
    * deadlock and throws if the lock cannot be obtained.
    */
   private static AutoCloseable initLock()
   {
      try
      {
         String lockErr = "Internal error. Failed to obtain lock for server instance in a timely fashion.";
         if (!mgrLock.tryLock(10, TimeUnit.SECONDS))
            throw new IllegalStateException(lockErr);

         return () -> mgrLock.unlock();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Internal error. Failed to obtain lock for server instance.", ex);
      }
   }

   private static void initServer()
   {
      Properties cfg = getConfig();
      mgr = new GrizzlyServerMain(cfg);
      mgr.start();

      String startup_msg = "Jersey app started with WADL available at {0}/application.wadl";
      logger.info(format(startup_msg, mgr.getBaseUri()));
   }

   private static Properties getConfig()
   {
      String noConfigFileSupplied = "No configuration file supplied. The path to the configuration "
            + "properies file should be specified using the '{0}' system property.";
      String badConfigFile = "The supplied config file [{0}] could not be found.";

      String cfgLocation = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROP),
            () -> format(noConfigFileSupplied, CONFIG_FILE_PROP));
      Path cfgPath = Paths.get(cfgLocation);
      if (!Files.isRegularFile(cfgPath) || !Files.isReadable(cfgPath))
         throw new IllegalStateException(format(badConfigFile, cfgPath.toAbsolutePath().toString()));

      Properties cfg = new Properties();
      try (InputStream is = Files.newInputStream(cfgPath, StandardOpenOption.READ))
      {
         logger.info("Loading configuration properties from " + cfgPath.toAbsolutePath());

         cfg.load(is);

         logger.finer("Loadeding configuration properties\n" + cfg);
         return cfg;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException(format("Failed to load config file {0}", cfgPath), ex);
      }
   }

   private final Properties cfg;

   private final URI baseUri;
   private final String host;
   private final String appRoot;

   private HttpServer server;

   private final InMemoryApplicationContext ctx;

   public GrizzlyServerMain(Properties cfg)
   {
      this.cfg = cfg;

      host = cfg.getProperty(CFG_HOST, HOST_DEFAULT);
      appRoot = cfg.getProperty(CFG_APPROOT, APPROOT_DEFAULT);

      try
      {
         // create and start a new instance of grizzly http server exposing the Jersey application at BASE_URI
         this.baseUri = new URI(host).resolve(appRoot);
         this.ctx = new InMemoryApplicationContext(cfg);

      }
      catch (URISyntaxException e)
      {
         String msg = "Configuration error: The supplied host name [{0}] is not a valid URI.";
         throw new IllegalStateException(format(msg, host));
      }
   }

   public Properties getConfiguration()
   {
      return cfg;
   }

   public VwiseApplicationContext getVwiseContext()
   {
      return ctx;
   }

   public URI getBaseUri()
   {
      return baseUri;
   }

   public void start()
   {
      try
      {
         ResourceConfig rc = new ResourceConfig()
               .packages(RESOURCE_PKGS)
               .register(new ThrowableExceptionMapper());
         server = GrizzlyHttpServerFactory.createHttpServer(getBaseUri(), rc);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to start server", e);
      }
   }

   private void shutdownServer()
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
         getInstance();
         System.out.println("Hit enter to stop it...");
         System.in.read();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Fatal error launching Grizzly Server", ex);
      }
      finally {
         GrizzlyServerMain.shutdown();
      }
   }
}

