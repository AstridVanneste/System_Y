package Node;

import java.io.IOException;
import java.nio.file.*;
import java.rmi.RemoteException;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private WatchKey watchkey;
	private final Path LOCAL_DIR;
	public boolean running;

	public UpdateAgent(){
		//this.LOCAL_DIR = Paths.get("Files");
		this.LOCAL_DIR = null;
	}

	/**
	 * startup of the directorywatcher
	 */
	public void start()
	{
		try{
			this.running = true;
			this.service = FileSystems.getDefault().newWatchService();

			//specify which entries should be watched. in this case only the creation of  a file will be watched.

			LOCAL_DIR.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			Thread thread = new Thread(this);
			thread.start();

		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void run()
	{
		while(this.running){
			watcher();
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * If a file is added, watcher will be notified.
	 * Then it will handle a new file using the correct procedure
	 */
	public void watcher(){
		try
		{

			watchkey = service.take();

			//events will in THIS case always return a path, so it can be cast as one
			Path eventDir = (Path)watchkey.watchable();
			//iterate over all possible events
			for(WatchEvent<?> event : watchkey.pollEvents()){
				WatchEvent.Kind<?> kind = event.kind();
				Path eventPath = (Path)event.context();


				try
				{
					short idFileOwner = Node.getInstance().getResolverStub().getOwnerID(eventPath.toString());
					//when owner is different from own id, no changes need to be made
					if(idFileOwner != Node.getInstance().getId()){
						Node.getInstance().getFileManager().sendFile(idFileOwner,eventPath.toString(),FileType.OWNED_FILE);
					}
					//when owner is the same as your own id,
					if(idFileOwner == Node.getInstance().getId()){
						Node.getInstance().getFileManager().sendFile(idFileOwner,eventPath.toString(),FileType.LOCAL_FILE);
					}
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}


				System.out.println(eventDir +  " :" + kind + " : " + eventPath);
			}

			watchkey.reset();

		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void stop()
	{
		try
		{
			this.running =false;
			service.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
