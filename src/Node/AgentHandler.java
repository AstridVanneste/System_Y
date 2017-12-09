package Node;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class AgentHandler implements AgentHandlerInterface
{
	private LinkedList <String> allFiles;
	private LinkedList <String> advertiseQueue;
	private LinkedList <String> downloadQueue;
	private LinkedList <String> removeQueue;

	public AgentHandler ()
	{
		this.allFiles = new LinkedList<String>();
		this.advertiseQueue = new LinkedList<String>();
		this.downloadQueue = new LinkedList<String>();
		this.removeQueue = new LinkedList<String>();
	}

	@Override
	public void runAgent(Agent agent)
	{
		try
		{
			Thread agentThread = new Thread(agent);
			agentThread.setName("FileAgentThread, Node: " + Node.getInstance().getName());
			agentThread.start();

			agentThread.join();

			if (!agent.isFinished())
			{
				Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(Node.getInstance().getNextNeighbour()));
				AgentHandlerInterface remoteAgentHandler = (AgentHandlerInterface) reg.lookup(Node.AGENT_HANDLER_NAME);
				remoteAgentHandler.runAgent(agent);
			}
		}
		catch (InterruptedException | RemoteException | NotBoundException ie)
		{
			ie.printStackTrace();
		}
	}

	public synchronized LinkedList<String> getDownloadQueue()
	{
		return this.downloadQueue;
	}

	public synchronized LinkedList<String> getRemoveQueue ()
	{
		return this.removeQueue;
	}

	public synchronized LinkedList<String> getAdvertiseQueue ()
	{
		return this.advertiseQueue;
	}

	public synchronized  LinkedList<String> getAllFiles ()
	{
		return this.allFiles;
	}

	public synchronized void clearDownloadQueue ()
	{
		this.downloadQueue.clear();
	}

	public synchronized void clearRemoveQueue ()
	{
		this.removeQueue.clear();
	}

	public synchronized void clearAdvertiseQueue ()
	{
		this.advertiseQueue.clear();
	}

	public synchronized void setAllFiles (Set<String> files)
	{
		this.allFiles.clear();
		this.allFiles.addAll(files);
	}

	// Method is synchronized because FileAgent uses it to re-add unfinished tasks
	public synchronized void downloadFile (String filename)
	{
		if (!this.downloadQueue.contains(filename))
		{
			this.downloadQueue.addLast(filename);
		}
	}

	public synchronized void deleteFile (String filename)
	{
		if (!this.removeQueue.contains(filename))
		{
			this.removeQueue.addLast(filename);
		}
	}

	public synchronized void advertiseFile (String filename)
	{
		if (!this.advertiseQueue.contains(filename))
		{
			this.advertiseQueue.add(filename);
		}
	}
}
