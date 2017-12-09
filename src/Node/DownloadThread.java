package Node;

public class DownloadThread implements Runnable
{
	private boolean done;
	private String filename;
	private Thread thread;

	public DownloadThread (String filename)
	{
		this.done = false;
		this.filename = filename;
		this.thread = new Thread (this);
		this.thread.setName("DownloadThread, file: " + this.filename);
	}

	public void start ()
	{
		this.thread.start();
	}

	@Override
	public void run()
	{
		Node.getInstance().getFileManager().requestFile(filename);
		this.done = true;
	}

	public synchronized boolean isDone ()
	{
		return this.done;
	}

	public void stop ()
	{
		try
		{
			this.thread.join();
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}
}
