package NameServer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.Map;

/**
 * Created by Astrid on 22-10-2017.
 */
public class Resolver implements ResolverInterface
{
	public Resolver()
	{
	}


	@Override
	public String getIP(int nodeId) throws RemoteException, InvalidParameterException
	{
		if(NameServer.getInstance().map.containsKey(nodeId))
		{
			return NameServer.getInstance().map.get(nodeId);
		}
		else
		{
			throw new InvalidParameterException("No node with that ID");
		}
	}

	@Deprecated
	public void removeFromTree(int nodeId) throws RemoteException
	{
		if(NameServer.getInstance().map.containsKey(nodeId))
		{
			NameServer.getInstance().map.remove(nodeId);
		}
		else
		{
			throw new InvalidParameterException("No node with that ID");
		}
	}

	@Override
	public String getOwnerIP(String filename)
	{
		short hash = NameServer.getHash(filename);

		if(hash < NameServer.getInstance().map.firstKey())
		{
			return NameServer.getInstance().map.get(NameServer.getInstance().map.lastKey());
		}
		else
		{
			return NameServer.getInstance().map.get(NameServer.getInstance().map.lowerKey(hash));
		}
	}


	@Deprecated
	public void addToTree(short ID, String IP) throws RemoteException
	{
		NameServer.getInstance().map.put(ID,IP);
	}
}
