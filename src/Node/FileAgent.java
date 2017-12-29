package Node;

import GUI.MainController;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeMap;

public class FileAgent extends Agent implements Serializable
{
	private TreeMap<String, DownloadPair> fileMap;

	public FileAgent ()
	{
		this.fileMap = new TreeMap<String, DownloadPair>();
	}

	@Override
	public void run()
	{
		//System.out.println("Running FileAgent, Thread:  " + Thread.currentThread().getName());

		LinkedList<String> newFiles = new LinkedList<String>();
		newFiles.addAll(Node.getInstance().getAgentHandler().getAdvertiseQueue());

		// A node advertised these files as 'network available', add them to the fileMap
		if (newFiles.size() > 0)
		{
			for (String filename : newFiles)
			{
				if (!this.fileMap.containsKey(filename))
				{
					//We add these files to the filemap, but only if they don't exist already
					this.fileMap.put(filename, new DownloadPair(1, Node.DEFAULT_ID));
				}
				else
				{
					System.err.println("Tried to add file that was already available in the network");
				}
			}

			Node.getInstance().getAgentHandler().clearAdvertiseQueue();
		}

		LinkedList<String> removeList = new LinkedList<String>();
		removeList.addAll(Node.getInstance().getAgentHandler().getRemoveQueue());

		// Remove all files (from the network) that this node requested to be removed
		if (removeList.size() > 0)
		{
			for (String filename : removeList)
			{
				if (this.fileMap.containsKey(filename))
				{
					this.fileMap.remove(filename);
					try
					{
						short ownerId = Node.getInstance().getResolverStub().getOwnerID(filename);
						Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(ownerId));
						FileManagerInterface ownerFM = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
						ownerFM.deleteFileInNetwork(filename);
					}
					catch (RemoteException | NotBoundException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					System.err.println("Tried to remove file that wasn't present in the network");
				}
			}

			Node.getInstance().getAgentHandler().clearRemoveQueue();
		}

		Node.getInstance().getAgentHandler().setAllFiles(this.fileMap.keySet());

		LinkedList<String> downloadQueue = Node.getInstance().getAgentHandler().getDownloadQueue();

		LinkedList<String> reDownloadQueue = new LinkedList<String>();

		// If a file is present in the network and isn't locked by someone else, start downloading
		if (downloadQueue.size() > 0)
		{
			for (String filename : downloadQueue)
			{
				//System.out.println("Downloading file " + filename);
				if (this.fileMap.containsKey(filename))
				{
					if (this.fileMap.get(filename).availablePermits() >= 1)
					{
						try
						{
							//System.out.println("Requesting lock on file");
							this.fileMap.get(filename).acquire(1);
							//System.out.println("Got lock on file");
							this.fileMap.get(filename).setID(Node.getInstance().getId()); // Set the request ID to our ID, so we know we asked for this one when it comes back around
							DownloadManager.getInstance().submit(filename);
							//System.out.println("Finished submitting");
						}
						catch (InterruptedException ie)
						{
							System.err.println("An exception was thrown while trying to acquire" + Integer.toString(this.fileMap.get(filename).availablePermits()) + " locks on file '" + filename + "'");
							ie.printStackTrace();
						}
					}
					else
					{
						System.err.println("No permits left for file '" + filename + "'");
						reDownloadQueue.addLast(filename);
					}
				}
				else
				{
					System.err.println("Requested non-existant file for download: '" + filename + "'");
					downloadQueue.remove(filename);
				}
			}
		}

		Node.getInstance().getAgentHandler().clearDownloadQueue();

		if (reDownloadQueue.size() > 0)
		{
			for (String filename : reDownloadQueue)
			{
				//System.out.println("Adding " + filename + "to re-download queue");
				Node.getInstance().getAgentHandler().downloadFile(filename);
			}
		}

		// Iterate over all downloads, if a file was requested by us and the download is done, remove it from the DownloadManager
		if (DownloadManager.getInstance().getDownloadList().size() > 0)
		{
			for (String filename : DownloadManager.getInstance().getDownloadList())
			{
				//System.out.println("Checking if file " + filename + " is done.");
				// The download is done and we're the one that requested it
				if (DownloadManager.getInstance().isDone(filename) && (this.fileMap.get(filename).getID() == Node.getInstance().getId()))
				{
					//System.out.println("We requested " + filename + " and the download is marked done");
					DownloadManager.getInstance().removeFileFromList(filename);
					this.fileMap.get(filename).setID(Node.DEFAULT_ID); // Set the ID of the node that requested the file back to default.
					this.fileMap.get(filename).release(1);
				}
			}
		}
	}

	@Override
	public boolean isFinished()
	{
		return false;
	}
}
