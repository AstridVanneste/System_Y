package Node;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Astrid on 07/11/2017.
 */
public class FailureAgent
{
	public FailureAgent()
	{

	}


	/**
	 * Called every time a Remote Exception is called in the node. It removes a node with a given ID from the system.
	 * @param ID
	 */
	public void failure(short ID)
	{
		String IPprev = "";
		String IPnext = "";
		short next = -1;
		short prev = -1;
		try
		{
			prev = Node.getInstance().getLifeCycleManager().getResolverStub().getPrevious(ID);
			next = Node.getInstance().getLifeCycleManager().getResolverStub().getNext(ID);
			IPprev = Node.getInstance().getLifeCycleManager().getResolverStub().getIP(prev);
			IPnext = Node.getInstance().getLifeCycleManager().getResolverStub().getIP(next);
			System.out.println("PREV " + IPprev);
			System.out.println("NEXT " + IPnext);

		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		try
		{
			Registry registryPrev = LocateRegistry.getRegistry(IPprev);
			NodeInteractionInterface previousNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			previousNode.setNextNeighbour(next);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
		}


		try
		{
			Registry registryNext = LocateRegistry.getRegistry(IPnext);
			NodeInteractionInterface nextNode = (NodeInteractionInterface) registryNext.lookup(Node.NODE_INTERACTION_NAME);
			nextNode.setPreviousNeighbour(prev);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
		}

		try
		{
			Node.getInstance().getLifeCycleManager().getShutdownStub().requestShutdown(ID);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

	}
}
