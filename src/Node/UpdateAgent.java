package Node;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UpdateAgent implements Runnable
{
	private WatchService service;
	private Path path;
	private Map keyMap;
	private WatchKey watchkey;
	public UpdateAgent(){

	}

	public void start(){
		try{
			service = FileSystems.getDefault().newWatchService();
			keyMap= new HashMap<WatchKey, Path>();

			//directory of files
			path = Paths.get("Files");

			Thread thread = new Thread(this);
			thread.start();

			//specify which entries should be watched. in this case only the creation of  a file will be watched.
			keyMap.put(path.register(service,StandardWatchEventKinds.ENTRY_CREATE),path);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void run(){
		watcher();
	}

	public void watcher(){
		try
		{
			watchkey = service.take();
			Path eventDir = (Path)keyMap.get(watchkey);
			 for(WatchEvent<?> event : watchkey.pollEvents()){
				WatchEvent.Kind<?> kind = event.kind();
				Path eventPath = (Path)event.context();
			 	System.out.println(eventDir +  " :" + kind + " : " + eventPath);
			 }
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
