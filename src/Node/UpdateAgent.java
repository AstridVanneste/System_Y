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

	public void watcher()
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

			for (WatchEvent<?> event : watchKey.pollEvents())
			{
				WatchEvent.Kind<?> kind = event.kind();
				Path eventPath = (Path) event.context();
				System.out.println(eventDir + " :" + kind + " : " + eventPath);

				// this check is necessary just in case
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println("File created");
					//handle...
					continue;
				}

			}

			watchKey.reset();

		}
	}

	public void stop(){
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
