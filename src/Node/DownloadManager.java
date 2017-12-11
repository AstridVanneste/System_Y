package Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

//todo: remove files from DownloadManager when they are done
public class DownloadManager implements Runnable
{
	private Thread thread;
	private LinkedList<String> queuedFiles;
	private HashMap <String, DownloadThread> threads;

	private static DownloadManager instance;

	private DownloadManager ()
	{
		this.thread = new Thread(this);
		this.thread.setName("DownloadManager Thread, Node: " + Node.getInstance().getName());
		this.queuedFiles = new LinkedList<String>();
		this.threads = new HashMap<String, DownloadThread>();
	}

	public static DownloadManager getInstance ()
	{
		if (DownloadManager.instance == null)
		{
			DownloadManager.instance = new DownloadManager();
		}

		return DownloadManager.instance;
	}

	public void start ()
	{
		this.thread.start();
	}

	@Override
	public void run()
	{
		while (true)    // todo: insert stop condition
		{
			synchronized (this.queuedFiles)
			{
				for (String filename : this.queuedFiles)
				{
					if (!this.threads.containsKey(filename))
					{
						this.threads.put(filename, new DownloadThread(filename));
						this.threads.get(filename).start();
					}
				}

				this.queuedFiles.clear();
			}

			for (String filename : this.threads.keySet())
			{
				if (this.threads.get(filename).isDone())
				{
					this.threads.get(filename).stop();
				}
			}
		}
	}

	public synchronized void submit (String filename)
	{
		this.queuedFiles.add(filename);
	}

	public boolean isDone (String filename)
	{
		if (this.threads.containsKey(filename))
		{
			return this.threads.get(filename).isDone();
		}
		else
		{
			System.err.println("Requested status on non-existant file '" + filename + "'");
			return true; //todo: Find a proper solution to indicate errors, Exceptions?
		}
	}

	public LinkedList<String> getBusy ()
	{
		LinkedList <String> list = new LinkedList<String>();
		for (String file : this.threads.keySet())
		{
			if (!this.threads.get(file).isDone())
			{
				list.add(file);
			}
		}

		return list;
	}

	public void removeFileFromList (String filename)
	{
		if (this.threads.containsKey(filename))
		{
			if (this.threads.get(filename).isDone())
			{
				this.threads.remove(filename);
			}
			else
			{
				//todo: Handle errors
			}
		}
		else
		{
			//todo: Handle errors
		}
	}

	public Set<String> getDownloadList ()
	{
		return this.threads.keySet();
	}
}
