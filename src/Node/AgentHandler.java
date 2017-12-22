package Node;


import GUI.MainController;
import GUI.ManageController;
import IO.File;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import static java.rmi.server.RemoteServer.getClientHost;

public class AgentHandler implements AgentHandlerInterface, Runnable
{
	private LinkedList <String> allFiles;
	private LinkedList <String> advertiseQueue;
	private LinkedList <String> downloadQueue;
	private LinkedList <String> removeQueue;
	private Semaphore proceedSem;
	private Thread thread;
	private LinkedList<Agent> finishedAgents;

	public AgentHandler ()
	{
		this.allFiles = new LinkedList<String>();
		this.advertiseQueue = new LinkedList<String>();
		this.downloadQueue = new LinkedList<String>();
		this.removeQueue = new LinkedList<String>();
		this.proceedSem =  new Semaphore(1, true);
		this.thread = new Thread(this);
		this.finishedAgents = new LinkedList<Agent>();
	}

	public void start()
	{
		try
		{
			this.proceedSem.acquire(1);
			this.thread.setName("AgentHandler Thread - " + Node.getInstance().getName());
			this.thread.start();
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				proceedSem.acquire( 1);

				Agent agent = this.finishedAgents.removeFirst();

				if (!agent.isFinished())
				{
					short nextId = Node.getInstance().getNextNeighbour();
					try
					{
						while (Node.getInstance().getFailureAgent().getActiveFailures().contains(nextId))
						{
							//System.out.println(nextId + " was in the failure-list.");
							nextId = Node.getInstance().getResolverStub().getNext(nextId);
						}

						//System.out.println("Sending agent to " + nextId);
					}
					catch(RemoteException re)
					{
						re.printStackTrace();
					}
					try
					{
						/*
						if (agent instanceof FileAgent)
						{
							System.out.println("FileAgent");
						}
						else
						{
							System.out.println("RecoveryAgent");
						}
						*/

						//System.out.println("1");
						Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(nextId));
						//System.out.println("2");
						AgentHandlerInterface remoteAgentHandler = (AgentHandlerInterface) reg.lookup(Node.AGENT_HANDLER_NAME);
						//System.out.println("3");
						remoteAgentHandler.runAgent(agent);
						//System.out.println("4");
					}
					catch (RemoteException | NotBoundException e)
					{
						System.err.println("AgentHandler.runAgent() - " + nextId);
						Node.getInstance().getFailureAgent().failure(nextId);
						e.printStackTrace();
					}
				}
				else
				{
					Node.getInstance().getAgentHandler().runAgent(new FileAgent());
				}

			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void runAgent(Agent agent)
	{
		if(agent instanceof FileAgent)
		{
			RingMonitor.getInstance().fileAgentPassed();
		}
		try
		{
			if (agent instanceof FileAgent)
			{
				RingMonitor.getInstance().fileAgentPassed();
			}

			if (Node.getInstance().getFileManager().isRunning() || (this.removeQueue.size() > 0))
			{
				Thread agentThread = new Thread(agent);
				agentThread.setName("FileAgentThread, Node: " + Node.getInstance().getName());
				agentThread.start();
				agentThread.join();
			}

			this.finishedAgents.add(agent);
			//ManageController.getInstance().getMainController().updateFiles(allFiles);
			this.proceedSem.release(1);
		}
		catch (InterruptedException ie)
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
			//System.out.println("Add file to queue");
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
