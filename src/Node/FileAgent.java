package Node;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class FileAgent extends Agent
{
	private LinkedList<String> fileQueue;

	@Override
	public void run()
	{
		TreeMap <String, Semaphore> fileMap = new TreeMap<String, Semaphore>();

		// 1.) Update the file-list
		for (String file : Node.getInstance().getFileManager().getOwnedFiles())
		{
			if (!fileMap.containsKey(file))
			{
				fileMap.put(file, new Semaphore(1, true));
			}
		}

		this.fileQueue.clear();
	}

	public void queueFile (String filename)
	{
		this.fileQueue.addLast(filename);
	}
}
