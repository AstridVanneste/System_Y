package Node;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

/**
 * Created by Astrid on 04/12/2017.
 */
public class RecoveryAgent extends Agent
{
	private short failedId;
	private short callerId;
	private HashMap<String, FileLedger> ledgers;
	private boolean finished;

	public RecoveryAgent(short failedId, short callerId)
	{
		this.failedId = failedId;
		this.callerId = callerId;
		this.ledgers = new HashMap<String, FileLedger>();
		this.finished = false;
	}

	@Override
	public void run()
	{
		//OWNED FILES
		HashMap<String, FileLedger> ownedFilesMap = Node.getInstance().getFileManager().getFileLedgers();
		for(String filename:  ownedFilesMap.keySet())
		{
			FileLedger ledger = ownedFilesMap.get(filename);

			if(ledger.getLocalID() == failedId)		//the failed id was local of the file
			{
				if(ledger.getNumDownloads() > 0)	//the file has been downloaded so we need to replicate the file
				{

					Node.getInstance().getFileManager().sendFile(Node.getInstance().getPreviousNeighbour(),filename,FileType.OWNED_FILE, FileType.REPLICATED_FILE ); //send the file to your previous neighbour as replicated file
					ledger.setLocalID(Node.DEFAULT_ID);
					ledger.setReplicatedId(Node.getInstance().getPreviousNeighbour());

					Node.getInstance().getFileManager().replaceFileLedger(ledger);            	//change the replicated id and invalidate the local id in the fileledger
																								//previous ledger will be replaced
				}
				else								//the file has not been downloaded yet so we will delete it from the system
				{
					File file = new File(Node.getInstance().getFileManager().getFullPath(filename, FileType.OWNED_FILE));
					file.delete();
					Node.getInstance().getFileManager().deleteFileLedger(filename);
				}
			}
			else if(ledger.getReplicatedId() == failedId) //the failed id was replicated of the file. We need to replicate it again.
			{
				Node.getInstance().getFileManager().sendFile(Node.getInstance().getPreviousNeighbour(),filename,FileType.OWNED_FILE,FileType.REPLICATED_FILE); //send the file to your previous neighbour as replicated file
			}
		}


		//For files of which te failed node was owner we will need to do the entire circle to recreate the original fileledger.
		//When we have finished the circle we will send the file (ask the local to send the file) and send the fileledgers.

		//LOCAL FILES

		File folder = new File(Node.getInstance().getFileManager().getFolder(FileType.LOCAL_FILE));
		for(File file: folder.listFiles())
		{
			if(this.wasOwner(file.getName()))	//failed node was owner of the file
			{
				if(ledgers.keySet().contains(file.getName()))
				{
					ledgers.get(file.getName()).setLocalID(Node.getInstance().getId());
				}
				else
				{
					this.ledgers.put(file.getName(),new FileLedger(file.getName(),Node.getInstance().getId(),Node.DEFAULT_ID,Node.DEFAULT_ID)); //file was not yet detected => we need to create a ledger!
				}
			}
		}

		//REPLICATED FILES

		folder = new File(Node.getInstance().getFileManager().getFolder(FileType.REPLICATED_FILE));

		for(File file: folder.listFiles())
		{
			if(this.wasOwner(file.getName()))	//failed node was owner of the file
			{
				if(ledgers.keySet().contains(file.getName()))
				{
					ledgers.get(file.getName()).setReplicatedId(Node.getInstance().getId());
				}
				else
				{
					this.ledgers.put(file.getName(),new FileLedger(file.getName(),Node.DEFAULT_ID,Node.DEFAULT_ID,Node.getInstance().getId())); //file was not yet detected => we need to create a ledger!
				}
			}
		}

		//DOWNLOADED FILES

		folder = new File(Node.getInstance().getFileManager().getFolder(FileType.DOWNLOADED_FILE));

		for(File file: folder.listFiles())
		{
			if(this.wasOwner(file.getName()))	//failed node was owner of the file
			{
				if(ledgers.keySet().contains(file.getName()))
				{
					ledgers.get(file.getName()).addDownloader(Node.getInstance().getId());
				}
				else
				{
					FileLedger ledger = new FileLedger(file.getName(),Node.DEFAULT_ID,Node.DEFAULT_ID,Node.DEFAULT_ID);
					ledger.addDownloader(Node.getInstance().getId());
					this.ledgers.put(file.getName(),ledger); //file was not yet detected => we need to create a ledger!
				}
			}
		}


		if(callerId == Node.getInstance().getNextNeighbour())	//agent has made it back to the start. the ledgers are complete and can be send to the owners.
		{
			for(String filename: this.ledgers.keySet())
			{
				FileLedger ledger = this.ledgers.get(filename);

				short owner = -1;
				String ownerIP = "";

				try
				{
					owner = Node.getInstance().getResolverStub().getOwnerID(filename);
					ownerIP = Node.getInstance().getResolverStub().getIP(owner);
				}
				catch(RemoteException re)
				{
					re.printStackTrace();
				}

				try
				{
					Registry reg = LocateRegistry.getRegistry(ownerIP);
					FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
					remoteFileManager.addFileLedger(ledger);
				}
				catch(RemoteException re)
				{
					re.printStackTrace();
					Node.getInstance().getFailureAgent().failure(owner);
				}
				catch (NotBoundException | IOException nbi)
				{
					nbi.printStackTrace();
				}


				short localID = this.ledgers.get(filename).getLocalID();
				String localIP = "";
				try
				{
					localIP = Node.getInstance().getResolverStub().getIP(localID);
				}
				catch(RemoteException re)
				{
					re.printStackTrace();
				}

				try
				{
					Registry reg = LocateRegistry.getRegistry(localIP);
					FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
					remoteFileManager.copyFile(filename,this.ledgers.get(filename).getOwnerID(),FileType.LOCAL_FILE, FileType.OWNED_FILE);

				}
				catch(RemoteException | NotBoundException re)
				{
					re.printStackTrace();
					Node.getInstance().getFailureAgent().failure(localID);
				}

			}


			this.finished = true;
		}

	}

	private boolean wasOwner(String filename)
	{
		try
		{
			short owner = Node.getInstance().getResolverStub().getOwnerID(filename);
			short fileHash = Node.getInstance().getResolverStub().getHash(filename);

			if(owner < this.failedId && this.failedId < fileHash)						//OWNER=====FAILED=====FILEHASH
			{
				return true;
			}
			else if(owner > this.failedId && this.failedId > fileHash) 					//OWNER=====FAILED=====||=====FILEHASH	(|| is rollover point)
			{
				return true;
			}
			else if(this.failedId <= fileHash && fileHash < owner)						//OWNER=====||=====FAILED=====FILEHASH	(|| is rollover point)
			{
				return true;
			}

		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean isFinished()
	{
		return this.finished;
	}
}


