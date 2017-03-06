package edu.tamu.tcat.vwise.client;

/**
 *  Used to collect stats about the performance of the rest API.
 */
public interface Monitor
{

   /**
    * Notify the monitor holder that the repository has been closed.
    */
   void finish();

}
