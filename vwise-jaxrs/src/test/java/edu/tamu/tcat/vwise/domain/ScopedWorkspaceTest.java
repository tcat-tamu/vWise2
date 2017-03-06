package edu.tamu.tcat.vwise.domain;

import org.junit.After;
import org.junit.Before;

import edu.tamu.tcat.vwise.impl.memory.InMemoryApplicationContext;

/**
 *
 * Currently a placeholder to remind that we need to pay special attention
 * to the logic of scoping.
 *
 */
public class ScopedWorkspaceTest
{
   @SuppressWarnings("unused")
   private InMemoryApplicationContext ctx;

   @Before
   public void setUp() throws Exception
   {
      ctx = new InMemoryApplicationContext();
   }

   @After
   public void tearDown() throws Exception
   {
      ctx = null;
   }


}
