package edu.tamu.tcat.vwise.impl.memory;

import java.util.Properties;
import java.util.logging.Logger;

import edu.tamu.tcat.vwise.VwiseApplicationContext;
import edu.tamu.tcat.vwise.WorkspaceRepository;

public class InMemoryApplicationContext implements VwiseApplicationContext
{
   final static Logger logger = Logger.getLogger(InMemoryApplicationContext.class.getName());

   private final Properties config;
   private final WorkspaceRepoImpl repo;

   public InMemoryApplicationContext(Properties config)
   {
      this.config = config;
      repo = new WorkspaceRepoImpl(config);
   }

   @Override
   public WorkspaceRepository getRepository()
   {
      return repo;
   }
}
