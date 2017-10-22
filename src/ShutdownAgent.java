import Network.UDP.Unicast.Client;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Axel on 22/10/2017.
 */

/*
 * 1) Neighbours suspect that their node between them is dead
 * 2) One of neighbours sends a requestDeadNode-request to NameServer (NS)
 * 3) NS pings to dead node
 * 4a) If no answer: NS deletes node from Map
 * 4b) If answer: leave the case
 * 5) NS sends the both neighbours that they are now eachothers neighbour
 *      REMARK: how does NS knows the other neighbour?
 * 6) NS sends broadcast to erase all links with local documents of dead node
 */

public class ShutdownAgent extends UnicastRemoteObject implements ShutdownAgentInterface {


    InetAddress deadNodeIP;
    boolean statusNode;

    protected ShutdownAgent() throws RemoteException {

    }

    @Override
    public void requestDeadNode(InetAddress nodeIP) throws RemoteException {

        deadNodeIP = nodeIP;
        statusNode = pingNode(deadNodeIP);


        if (!statusNode)
            deleteNodeFromMap(deadNodeIP);


    }

    // Return true if node replies, false if not
    //      Not necessary in interface
    public boolean pingNode (InetAddress nodeIP) {

        // Send a pingRequest makes me client or server???
        new Client();

        return true;
    }

    // NS can delete a node from his Map
    //      Not necessary in interface
    public void deleteNodeFromMap (InetAddress nodeIP){

    }

    // Send new IP-addresses to neighbours of dead node
    // NS knows just one IP of the neighbours??
    //      Not necessary in interface
    public void sendNeighboursIP (InetAddress deadNodeIP){

    }

    // Sends a broadcast about the dead node. This allows all nodes
    //  to remove all links with the files of the dead node
    //      Not necessary in interface
    public void sendBroadCast (InetAddress deadNodeIP){

    }


}
