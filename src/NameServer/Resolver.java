package NameServer;


import javax.print.attribute.standard.MediaSize;
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
	public String getIP(short nodeId) throws RemoteException, InvalidParameterException
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
	public void removeFromTree(short nodeId) throws RemoteException
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
		System.out.println("FIRST: " + NameServer.getInstance().map.firstKey());
		System.out.println("ID: " + ID);
		//if(NameServer.getInstance().map.firstKey() >= ID)
		if(NameServer.getInstance().map.firstKey().compareTo(ID)>0 || NameServer.getInstance().map.firstKey().equals(ID))
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
		System.out.println("LAST: " + NameServer.getInstance().map.lastKey());
		System.out.println("ID: " + ID);
		System.out.println("EQUALS: " + Boolean.toString((NameServer.getInstance().map.lastKey().equals(ID))));
		if(NameServer.getInstance().map.lastKey().compareTo(ID) < 0 ||  NameServer.getInstance().map.lastKey().equals( ID))
		{
			return NameServer.getInstance().map.firstKey();
		}
		else
		{
			return NameServer.getInstance().map.higherKey(ID);
		}
	}


	@Deprecated
	public void addToTree(short ID, String IP) throws RemoteException
	{
		NameServer.getInstance().map.put(ID,IP);
	}

}
