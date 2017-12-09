package GUI;

import Node.Node;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Set;

import Node.FileLedger;

import static Node.FileType.*;

public class Controller {
	public Controller()
	{

	}

	public void updateFiles()
	{

	}

	public void openFile(String fileName)
	{

	}

	public void deleteLocal(String fileName)
	{
		if(Node.getInstance().getFileManager().hasFile(fileName, LOCAL_FILE))
		{
			try
			{
				Node.getInstance().getFileManager().deleteFile(fileName,LOCAL_FILE);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			FileLedger fileLedger;

			short ownerId = 0;
			try
			{
				ownerId = Node.getInstance().getResolverStub().getOwnerID(fileName);
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
			fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,fileName);
			short localId = fileLedger.getLocalID();
			if(localId != 0)
			{
				Node.getInstance().getFileManager().deleteFileRemote(localId,fileName,LOCAL_FILE);
			}
		}
	}

	public void deleteNetwork(String fileName)
	{
		if(Node.getInstance().getFileManager().hasFile(fileName, OWNED_FILE))
		{
			FileLedger fileLedger = Node.getInstance().getFileManager().getFileLedgers().get(fileName);
			short localId = fileLedger.getLocalID();
			short replicatedId = fileLedger.getReplicatedId();
			Set<Short> downloaders = fileLedger.getCopies();
			if(localId != 0)
			{
				Node.getInstance().getFileManager().deleteFileRemote(localId,fileName,LOCAL_FILE);

			}
			if(localId != 0)
			{
				Node.getInstance().getFileManager().deleteFileRemote(replicatedId,fileName,REPLICATED_FILE);
			}
			for(short downId : downloaders){
				Node.getInstance().getFileManager().deleteFileRemote(downId,fileName,DOWNLOADED_FILE);
			}
			try
			{
				Node.getInstance().getFileManager().deleteFile(fileName,OWNED_FILE);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			Node.getInstance().getFileManager().deleteFileLedger(fileLedger.getFileName());
		}
		else
		{
			FileLedger fileLedger;
			try
			{
				short ownerId = Node.getInstance().getResolverStub().getOwnerID(fileName);
				fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,fileName);
				short localId = fileLedger.getLocalID();
				short replicatedId = fileLedger.getReplicatedId();
				Set<Short> downloaders = fileLedger.getCopies();
				if(localId != 0)
				{
					if(localId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(localId,fileName,LOCAL_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(fileName,LOCAL_FILE);
					}
				}
				if(replicatedId != 0)
				{
					if(replicatedId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(replicatedId,fileName,REPLICATED_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(fileName,REPLICATED_FILE);
					}
				}
				for(short downId : downloaders){
					if(downId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(downId,fileName,DOWNLOADED_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(fileName,DOWNLOADED_FILE);
					}
				}
				if(ownerId != 0)
				{
					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,fileName,OWNED_FILE);
					Node.getInstance().getFileManager().deleteFileLedgerRemote(fileLedger.getFileName());
				}
			} catch (RemoteException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void shutdown()
	{
		Node.getInstance().stop();
	}

	/*
	public void pressButton (ActionEvent event) {
		System.out.println("Hello world");
	}*/
}
