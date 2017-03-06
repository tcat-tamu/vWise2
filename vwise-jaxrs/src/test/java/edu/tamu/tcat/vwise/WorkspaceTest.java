package edu.tamu.tcat.vwise;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;

import edu.tamu.tcat.vwise.model.WorkspaceMeta;
import jersey.repackaged.com.google.common.base.Objects;

/**
 *  Performs core functionality testing of workspaces. Intended to be sub-classed
 *  by specific implementations that initialize the {@link VwiseApplicationContext}
 *  during the setup/teardown phase of the tests. This allows consistent testing
 *  across different implementations.
 *
 *  <p>Implementation-specific testing should extend this base set of test conditions
 *  to support implementation-specific regression tests and additional capabilities.
 */
public abstract class WorkspaceTest
{
   protected VwiseApplicationContext ctx;

   @Test
   public void testCreateWorkspace() throws Exception
   {
      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = "Test Workspace";
      ws.description = "This is a test workspace.";

      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);

         assertNotNull("The workspace id should be set", created.id);
         assertNotNull("The workspace version should be set", created.version);
         assertEquals("The name match the supplied name", ws.name, created.name);
         assertEquals("The description match the supplied description", ws.description, created.description);
      }
   }

   @Test
   public void testGetWorkspace() throws Exception
   {
      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = "Test Workspace";
      ws.description = "This is a test workspace.";

      // create the workspace
      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);
         String wsId = created.id;

         // retrieve the created workspace
         Optional<WorkspaceMeta> optional = repo.get(wsId);

         assertTrue("A workspace should be returned", optional.isPresent());
         WorkspaceMeta retrieved = optional.get();

         assertEquals("The id should be match the requested id", wsId, retrieved.id);
         assertEquals("The version should match the initial version", created.version, retrieved.version);
         assertEquals("The retrieved name match the original name", ws.name, retrieved.name);
         assertEquals("The retrieved description match the original description", ws.description, retrieved.description);
      }
   }

   @Test
   public void testUpdateWorkspace() throws Exception
   {
      String origName = "Test Workspace";
      String origDescription = "This is a test workspace.";

      String updatedName = "Updated Workspace";
      String updatedDescription = "This is an updated workspace description.";

      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = origName;
      ws.description = origDescription;

      // create the workspace
      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);
         created.name = updatedName;
         created.description = updatedDescription;

         // update the workspace and verify results
         WorkspaceMeta updated = repo.update(created);
         assertEquals("The updated id should be match the original id", created.id, updated.id);
         assertFalse("The updated version should not match the original version",
               Objects.equal(created.version, updated.version));
         assertEquals("The updated name should reflect the new value", updatedName, updated.name);
         assertEquals("The updated description should reflect the new value", updatedDescription, updated.description);

         // retrieve the updated workspace to make sure the correct version is returned
         // retrieve the created workspace
         Optional<WorkspaceMeta> optional = repo.get(ws.id);

         assertTrue("A workspace should be returned", optional.isPresent());
         updated = optional.get();

         assertEquals("The updated id should be match the original id", created.id, updated.id);
         assertFalse("The updated version should not match the original version",
               Objects.equal(created.version, updated.version));
         assertEquals("The updated name should reflect the new value", updatedName, updated.name);
         assertEquals("The updated description should reflect the new value", updatedDescription, updated.description);
      }
   }

   @Test
   public void testInterleavedWorkspace() throws Exception
   {
      String origName = "Test Workspace";
      String origDescription = "This is a test workspace.";

      String updatedName = "Updated Workspace";
      String updatedDescription = "This is an updated workspace description.";

      String updatedName2 = "Another Workspace";
      String updatedDescription2 = "This is another update.";

      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = origName;
      ws.description = origDescription;

      // create the workspace
      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);
         created.name = updatedName;
         created.description = updatedDescription;

         // update the workspace and verify results
         WorkspaceMeta updated = repo.update(created);

         created.name = updatedName2;
         WorkspaceMeta updated2 = repo.update(created);

         created.description = updatedDescription2;
         WorkspaceMeta updated3 = repo.update(created);

         // verify versions are unique
         HashSet<String> versions = new HashSet<>(
               Arrays.asList(created.version, updated.version, updated2.version, updated3.version));
         assertEquals("The versions should all be different", 4, versions.size());


         // test the progression of names
         assertEquals("The first updated name should reflect the first updated name", updatedName, updated.name);
         assertEquals("The second updated name should reflect the second update name", updatedName2, updated2.name);
         assertEquals("The third updated name should reflect the second updated name", updatedName2, updated3.name);

         // test the progression of descriptions
         assertEquals("The first updated description should reflect the first updated description", updatedDescription, updated.description);
         assertEquals("The second updated description should reflect the first updated description", updatedDescription, updated2.description);
         assertEquals("The thrid updated description should reflect the second updated description", updatedDescription2, updated3.description);
      }
   }

   @Test
   public void testGetVersionedWorkspace()
   {
      assertFalse("Test not implemented", true);
   }

   @Test
   public void testRemoveWorkspace() throws Exception
   {
      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = "Test Workspace";
      ws.description = "This is a test workspace.";

      // create the workspace
      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);
         String wsId = created.id;
         String version = created.version;

         repo.remove(wsId);

         // should not be able to retrieve using id only
         Optional<WorkspaceMeta> opt = repo.get(wsId);
         assertFalse("Should not be able to retrieve the removed workspace using its id", opt.isPresent());

         // should be able to retrieve using id and version
         opt = repo.get(wsId, version);
         assertTrue("Should be able to retrieve the removed workspace using its id and version", opt.isPresent());

         WorkspaceMeta retrieved = opt.get();

         assertEquals("The id should be match the requested id", wsId, retrieved.id);
         assertEquals("The version should match the initial version", created.version, retrieved.version);
         assertEquals("The retrieved name match the original name", ws.name, retrieved.name);
         assertEquals("The retrieved description match the original description", ws.description, retrieved.description);
      }
   }

   @Test
   public void testPurgeWorkspace() throws Exception
   {
      WorkspaceMeta ws = new WorkspaceMeta();
      ws.name = "Test Workspace";
      ws.description = "This is a test workspace.";

      // create the workspace
      try (WorkspaceRepository repo = ctx.getRepository())
      {
         WorkspaceMeta created = repo.create(ws);
         String wsId = created.id;
         String version = created.version;

         repo.remove(wsId);

         // should not be able to retrieve using id only
         Optional<WorkspaceMeta> opt = repo.get(wsId);
         assertFalse("Should not be able to retrieve the purged workspace using its id", opt.isPresent());

         // should be able to retrieve using id and version
         opt = repo.get(wsId, version);
         assertFalse("Should not be able to retrieve the purged workspace using its id and version", opt.isPresent());
      }
   }
}
