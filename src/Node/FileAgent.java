package Node;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.TreeMap;

public class FileAgent extends Agent implements Serializable
{
	TreeMap<String, DownloadPair> fileMap;

	public FileAgent ()
	{
		this.fileMap = new TreeMap<String, DownloadPair>();
	}

	@Override
	public void run()
	{
		//System.out.println("Running FileAgent, Thread:  " + Thread.currentThread().getName());
		//todo: Request list of all files on this node and merge fileMap with this
		LinkedList<String> newFiles = Node.getInstance().getAgentHandler().getAdvertiseQueue();
		//LinkedList<String> newFiles = new LinkedList<String>();

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

		//todo: Request list of all files that need to be removed from fileMap
		LinkedList<String> removeList = Node.getInstance().getAgentHandler().getRemoveQueue();
		//LinkedList<String> removeList = new LinkedList<String>();

		// Remove all files (from the network) that this node requested to be removed
		if (removeList.size() > 0)
		{
			for (String filename : removeList)
			{
				if (this.fileMap.containsKey(filename))
				{
					this.fileMap.remove(filename);
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
				if (this.fileMap.containsKey(filename))
				{
					if (this.fileMap.get(filename).availablePermits() >= 1)
					{
						try
						{
							this.fileMap.get(filename).acquire(1);
							this.fileMap.get(filename).setID(Node.getInstance().getId()); // Set the request ID to our ID, so we know we asked for this one when it comes back around
							DownloadManager.getInstance().submit(filename);
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
				Node.getInstance().getAgentHandler().downloadFile(filename);
			}
		}

		// Iterate over all downloads, if a file was requested by us and the download is done, remove it from the DownloadManager
		if (DownloadManager.getInstance().getDownloadList().size() > 0)
		{
			for (String filename : DownloadManager.getInstance().getDownloadList())
			{
				// The download is done and we're the one that requested it
				if (DownloadManager.getInstance().isDone(filename) && (this.fileMap.get(filename).getID() == Node.getInstance().getId()))
				{
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
