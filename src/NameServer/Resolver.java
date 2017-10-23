package NameServer;

import java.rmi.RemoteException;

/**
 * Created by Astrid on 22-10-2017.
 */
public class Resolver implements ResolverInterface
{

	private NameServer nameServer;

	public Resolver(NameServer nameServer)
	{
		this.nameServer = nameServer;
	}

	public void init()
	{
		this.nameServer.bind("RESOLVER");
	}

	@Override
	public String lookup(int nodeId) throws RemoteException
	{
		return nameServer.lookup();
	}

	@Override
	public String lookup(String filename) throws RemoteException
	{
		int hash = filename.hashCode()%32768; //todo: CHECK IF FORMULA CORRECT!!!

		return null;
	}
}
