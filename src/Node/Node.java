package Node;

//import GUI.Controller;
import GUI.MainController;
import GUI.ManageController;
import NameServer.*;

import java.rmi.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Node implements NodeInteractionInterface
{
	public static final short DEFAULT_ID = -1;
	public static final String NODE_INTERACTION_NAME = "NODE_INTERACTION";
	public static final String FILE_MANAGER_NAME = "FILE_MANAGER";
	public static final String AGENT_HANDLER_NAME = "AGENT_HANDLER";

	private String name;
	private Short id;
	private Short previousNeighbour;
	private Short nextNeighbour;

	private Boolean GUIActive;

	private LifeCycleManager lifeCycleManager;
	private FailureAgent failureAgent;
	private FileManager fileManager;
	private UpdateAgent updateAgent;
	private AgentHandler agentHandler;

	private ResolverInterface resolverStub;

	private Semaphore neighbourSetSemaphore;

	private static Node instance;

	public Node()
	{
		this.name = "";
		this.id = DEFAULT_ID;
		this.previousNeighbour = DEFAULT_ID;
		this.nextNeighbour = DEFAULT_ID;
		this.GUIActive = false;
		this.lifeCycleManager = new LifeCycleManager();
		this.failureAgent = new FailureAgent();
		this.fileManager = new FileManager();
		this.resolverStub = null;
		this.updateAgent = new UpdateAgent();
		this.neighbourSetSemaphore = new Semaphore(2, true);
		this.agentHandler = new AgentHandler();
	}

	/**
	 * Returns the singleton instance of the Node.
	 * @warning THE NODE STILL NEEDS TO BE STARTED SEPARATELY
	 * @return
	 */
	public static Node getInstance()
	{
		if(Node.instance == null)
		{
			Node.instance = new Node();
		}
		return Node.instance;
	}

	public void start()
	{
		Thread.currentThread().setName("Main - " + Node.getInstance().getName());

		if (!this.lifeCycleManager.isRunning())
		{
			if (System.getSecurityManager() == null)
			{
				System.setSecurityManager(new SecurityManager());
			}

			try
			{
				this.neighbourSetSemaphore.acquire(2);

				NodeInteractionInterface nodeInteractionStub = (NodeInteractionInterface) UnicastRemoteObject.exportObject(this, 0);
				FileManagerInterface fileInteractionStub = (FileManagerInterface) UnicastRemoteObject.exportObject(this.fileManager,0);
				AgentHandlerInterface agentHandlerInterface = (AgentHandlerInterface) UnicastRemoteObject.exportObject(this.agentHandler, 0);

				Registry registry = LocateRegistry.createRegistry(1099);
				registry.bind(Node.NODE_INTERACTION_NAME, nodeInteractionStub);
				registry.bind(Node.FILE_MANAGER_NAME, fileInteractionStub);
				registry.bind(Node.AGENT_HANDLER_NAME, agentHandlerInterface);
			}
			catch (RemoteException re)
			{
				System.err.println("Exception when creating stub");
				re.printStackTrace();
			}
			catch (AlreadyBoundException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException ie)
			{
				System.err.println("Exception was thrown while trying to lock neighbour-semaphore");
				ie.printStackTrace();
			}

			this.lifeCycleManager.start();

			try
			{
				this.neighbourSetSemaphore.acquire(2);  // Blocks until (a) permit(s) become available
			}
			catch (InterruptedException ie)
			{
				System.err.println("Exception was thrown while trying to lock neighbour-semaphore");
				ie.printStackTrace();
			}

			DownloadManager.getInstance().start();

			this.fileManager.start();

			this.updateAgent.start();

			this.agentHandler.start();

			if (this.GUIActive)
				ManageController.getInstance().setCloseLoad(true);
		}
		else
		{
			System.err.println("[ERROR]\tTried to start Node that was already running.");
		}
	}

	public void stop ()
	{
		DownloadManager.getInstance().stop();

		this.updateAgent.stop();
		this.fileManager.stop();
		this.lifeCycleManager.stop(); // stop lifecycle manager first to make sure the neighbours are already changed!!

		try
		{
			Registry reg = LocateRegistry.getRegistry();
			reg.unbind(NODE_INTERACTION_NAME);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
		}

		this.resolverStub = null;

		try
		{
			UnicastRemoteObject.unexportObject(this,false);
		}
		catch (NoSuchObjectException e)
		{
			e.printStackTrace();
		}

		this.neighbourSetSemaphore.release(this.neighbourSetSemaphore.availablePermits());
	}

	public String getName()
	{
		return this.name;
	}

	/**
	 * Set new name for the node.
	 * @warning NODE PARAMETERS ARE INVALID AFTER A RESTART<br>PLEASE RESTART THE NODE AFTER CHANGING ITS NAME
	 * @param name the node's new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public LifeCycleManager getLifeCycleManager()
	{
		return this.lifeCycleManager;
	}

	public FailureAgent getFailureAgent()
	{
		return this.failureAgent;
	}

	public UpdateAgent getUpdateAgent(){
		return this.updateAgent;
	}

	public short getId()
	{
		return this.id;
	}

	// Package Private so no modifier
	void setId(short id)
	{
		this.id = id;
	}

	public Boolean getGUIActive()
	{
		return GUIActive;
	}

	public void setGUIActive(Boolean GUIActive)
	{
		this.GUIActive = GUIActive;
	}

	public short getPreviousNeighbour()
	{
		return this.previousNeighbour;
	}

	public void setPreviousNeighbour(short previousNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
	}

	public short getNextNeighbour()
	{
		return this.nextNeighbour;
	}

	public void setNextNeighbour(short nextNeighbour)
	{
		this.nextNeighbour = nextNeighbour;
	}

	public ResolverInterface getResolverStub()
	{
		return this.resolverStub;
	}

	public void setResolverStub(ResolverInterface resolverStub)
	{
		this.resolverStub = resolverStub;
	}

	public FileManager getFileManager()
	{
		return this.fileManager;
	}

	//REMOTE
	@Override
	public short getPreviousNeighbourRemote() throws RemoteException
	{
		return this.previousNeighbour;
	}

	@Override
	public void setPreviousNeighbourRemote(short previousNeighbour) throws RemoteException
	{
		this.previousNeighbour = previousNeighbour;
	}

	@Override
	public short getNextNeighbourRemote() throws RemoteException
	{
		return this.nextNeighbour;
	}

	@Override
	public void setNextNeighbourRemote(short nextNeighbour) throws RemoteException
	{
		this.nextNeighbour = nextNeighbour;
	}

	@Override
	public boolean isRunning() throws RemoteException
	{
		return this.lifeCycleManager.isRunning();
	}

	@Override
	public void releaseStartupSlot() throws RemoteException
	{

		this.neighbourSetSemaphore.release(1);
	}

	@Override
	public void runRingMonitor() throws RemoteException
	{
		RingMonitor.getInstance();
		RingMonitor.getInstance().start();
		//System.out.println("RingMonitor started");
	}

	public AgentHandler getAgentHandler ()
	{
		return this.agentHandler;
	}
}