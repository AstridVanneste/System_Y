package Node;

import java.io.IOException;
import java.nio.file.*;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private final Path LOCAL_DIR;
	private WatchKey watchKey;

	public UpdateAgent()
	{
		this.LOCAL_DIR = Paths.get("Local/");
	}

	public void start()
	{
		try
		{
			this.service = FileSystems.getDefault().newWatchService();

			// Specifying the type of events we want to monitor -> creature of a file
			LOCAL_DIR.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			Thread thread = new Thread(this);
			thread.start();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void run()
	{
		watcher();
	}

	/**
	 * Processing events in the local-map
	 */
	private void watcher()
	{
		for (;;)
		{

			// wait for key to be signalled
			try
			{
				watchKey = service.take();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return;
			}

			Path eventDir = (Path) watchKey.watchable();
			if (eventDir == null)
			{
				System.err.println("WatchKey not recognized!");
				continue;
			}

			// Process the pending events for the key
			for (WatchEvent<?> event : watchKey.pollEvents())
			{
				// Retrieve the type of event
				WatchEvent.Kind<?> kind = event.kind();

				// The file name is stored as the context of the event
				Path eventPath = (Path) event.context();

				System.out.println(eventDir + " :" + kind + " : " + eventPath);

				// This check is necessary just in case
				if (kind == StandardWatchEventKinds.OVERFLOW)
				{
					continue;
				}

				// File is created
				if (kind == StandardWatchEventKinds.ENTRY_CREATE)
				{
					System.out.println("File created");
					//handle...
					continue;
				}

			}

			// Put the key back into a ready state by invoking reset. If the key is no longer valid,
			// the directory is inaccessible so exit the loop.
			if (!watchKey.reset())
			{
				System.out.println("Watch key reset is failed. No listening anymore");
				break;
			}

		}
	}

	public void stop()
	{
		try
		{
			service.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
