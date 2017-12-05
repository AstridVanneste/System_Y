package Node;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class AgentHandler implements AgentHandlerInterface
{
	private TreeMap <String, Semaphore> fileMap;
	private LinkedList <String> queuedFiles;

	@Override
	public void runAgent(Agent agent)
	{
		if (agent instanceof FileAgent)
		{
			// We got a file agent, handle it
			FileAgent fileAgent = (FileAgent) agent;
			for (String file : queuedFiles)
			{
				fileAgent.queueFile(file);
			}
		}
		else if (agent instanceof RecoveryAgent)
		{
			// We got a recovery agent, handle it
			RecoveryAgent recoveryAgent = (RecoveryAgent) agent;
		}
	}

	public void downloadFile (String filename)
	{
		this.queuedFiles.addLast(filename);
	}
}
