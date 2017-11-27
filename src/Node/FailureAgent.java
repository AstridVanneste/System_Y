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
	private void failure(short firstID, short lastID)
	{
		System.out.println("failure(" + Short.toString(firstID) + "," + Short.toString(lastID) + ")");
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
			System.out.println("PREV: " + Short.toString(prevID));
			System.out.println("NEXT: " + Short.toString(nextID));
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		try
		{
			if(prevID != Node.getInstance().getId()) //No RMI to yourself
			{
				Registry registryPrev = LocateRegistry.getRegistry(IPprev);
				prevNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			}
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
			prevFailed = true;
		}


		try
		{
			if(nextID != Node.getInstance().getId()) //No RMI to yourself
			{
				Registry registryNext = LocateRegistry.getRegistry(IPnext);
				nextNode = (NodeInteractionInterface) registryNext.lookup(Node.NODE_INTERACTION_NAME);
			}
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
			System.out.println("Next and previous failed");
			this.failure(prevID, nextID);
		}
		else if (prevFailed)
		{
			System.out.println("Previous failed");
			this.failure(prevID, lastID);
		}
		else if (nextFailed)
		{
			System.out.println("Next failed");
			this.failure(firstID, nextID);
		}

		System.out.println("No Recursion");

		// No recursion occurred or we're the last level of recursion
		// first and last become eachothers neighbours
		
		try
		{
			if ((nextID == prevID) && (nextID == Node.getInstance().getId()))
			{
				try
				{
					Node.getInstance().getResolverStub().getIP(Node.getInstance().getId());
					Node.getInstance().getLifeCycleManager().getShutdownStub().requestShutdown(Node.getInstance().getResolverStub().getNext(prevID));
					Node.getInstance().setNextNeighbour(Node.getInstance().getId());
					Node.getInstance().setPreviousNeighbour(Node.getInstance().getId());
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
				short tmpID = Node.getInstance().getResolverStub().getNext(prevID);

				while (tmpID != nextID)
				{
					Util.General.printLineSep();
					System.out.println("Tmp: " + Short.toString(tmpID));
					System.out.println("PrevID: " + Short.toString(prevID));
					System.out.println("NextID: " + Short.toString(nextID));
					Util.General.printLineSep();
					Node.getInstance().getLifeCycleManager().getShutdownStub().requestShutdown(tmpID);
					tmpID = Node.getInstance().getResolverStub().getNext(tmpID);

					//Node.getInstance().getFailureAgent().failure(tmp);
				}

				if(prevNode != null)
				{
					prevNode.setNextNeighbourRemote(nextID);
				}
				else
				{
					Node.getInstance().setNextNeighbour(nextID);
				}

				if(nextNode != null)
				{
					nextNode.setPreviousNeighbourRemote(prevID);
				}
				else
				{
					Node.getInstance().setPreviousNeighbour(prevID);
				}

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

	public void failure(short ID)
	{
		this.failure(ID,ID);
	}
}
