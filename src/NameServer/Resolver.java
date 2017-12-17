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
			//Util.General.printLineSep();
			String IP=  NameServer.getInstance().map.get(nodeId);
			//System.out.println("Resolver.getIP(" + Short.toString(nodeId) + ")" + " = " + IP);
			try
			{
				System.out.println("Called by: " + getClientHost());
			}
			catch (ServerNotActiveException snae)
			{
				snae.printStackTrace();
			}
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
			//Util.General.printLineSep();
			id = NameServer.getInstance().map.lastKey();
		}
		else if(NameServer.getInstance().map.containsKey(hash))
		{
			return hash;
		}
		else
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.lowerKey(hash);
		}

		//Util.General.printLineSep();
		//System.out.println("Resolver.getOwnerID(" + filename + ") = " + id);
		//System.out.println("Hash of file = " + hash);
		/*try
		{
			System.out.println("Called by: " + getClientHost());
		}
		catch (ServerNotActiveException snae)
		{
			snae.printStackTrace();
		}*/

		return id;
	}

	@Override
	public short getPrevious(short ID) throws RemoteException
	{
		short id;

		//System.out.println("RESOLVER: ID: " + ID + " FIRST: " + NameServer.getInstance().map.firstKey());
		//System.out.println("EQUALS = " + NameServer.getInstance().map.firstKey().equals(ID));

		//System.out.println(NameServer.getInstance().toString());

		if(NameServer.getInstance().map.firstKey().compareTo(ID)>0 || NameServer.getInstance().map.firstKey().equals(ID))
		{
			//Util.General.printLineSep();
			id = NameServer.getInstance().map.lastKey();
		}
		else
		{
			//Util.General.printLineSep();
			id =  NameServer.getInstance().map.lowerKey(ID);
		}

		//Util.General.printLineSep();
		//System.out.println("Resolver.getPrevious(" + Short.toString(ID) + ") = " + id);
		/*
		try
		{
			System.out.println("Called by: " + getClientHost());
		}
		catch (ServerNotActiveException snae)
		{
			snae.printStackTrace();
		}
		*/

		return id;
	}

	@Override
	public short getNext(short ID) throws RemoteException
	{
		short id;

		if(NameServer.getInstance().map.lastKey().compareTo(ID) < 0 ||  NameServer.getInstance().map.lastKey().equals(ID))
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.firstKey();
		}
		else
		{
			Util.General.printLineSep();
			id =  NameServer.getInstance().map.higherKey(ID);
		}

		//Util.General.printLineSep();
		//System.out.println("Resolver.getNext(" + Short.toString(ID) + ") = " + id);
		/*try
		{
			System.out.println("Called by: " + getClientHost());
		}
		catch (ServerNotActiveException snae)
		{
			snae.printStackTrace();
		}*/

		return id;
	}

	@Override
	public short getHash(String name) throws RemoteException
	{
		return NameServer.getHash(name);
	}
}
