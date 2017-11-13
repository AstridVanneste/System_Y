package NameServer;

import IO.Network.UDP.Unicast.*;
import Util.Serializer;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
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

public class ShutdownAgent implements ShutdownAgentInterface
{
    public ShutdownAgent()
    {

    }

	/**
	 * ping dead node
	 * @param id
	 * @return
	 */
	@Deprecated
    public static boolean pingNode (short id)		//WERKT NIET
	{
        try
		{
            //ping the client
            if(InetAddress.getByName(NameServer.getInstance().map.get(id)).isReachable(1000))
            {
                return true;
            }
        }
        catch (IOException e)
		{
            e.printStackTrace();
        }
        return false;
    }

	/**
	 * delete node from map and write to file (node has to exist)
	 * @param id
	 */
	public void requestShutdown(short id)
    {
    	if(NameServer.getInstance().map.containsKey(id))
		{
			NameServer.getInstance().map.remove(id);
			NameServer.getInstance().writeMapToFile();
		}
    }
}
