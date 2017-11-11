package Node;

import NameServer.NameServer;
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
            scanner.nextLine();

            try
            {
                System.out.println(Node.getInstance().getNextNeighbour());
                System.out.println(Node.getInstance().getPreviousNeighbour());
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }

        }
    }
}
