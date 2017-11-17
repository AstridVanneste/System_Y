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
	 */
	public void failure(short firstID, short lastID)
	{
		String IPprev = "";
		String IPnext = "";
		short nextID = -1;
		short prevID = -1;
		boolean prevFailed = false;
		boolean nextFailed = false;
		NodeInteractionInterface prevNode = null;
		NodeInteractionInterface nextNode = null;

		try
		{
			prevID = Node.getInstance().getResolverStub().getPrevious(firstID);
			nextID = Node.getInstance().getResolverStub().getNext(lastID);
			IPprev = Node.getInstance().getResolverStub().getIP(prevID);
			IPnext = Node.getInstance().getResolverStub().getIP(nextID);
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
			prevNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			prevNode.setNextNeighbourRemote(nextID);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
			prevFailed = true;
		}


		try
		{
			Registry registryNext = LocateRegistry.getRegistry(IPnext);
			nextNode = (NodeInteractionInterface) registryNext.lookup(Node.NODE_INTERACTION_NAME);
			nextNode.setPreviousNeighbourRemote(prevID);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
			nextFailed = true;
		}

		// Handling Recursion
		if (prevFailed && nextFailed)
		{
			this.failure(prevID, nextID);
		}
		else if (prevFailed)
		{
			this.failure(prevID, lastID);
		}
		else if (nextFailed)
		{
			this.failure(firstID, nextID);
		}

		// No recursion occurred or we're the last level of recursion
		// first and last become eachothers neighbours
		try
		{
			if ((nextID == prevID) && (nextID == Node.getInstance().getId()))
			{
				try
				{
					Node.getInstance().getResolverStub().getIP(Node.getInstance().getId());
				}
				catch (RemoteException re)
				{
					System.err.println("[ERROR]\tEvery single node and the nameserver in the network failed, shutting down, please restart node manually");
					Node.getInstance().getLifeCycleManager().shutdown();
					re.printStackTrace();
				}

			}
			else
			{
				short tmpID = prevID;

				while (tmpID != nextID)
				{
					Node.getInstance().getLifeCycleManager().shutdown();
					tmpID = Node.getInstance().getResolverStub().getNext(tmpID);
				}

				prevNode.setNextNeighbourRemote(nextID);
				nextNode.setPreviousNeighbourRemote(prevID);
			}
		}
		catch (RemoteException re)
		{
			re.printStackTrace();
		}
		catch (NullPointerException npe)
		{
			String nextNodeStr = "";
			String prevNodeStr = "";

			if (nextNode == null)
			{
				nextNodeStr = "NULL";
			}
			else
			{
				nextNodeStr = nextNode.toString();
			}

			if (prevNode == null)
			{
				prevNodeStr = "NULL";
			}
			else
			{
				prevNodeStr = prevNode.toString();
			}


			System.err.println("[ERROR]\tNext (" + nextNodeStr + ") or Previous (" + prevNodeStr + ") stub was NULL when trying to set neighbours from FailureAgent");
			npe.printStackTrace();
		}
	}
}
