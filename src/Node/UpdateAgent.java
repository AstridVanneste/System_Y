package Node;

import java.io.IOException;
import java.nio.file.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private Path LOCAL_DIR;
	private Thread thread;
	public boolean running;

	public UpdateAgent()
	{
		this.LOCAL_DIR = null;
	}

	/**
	 * startup of the directorywatcher
	 */
	public void start()
	{
		LOCAL_DIR = Paths.get(Node.getInstance().getFileManager().getFolder(FileType.LOCAL_FILE));

		try
		{
			this.running = true;
			this.service = FileSystems.getDefault().newWatchService();

			//specify which entries should be watched. in this case only the creation of  a file will be watched.
			LOCAL_DIR.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			this.thread = new Thread(this);
			this.thread.setName("Thread - Node.UpdateAgent Thread");
			this.thread.start();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void run()
	{
		while(this.running)
		{
			watcher();

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * If a file is added, watcher will be notified.
	 * Then it will handle a new file using the correct procedure
	 */
	public void watcher()
	{
		try
		{

			WatchKey watchkey = service.take();

			//events will in THIS case always return a path, so it can be cast as one
			Path eventDir = (Path)watchkey.watchable();

			//iterate over all possible events
			for(WatchEvent<?> event : watchkey.pollEvents())
			{
				WatchEvent.Kind<?> kind = event.kind();
				Path eventPath = (Path)event.context();

				short idFileOwner = Node.DEFAULT_ID;

				try
				{
					idFileOwner = Node.getInstance().getResolverStub().getOwnerID(eventPath.toString());

					//when owner is different from own id, no changes need to be made
					if(idFileOwner != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().sendFile(idFileOwner,eventPath.toString(),FileType.LOCAL_FILE,FileType.OWNED_FILE);
						Registry registry = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(idFileOwner));
						FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);
						fileManager.addFileLedger(new FileLedger(eventPath.toString(),idFileOwner,Node.getInstance().getId(),Node.DEFAULT_ID));
					}

					//when owner is the same as your own id
					//You are the owner, but the local file should be held by the previous neighbour
					if(idFileOwner == Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().sendFile(Node.getInstance().getPreviousNeighbour(),eventPath.toString(),FileType.LOCAL_FILE,FileType.REPLICATED_FILE);
						Node.getInstance().getFileManager().addFileLedger(new FileLedger(eventPath.toString(),Node.getInstance().getId(),Node.getInstance().getId(), Node.getInstance().getPreviousNeighbour()));
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					Node.getInstance().getFailureAgent().failure(idFileOwner);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (NotBoundException e)
				{
					e.printStackTrace();
				}
			}

			watchkey.reset();

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void stop()
	{
		try
		{
			this.running = false;
			this.service.close();
			this.thread.join();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException ie)
		{
			System.err.println("An exception was thrown while trying to join UpdateAgent Thread");
			ie.printStackTrace();
		}
	}
}
