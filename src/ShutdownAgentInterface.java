/**
 * Created by Axel on 20/10/2017.
 */

import Network.UDP.Unicast.Server;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ShutdownAgentInterface extends Remote {

    // The node calls this method to do a request to the NS
    public void requestDeadNode (String nodeIP) throws RemoteException;
        // call pingNode


}
