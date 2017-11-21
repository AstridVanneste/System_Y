package Node;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private WatchKey watchkey;
	private final Path LOCAL_DIR;

	public UpdateAgent(){
		this.LOCAL_DIR = Paths.get("Files");
	}

	/**
	 * startup of the directorywatcher
	 */
	public void start(){
		try{
			service = FileSystems.getDefault().newWatchService();

			//specify which entries should be watched. in this case only the creation of  a file will be watched.
			LOCAL_DIR.register(service, StandardWatchEventKinds.ENTRY_CREATE);

			Thread thread = new Thread(this);
			thread.start();


		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}


	public void run(){
		while(true){
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
				System.out.println(eventDir +  " :" + kind + " : " + eventPath);
			}

			watchkey.reset();

		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}




	public void stop(){
		try
		{
			service.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
