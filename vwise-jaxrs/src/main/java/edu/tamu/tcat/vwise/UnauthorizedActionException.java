package edu.tamu.tcat.vwise;

/**
 * Thrown if a user is not authorized to complete the requested action.
 */
public class UnauthorizedActionException extends RuntimeException
{

   public UnauthorizedActionException(String msg)
   {
      super(msg);
   }
}
