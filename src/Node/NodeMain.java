package Node;

import NameServer.ShutdownAgent;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
        import java.rmi.registry.LocateRegistry;
        import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Created by Axel on 25/10/2017.
 */
public class NodeMain
{
    public static void main(String[] args)
    {
        Node.getInstance().start();

		Scanner scanner = new Scanner(System.in);

        while(true){

            System.out.println("press enter to see neighbours");
            if(scanner.nextLine().equals("p"))
			{
				break;
			}

            try
            {
                System.out.println(Node.getInstance().getNextNeighbour());
                System.out.println(Node.getInstance().getPreviousNeighbour());
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        try
		{
			Registry reg = LocateRegistry.getRegistry("10.0.0.3");
			NodeInteractionInterface stub = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);

		}
		catch (Exception e)
		{
			try
			{
				System.out.println("DOOOD");
				Node.getInstance().getFailureAgent().failure(Node.getInstance().getNextNeighbour());
				System.out.println("NEXT: " + Node.getInstance().getNextNeighbour());
				System.out.println("PREVIOUS: " + Node.getInstance().getPreviousNeighbour());
			}
			catch(RemoteException re)
			{
				re.printStackTrace();
			}
		}
		System.out.println("kleir");



    }
}
