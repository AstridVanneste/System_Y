package NameServer;

import Network.UDP.Unicast.*;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

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

public class ShutdownAgent implements ShutdownAgentInterface {


    private boolean statusNode;

    public ShutdownAgent()
    {
    	this.statusNode = false;
    }

    public void failureListener()
    {

    }

    @Override
    public void requestDeadNode(int id) throws RemoteException {

        statusNode = pingNode(id);

        if (!statusNode)
            deleteNodeFromMap(id);
            //send to neighbours
    }

    @Override
    public void requestShutdown(int id) throws RemoteException{
        deleteNodeFromMap(id);
    }

    // Return true if node replies, false if not
    //      Not necessary in interface
    public boolean pingNode (int id) {
        try {
            //ping the client
            if(InetAddress.getByName(NameServer.getInstance().map.get(id)).isReachable(1000)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // NS can delete a node from his Map
    //      Not necessary in interface
    public void deleteNodeFromMap (int id){
        NameServer.getInstance().map.remove(id);
    }

    // Send new IP-addresses to neighbours of dead node
    // NS knows just one IP of the neighbours??
    //      Not necessary in interface
    public void sendNeighboursIP (int id, Server server){

        int ID;
        int IDRight;
        int IDLeft;
        boolean found = false;

        Set set =  NameServer.getInstance().map.keySet();
        int size = set.size();
        ArrayList<Integer> list = new ArrayList<>(size);

        for (Object setElement : set)
            list.add((int)setElement);

        ListIterator listIterator = list.listIterator();

        while (!found) {
            if (listIterator.hasNext()) {

                ID = (int) listIterator.next();

                if (ID == id) {
                    found = true;

                    if (listIterator.nextIndex() == size)
                        IDRight = list.get(0);
                    else
                        IDRight = list.get(listIterator.nextIndex());

                    if (listIterator.previousIndex() == -1)
                        IDLeft = list.get(size-1);
                    else
                        IDLeft = list.get(listIterator.previousIndex());
                }

            }
        }
        System.out.println("new neighbours");
        //needs to be send to packagging (datagram pakket)
    }

    // Sends a broadcast about the dead node. This allows all nodes
    //  to remove all links with the files of the dead node
    //      Not necessary in interface
    public void sendBroadCast (int id){

    }
    
}
