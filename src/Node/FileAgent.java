package Node;

import java.util.LinkedList;
import java.util.TreeMap;

public class FileAgent extends Agent
{
	TreeMap<String, DownloadPair> fileMap;

	public FileAgent ()
	{
		this.fileMap = new TreeMap<String, DownloadPair>();
	}

	@Override
	public void run()
	{
		//todo: Request list of all files on this node and merge fileMap with this
		//todo: Request list of all files that need to be removed from fileMap

		//todo: Request file queue from Node.AgentHandler
		LinkedList<String> queuedFiles = new LinkedList<String>();

		for (String filename : queuedFiles)
		{
			if (this.fileMap.containsKey(filename))
			{
				if (this.fileMap.get(filename).availablePermits() >= 1)
				{
					try
					{
						this.fileMap.get(filename).acquire(this.fileMap.get(filename).availablePermits());
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
				}
			}
			else
			{
				System.err.println("Requested non-existant file for download: '" + filename + "'");
				queuedFiles.remove(filename);
			}
		}

		//todo: Handle downloads that are done
	}

	@Override
	public boolean isFinished()
	{
		return false;
	}
}
