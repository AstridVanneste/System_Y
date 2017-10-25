package NameServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidParameterException;

/**
 * Created by Astrid on 22-10-2017.
 */
public class Resolver extends UnicastRemoteObject implements ResolverInterface
{
	private NameServer nameServer;

	protected Resolver() throws RemoteException
	{
		this.nameServer = NameServer.getNameServer();
	}


	@Override
	public String lookup(int nodeId) throws RemoteException, InvalidParameterException
	{
		if(this.nameServer.map.containsKey(nodeId))
		{
			return this.nameServer.map.get(nodeId);
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


		return this.nameServer.map.get(ID);
	}

	public int getOwnerID(int hash)
	{
		int ID;
		boolean lower = true;

		ID = nameServer.map.lowerKey(hash);



		return ID;
	}

	@Deprecated
	public void addToTree(int ID, String IP)
	{
		this.nameServer.map.put(ID,IP);
	}
}
