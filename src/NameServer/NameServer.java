package NameServer;

import IO.File;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeMap;

/**
 * Created by Astrid on 20-Oct-17.
 */
public class NameServer
{
	public static final String SHUTDOWN_AGENT_NAME = "SHUTDOWN_INTERFACE";
	public static final String RESOLVER_NAME = "RESOLVER_INTERFACE";
	public static final String MAP_FILE_NAME = "TreeMap.csv";



	private static NameServer nameServer;	//singleton instance of nameserver

	TreeMap<Integer,String> map;			//can be accessed throughout entire NameServer package
	private ShutdownAgentInterface shutdownAgentStub;
	public ResolverInterface resolverStub;  // todo: temporary, move back to private when RMI testing is complete
	private DiscoveryAgent discoveryAgent;

	private NameServer()
	{
		this.map = new TreeMap<>();
		this.discoveryAgent = new DiscoveryAgent();
    }

	public static NameServer getInstance()
	{
		if(nameServer == null)
		{
			nameServer = new NameServer();
		}
		return nameServer;
	}

	/**
	 * Initializes RMI
	 */
	public void init()
	{
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}

	    try
	    {
	    	this.shutdownAgentStub = (ShutdownAgentInterface) UnicastRemoteObject.exportObject(new ShutdownAgent(), 0);
		    this.resolverStub = (ResolverInterface) UnicastRemoteObject.exportObject(new Resolver(), 0);
            this.bind();
        }
        catch (RemoteException re)
	    {
	    	System.out.println("Exception in init()");
	    	re.printStackTrace();
        }
	}

	/**
	 * Binds new RMI service to registry
	 */
	public void bind()
	{
	    try
	    {
            Registry registry = LocateRegistry.createRegistry(1099);
		    registry.bind(SHUTDOWN_AGENT_NAME, this.shutdownAgentStub);
			registry.bind(RESOLVER_NAME, this.resolverStub);
	    }
	    catch (AlreadyBoundException abe)
	    {
	    	System.out.println("AlreadyBoundException in bind()");
	    	abe.printStackTrace();
	    }
	    catch (RemoteException re)
	    {
	    	System.out.println("RemoteException in  bind()");
            re.printStackTrace();
       }

	}

	public static int getHash(String name)
	{
		return Math.abs(name.hashCode() % 32768); //todo: CHECK IF FORMULA CORRECT!!!
	}

	public void writeMapToFile()
	{
		File file = new File(MAP_FILE_NAME);

		try
		{
			file.write(File.mapToCSV(this.map).getBytes());
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void readMapFromFile()
	{
		File file = new File(MAP_FILE_NAME);

		try
		{
			byte[] bytes = file.read();
			String CSV = new String(bytes);
			this.map = File.CSVToMap(CSV);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();;
		}

	}

	public String toString()
	{
		String s = "";

		for(int n: this.map.keySet())
		{
			s += "KEY:	" + n + "	VALUE:	" + this.map.get(n) + "\n";
		}

		return s;
	}

	@Deprecated
	public void addToTree(int ID, String IP)
	{
		this.map.put(ID,IP);
	}

	@Deprecated
	public void removeFromTree(int ID)
	{
		this.map.remove(ID);
	}

}
