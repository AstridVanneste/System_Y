package Node;

import IO.Network.UDP.Unicast.UDPClient;
import NameServer.NameServer;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
        import java.rmi.registry.LocateRegistry;
        import java.rmi.registry.Registry;

/**
 * Created by Axel on 25/10/2017.
 */
public class NodeMain {
    public static void main(String[] args) {

        if(System.getSecurityManager()==null)
        {
            System.setSecurityManager(new SecurityManager());
        }
        try
        {
            Registry reg = LocateRegistry.getRegistry(1099);
            Remote resolver = reg.lookup(NameServer.RESOLVER_NAME);
            Remote shutdownAgent = reg.lookup((NameServer.SHUTDOWN_AGENT_NAME));
            ResolverInterface resolverInterface = (ResolverInterface) resolver;
            ShutdownAgentInterface shutdownAgentInterface = (ShutdownAgentInterface) shutdownAgent;

            Node node = new Node ("axel",resolverInterface,shutdownAgentInterface);

            System.out.println("Access request...");
            node.accessRequest();
            System.out.println("Access successful");
            node.start();
        }
        catch (RemoteException re)
        {
            System.out.println("RemoteException in LocateRegistry.getRegistry()");
            re.printStackTrace();
        }
        catch (NotBoundException nbe)
        {
            System.out.println("NotBoundException in getIP()");
            nbe.printStackTrace();
        }


    }
}
