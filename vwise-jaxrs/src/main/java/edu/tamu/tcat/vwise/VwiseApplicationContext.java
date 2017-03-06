package edu.tamu.tcat.vwise;

/**
 *  Provides a central point of access for obtaining repository instances various vWise
 *  application resources.
 *
 *  <p>The base application is responsible for properly initializing an application context
 *  and providing access that context as appropriate (e.g. CDI dependency injection, OSGi
 *  declarative services or as a singleton).
 */
public interface VwiseApplicationContext
{
   /**
    * Factory method to obtain a workspace repository.
    */
   public WorkspaceRepository getRepository();
}
