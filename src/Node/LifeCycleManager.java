package Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Astrid on 07/11/2017.
 */
public class LifeCycleManager
{
	public LifeCycleManager()
	{

	}


	/**
	 * Gracefully removes the node from the system.
	 */
	public void shutdown()
	{
		String IPprevious = "";
		String IPnext = "";

		/*try
		{
			IPprevious = Node.getInstance().getResolverStub().getIP(Node.getInstance().getPreviousNeighbour());
			IPnext = Node.getInstance().getResolverStub().getIP(Node.getInstance().getNextNeighbour());
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}*/


		try
		{
			Registry registryPrev = LocateRegistry.getRegistry(IPprevious);
			NodeInteractionInterface previousNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			//previousNode.setNextNeighbour(Node.getInstance().getNextNeighbour());
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
			//nextNode.setPreviousNeighbour(Node.getInstance().getPreviousNeighbour());
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
		}


		/*try
		{
			Node.getInstance().getShutdownStub().requestShutdown(Node.getInstance().getId());
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}*/
	}
}
