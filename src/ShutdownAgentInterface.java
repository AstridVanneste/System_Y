/**
 * Created by Axel on 20/10/2017.
 */

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * 1) Neighbours suspect that there node between them is dead
 * 2) One of neighbour send a requestDeadNode-request to NameServer (NS)
 * 3) NS pings to dead node
 * 4a) If no answer: NS deletes node from Map
 * 4b) If answer: leave the case
 * 5) NS sends the both neighbours that they are now eachothers neighbour
 *      REMARK: how does NS knows the other neighbour?
 * 6) NS sends broadcast to erase all links with local documents of dead node
 */

public interface ShutdownAgentInterface extends Remote {
    // Are all these methods necessary in the interface??


    public void requestDeadNode (InetAddress nodeIP);

    // Return true if node replies, false if not
    public boolean pingNode (InetAddress nodeIP);

    // Return 1 if deleted, -1 if IP cannot be found in Map
    public int deleteNodeFromMap (InetAddress nodeIP);

    // Send new IP-addresses to neighbours of dead node
    public void sendNeighboursIP (InetAddress deadNodeIP);

    // Sends a broadcast about the dead node. This allows all nodes
    //  to remove all links with the files of the dead node
    public void sendBroadCast (InetAddress deadNodeIP);

}
