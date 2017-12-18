package NameServer;


import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.security.InvalidParameterException;

import static java.rmi.server.RemoteServer.getClientHost;


/**
 * Created by Astrid on 22-10-2017.
 */
public class Resolver implements ResolverInterface
{
	public Resolver()
	{
	}

	@Override
	public String getIP(short nodeId) throws RemoteException, InvalidParameterException
	{
		if(NameServer.getInstance().map.containsKey(nodeId))
		{
			String IP=  NameServer.getInstance().map.get(nodeId);

			return IP;
		}
		else
		{
			throw new InvalidParameterException("No node with ID " + nodeId);
		}
	}

	@Override
	public short getOwnerID(String filename) throws RemoteException
	{

		short hash = NameServer.getHash(filename);
		short id;

		if(hash < NameServer.getInstance().map.firstKey())
		{
			id = NameServer.getInstance().map.lastKey();
		}
		else if(NameServer.getInstance().map.containsKey(hash))
		{
			return hash;
		}
		else
		{
			id =  NameServer.getInstance().map.lowerKey(hash);
		}

		return id;
	}

	@Override
	public short getPrevious(short ID) throws RemoteException
	{
		short id;

		if(NameServer.getInstance().map.firstKey().compareTo(ID)>0 || NameServer.getInstance().map.firstKey().equals(ID))
		{
			id = NameServer.getInstance().map.lastKey();
		}
		else
		{
			id =  NameServer.getInstance().map.lowerKey(ID);
		}

		return id;
	}

	@Override
	public short getNext(short ID) throws RemoteException
	{
		short id;

		if(NameServer.getInstance().map.lastKey().compareTo(ID) < 0 ||  NameServer.getInstance().map.lastKey().equals(ID))
		{
			id =  NameServer.getInstance().map.firstKey();
		}
		else
		{
			id =  NameServer.getInstance().map.higherKey(ID);
		}

		return id;
	}

	@Override
	public short getHash(String name) throws RemoteException
	{
		return NameServer.getHash(name);
	}
}
