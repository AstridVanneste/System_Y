package Node;

import GUI.TableFile;

import java.io.IOException;
import java.nio.file.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private Path localDir;
	private Thread thread;
	private boolean running;

	public UpdateAgent()
	{
		this.localDir = null;
	}

	/**
	 * startup of the directorywatcher
	 */
	public void start()
	{
		this.localDir = Paths.get(Node.getInstance().getFileManager().getFolder(FileType.LOCAL_FILE));

		try
		{
			this.running = true;
			this.service = FileSystems.getDefault().newWatchService();

			//specify which entries should be watched. in this case only the creation of  a file will be watched.
			this.localDir.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			this.thread = new Thread(this);
			this.thread.setName("Node.UpdateAgent Thread: " + Node.getInstance().getName());
			this.thread.start();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * If a file is added, watcher will be notified.
	 * Then it will handle a new file using the correct procedure
	 */
	public void run()
	{
		while(this.running)
		{
			try
			{
				WatchKey watchkey = this.service.take();

				//events will in THIS case always return a path, so it can be cast as one
				Path eventDir = (Path)watchkey.watchable();

				//iterate over all possible events
				for(WatchEvent<?> event : watchkey.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind(); // todo: Shouldn't we check the type of event?
					Path eventPath = (Path)event.context();

					//Node.getInstance().getController().addFile(new TableFile(eventPath.toString(), "Not supported yet"));

					short ownerId = Node.DEFAULT_ID;

					try
					{
						ownerId = Node.getInstance().getResolverStub().getOwnerID(eventPath.toString());

						//when owner is different from own id, no changes need to be made
						if(ownerId != Node.getInstance().getId())   //todo: A file is modified locally, shouldn't we warn other people (replica's, owners, nodes)
						{
							Node.getInstance().getFileManager().sendFile(ownerId,eventPath.toString(),FileType.LOCAL_FILE,FileType.OWNED_FILE);
						}

						//when owner is the same as your own id
						//You are the owner, but the local file should be held by the previous neighbour
						if(ownerId == Node.getInstance().getId())
						{
							Node.getInstance().getFileManager().sendFile(Node.getInstance().getPreviousNeighbour(),eventPath.toString(),FileType.LOCAL_FILE,FileType.REPLICATED_FILE);
						}
					}
					catch (RemoteException e)
					{
						e.printStackTrace();
						System.err.println("UpdateAgent.run");
						Node.getInstance().getFailureAgent().failure(ownerId);
					}

					Node.getInstance().getAgentHandler().advertiseFile(eventPath.toString());
				}

				watchkey.reset();

			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
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
