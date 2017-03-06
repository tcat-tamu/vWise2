package edu.tamu.tcat.vwise.domain;

import org.junit.After;
import org.junit.Before;

import edu.tamu.tcat.vwise.WorkspaceTest;
import edu.tamu.tcat.vwise.impl.memory.InMemoryApplicationContext;

public class InMemoryWorkspaceTest extends WorkspaceTest
{
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
