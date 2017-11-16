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
