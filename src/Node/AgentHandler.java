package Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
		try
		{
			Thread agentThread = new Thread(agent);
			agentThread.setName("Thread - FileAgentThread, Node: " + Node.getInstance().getName());
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

	public LinkedList<String> getQueuedFiles()
	{
		return this.queuedFiles;
	}

	// Method is synchronized because FileAgent uses it to re-add unfinished tasks
	public synchronized void downloadFile (String filename)
	{
		if (!this.queuedFiles.contains(filename))
		{
			this.queuedFiles.addLast(filename);
		}
	}
}
