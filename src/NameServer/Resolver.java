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
	public String lookup(int nodeId) throws RemoteException, InvalidParameterException
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

		int hash = NameServer.getHash(filename);


		System.out.println("HASH = " + hash);

		int ID = getOwnerID(hash);


		return NameServer.getInstance().map.get(ID);
	}

	public int getOwnerID(int hash)
	{
		int ID;
		boolean lower = true;

		ID = NameServer.getInstance().map.lowerKey(hash);

		return ID;
	}

	@Deprecated
	public void addToTree(int ID, String IP)
	{
		NameServer.getInstance().map.put(ID,IP);
	}
}
