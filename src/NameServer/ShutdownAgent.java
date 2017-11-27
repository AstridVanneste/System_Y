package NameServer;

import IO.Network.UDP.Unicast.*;
import Util.Serializer;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.*;

import static java.rmi.server.RemoteServer.getClientHost;

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
		Util.General.printLineSep();
		System.out.println("ShutdownAgent.requestShutdown(" + id + ")");
		try
		{
			System.out.println("Called by: " + getClientHost());
		}
		catch (ServerNotActiveException snae)
		{
			snae.printStackTrace();
		}
		System.out.println("BEFORE SHUTDOWN");
		System.out.println(NameServer.getInstance().toString());
		if(NameServer.getInstance().map.containsKey(id))
		{
			NameServer.getInstance().map.remove(id);
			NameServer.getInstance().writeMapToFile();
		}
		System.out.println("AFTER SHUTDOWN");
		System.out.println(NameServer.getInstance().toString());
    }
}
