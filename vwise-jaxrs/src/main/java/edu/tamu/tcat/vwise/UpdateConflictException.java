package edu.tamu.tcat.vwise;

/**
 * An attempt to update a resource failed to due to conflicting state. For example, a
 * bad version identifier, scope and key in use, etc.
 */
public class UpdateConflictException extends RuntimeException
{

   public UpdateConflictException(String msg)
   {
      super(msg);
   }
}
