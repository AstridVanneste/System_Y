package Node;

import NameServer.*;

import java.rmi.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Node implements NodeInteractionInterface
{
	public static final int DEFAULT_ID = -1;
	public static final String NODE_INTERACTION_NAME = "NODE_INTERACTION";

	private String name;
	private short id;
	private short previousNeighbour;
	private short nextNeighbour;

	private LifeCycleManager lifeCycleManager;
	private FailureAgent failureAgent;
	private FileManager fileManager;

	private ResolverInterface resolverStub;

	private static Node instance;

	public Node()
	{
		this.name = "";
		this.id = DEFAULT_ID;
		this.previousNeighbour = DEFAULT_ID;
		this.nextNeighbour = DEFAULT_ID;
		this.lifeCycleManager = new LifeCycleManager();
		this.failureAgent = new FailureAgent();
		this.fileManager = new FileManager();
		this.resolverStub = null;
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
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		try
		{
			NodeInteractionInterface stub = (NodeInteractionInterface) UnicastRemoteObject.exportObject(this,0);
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind(Node.NODE_INTERACTION_NAME, stub);

		}
		catch(RemoteException re)
		{
			System.err.println("Exception when creating stub");
			re.printStackTrace();
		}
		catch (AlreadyBoundException e)
		{
			e.printStackTrace();
		}

		this.fileManager.start();
		this.lifeCycleManager.start();
	}

	public void stop ()
	{
		this.fileManager.stop();
		this.lifeCycleManager.stop();
	}

	public String getName()
	{
		return this.name;
	}

	/**
	 * Set new name for the node.
	 * @warning THIS METHOD STOPS AND STARTS THE NODE
	 * @param name the node's new name
	 */
	public void setName(String name)
	{
		this.name = name;
		this.stop();
		this.start();
	}

	public LifeCycleManager getLifeCycleManager()
	{
		return this.lifeCycleManager;
	}

	public FailureAgent getFailureAgent()
	{
		return this.failureAgent;
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
}