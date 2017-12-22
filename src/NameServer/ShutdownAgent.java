package NameServer;

import IO.Network.UDP.Unicast.*;
import Node.Node;
import Node.NodeInteractionInterface;
import Util.Serializer;
import Util.General;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;

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
		General.printLineSep();
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

		if(id == NameServer.getInstance().getRingMonitorId())
		{
			if(NameServer.getInstance().map.size() >= 1)
			{
				try
				{
					short ringMonitorId = NameServer.getInstance().getResolver().getPrevious(id);
					NameServer.getInstance().setRingMonitorId(ringMonitorId);

					Registry reg = LocateRegistry.getRegistry(NameServer.getInstance().getResolver().getIP(ringMonitorId));
					NodeInteractionInterface node = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
					node.runRingMonitor();
					General.printLineSep();
					System.out.println("Ring Monitor changes to: " + ringMonitorId);
					General.printLineSep();
				} catch (RemoteException re)
				{
					re.printStackTrace();
				} catch (NotBoundException nbe)
				{
					nbe.printStackTrace();
				}
			}
		}
    }
}
