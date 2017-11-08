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

	@Override
	public short getPrevious(short ID)
	{
		if(NameServer.getInstance().map.firstKey() > ID)
		{
			return NameServer.getInstance().map.lastKey();
		}
		else
		{
			return NameServer.getInstance().map.lowerKey(ID);
		}
	}

	@Override
	public short getNext(short ID)
	{
		if(NameServer.getInstance().map.lastKey() < ID)
		{
			return NameServer.getInstance().map.firstKey();
		}
		else
		{
			return NameServer.getInstance().map.lowerKey(ID);
		}
	}


	@Deprecated
	public void addToTree(short ID, String IP) throws RemoteException
	{
		NameServer.getInstance().map.put(ID,IP);
	}

	public short lastKey() throws RemoteException{
		return NameServer.getInstance().map.lastKey();
	}

	public short firstKey() throws RemoteException{
		return NameServer.getInstance().map.firstKey();
	}


}
