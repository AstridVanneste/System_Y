package NameServer;

import java.rmi.RemoteException;
import java.security.InvalidParameterException;

/**
 * Created by Astrid on 22-10-2017.
 */
public class Resolver implements ResolverInterface
{
	public Resolver()
	{
	}

	@Override
	public String lookup(short nodeId) throws RemoteException, InvalidParameterException
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

	@Override
	public String lookup(String filename) throws RemoteException
	{
		/*
		short hash = ;

		System.out.println("HASH = " + hash);

		short ID = ;
		*/
		return NameServer.getInstance().map.get(NameServer.getInstance().map.lowerKey(NameServer.getHash(filename)));
	}

	@Deprecated
	public void addToTree(short ID, String IP) throws RemoteException
	{
		NameServer.getInstance().map.put(ID,IP);
	}
}
