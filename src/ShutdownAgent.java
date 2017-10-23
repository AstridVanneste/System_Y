import Network.UDP.Unicast.*;

import java.io.IOException;
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


    String deadNodeIP;
    boolean statusNode;

    protected ShutdownAgent() throws RemoteException {
    }

    public void failureListener(){

    }


    public void requestDeadNode(String nodeIP) throws RemoteException {

        deadNodeIP = nodeIP;
        statusNode = pingNode(deadNodeIP);


        if (!statusNode)
            deleteNodeFromMap(deadNodeIP);
            //send to neighbours
    }

    // Return true if node replies, false if not
    //      Not necessary in interface
    public boolean pingNode (String nodeIP) {
        try {
            //ping the client
            if(InetAddress.getByName(nodeIP).isReachable(3000)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // NS can delete a node from his Map
    //      Not necessary in interface
    public void deleteNodeFromMap (String deadNodeIP){

    }

    // Send new IP-addresses to neighbours of dead node
    // NS knows just one IP of the neighbours??
    //      Not necessary in interface
    public void sendNeighboursIP (InetAddress deadNodeIP, Server server){
        //Node neighbour1 = node.getLeftNeighbour();
        //Node neighbour2 = node.getRightNeighbour();
        //server.send(ipaddr neighbour 2) <= neigbour needs to be updated, left neighbour from deleted node has new right neigbhour = right neighbour dleted node
        server.send("",2000,"new left neighbour"); //ip's of neighbour??
        server.send("",2000,"new left neighbour");
    }

    // Sends a broadcast about the dead node. This allows all nodes
    //  to remove all links with the files of the dead node
    //      Not necessary in interface
    public void sendBroadCast (InetAddress deadNodeIP){

    }


}
