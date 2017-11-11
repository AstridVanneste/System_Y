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
		/*try
		{
			IPprev = Node.getInstance().getResolverStub().getIP(Node.getInstance().getResolverStub().getPrevious(ID));
			IPnext = Node.getInstance().getResolverStub().getIP(Node.getInstance().getResolverStub().getNext(ID));

		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}*/

		try
		{
			Registry registryPrev = LocateRegistry.getRegistry(IPprev);
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
			Node.getInstance().getShutdownStub().requestShutdown(ID);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}*/

	}
}
