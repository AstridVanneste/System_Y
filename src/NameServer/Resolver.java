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
	public Resolver(){

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
	public void removeFromTree(int nodeId) throws RemoteException{
		if(NameServer.getInstance().map.containsKey(nodeId)){
			NameServer.getInstance().map.remove(nodeId);
		}
		else{
			throw new InvalidParameterException("No node with that ID");
		}
	}

	public void writeMap() throws RemoteException{
		String eol = System.getProperty("line.separator");

		try (Writer writer = new FileWriter("somefile.csv")) {
			for (Map.Entry<Short, String> entry : NameServer.getInstance().map.entrySet()) {
				writer.append((entry.getKey().toString())).append(',').append(entry.getValue()).append(eol);
			}
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
	}

	//@Override
	/*public String getOwnerIP(String filename) throws RemoteException	{

		int hash = NameServer.getHash(filename);

		System.out.println("HASH = " + hash);

		int ID = getOwnerID(hash);

		return NameServer.getInstance().map.get(NameServer.getInstance().map.floorKey(new Integer(ID)));
	}
	*/

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
