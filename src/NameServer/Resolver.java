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
	public String getIP(short nodeId) throws RemoteException, InvalidParameterException
	{
		Util.General.printLineSep();

		if(NameServer.getInstance().map.containsKey(nodeId))
		{
			Util.General.printLineSep();
			String IP=  NameServer.getInstance().map.get(nodeId);
			System.out.println("Resolver.getIP(" + Short.toString(nodeId) + ")" + " = " + IP);
			return IP;
		}
		else
		{
			throw new InvalidParameterException("No node with ID " + nodeId);
		}
	}

	@Deprecated
	public void removeFromTree(short nodeId) throws RemoteException
	{
		Util.General.printLineSep();
		System.out.println("Resolver.removeFromTree(" + Short.toString(nodeId) + ")");
		if(NameServer.getInstance().map.containsKey(nodeId))
		{
			NameServer.getInstance().map.remove(nodeId);
			Util.General.printLineSep();
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
			Util.General.printLineSep();
			id = NameServer.getInstance().map.lastKey();
		}
		else
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.lowerKey(hash);
		}

		Util.General.printLineSep();
		System.out.println("Resolver.getOwnerIP(" + filename + ") = " + id);

		return id;
	}

	@Override
	public short getPrevious(short ID) throws RemoteException
	{

		short id;

		if(NameServer.getInstance().map.firstKey().compareTo(ID)>0 || NameServer.getInstance().map.firstKey().equals(ID))
		{
			Util.General.printLineSep();
			id = NameServer.getInstance().map.lastKey();
		}
		else
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.lowerKey(ID);
		}

		Util.General.printLineSep();
		System.out.println("Resolver.getPrevious(" + Short.toString(ID) + ") = " + id);

		return id;
	}

	@Override
	public short getNext(short ID) throws RemoteException
	{
		short id;

		if(NameServer.getInstance().map.lastKey().compareTo(ID) < 0 ||  NameServer.getInstance().map.lastKey().equals( ID))
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.firstKey();
		}
		else
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.higherKey(ID);
		}

		Util.General.printLineSep();
		System.out.println("Resolver.getNext(" + Short.toString(ID) + ") = " + id);

		return id;
	}


	@Deprecated
	public void addToTree(short ID, String IP) throws RemoteException
	{
		Util.General.printLineSep();
		System.out.println("Resolver.addToTree(" + Short.toString(ID) + "," + IP + ")");
		NameServer.getInstance().map.put(ID,IP);
		Util.General.printLineSep();
	}
}
