package edu.tamu.tcat.vwise.impl.memory;

import java.util.Collection;
import java.util.Optional;

import edu.tamu.tcat.vwise.WorkspaceRepository;
import edu.tamu.tcat.vwise.model.WorkspaceMeta;

public class WorkspaceRepoImpl implements WorkspaceRepository
{

   @Override
   public Collection<WorkspaceMeta> listAll()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Optional<WorkspaceMeta> get(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Optional<WorkspaceMeta> get(String id, String version)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public WorkspaceMeta create(WorkspaceMeta data)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public WorkspaceMeta update(WorkspaceMeta data)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void remove(String id)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void purge(String id)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void close() throws Exception
   {
      // TODO Auto-generated method stub

   }

}
