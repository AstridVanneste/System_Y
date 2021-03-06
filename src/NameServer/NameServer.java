package NameServer;

import IO.File;
import Node.Node;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
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
	public static final String MAP_FILE_NAME = "TreeMap.csv"; //todo: treemap file leegmaken bij start programma

	private static NameServer instance;	//singleton instance of nameserver

	TreeMap<Short,String> map;			//can be accessed throughout entire NameServer package
	private ShutdownAgent shutdownAgent;
	private Resolver resolver;
	private DiscoveryAgent discoveryAgent;
	private ShutdownAgentInterface shutdownAgentStub;
	private ResolverInterface resolverStub;

	private short ringMonitorId;

	private NameServer()
	{
		this.map = new TreeMap<>();
		this.shutdownAgent = new ShutdownAgent();
		this.resolver = new Resolver();
		this.discoveryAgent = new DiscoveryAgent();
		this.ringMonitorId = Node.DEFAULT_ID;
    }

	public static NameServer getInstance()
	{
		if(instance == null)
		{
			instance = new NameServer();
		}
		return instance;
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
	    	this.shutdownAgentStub = (ShutdownAgentInterface) UnicastRemoteObject.exportObject(this.shutdownAgent, 0);
		    this.resolverStub = (ResolverInterface) UnicastRemoteObject.exportObject(this.resolver, 0);
		    this.discoveryAgent.init();
            this.bind();
        }
        catch (RemoteException re)
	    {
	    	System.err.println("Exception in start()");
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
	    	System.err.println("AlreadyBoundException in bind()");
	    	abe.printStackTrace();
	    }
	    catch (RemoteException re)
	    {
	    	System.err.println("RemoteException in  bind()");
            re.printStackTrace();
       }
	}

	public static short getHash(String name)
	{
		return (short) Math.abs(name.hashCode() % 32768);
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
		StringBuilder s = new StringBuilder();

		for(short n: this.map.keySet())
		{
			s.append("KEY:	").append(n).append("	VALUE:	").append(this.map.get(n)).append("\n");
		}

		return s.toString();
	}

	public void stop()
	{
		discoveryAgent.stop();
		try
		{
			Registry reg = LocateRegistry.getRegistry();
			reg.unbind(RESOLVER_NAME);
			reg.unbind(SHUTDOWN_AGENT_NAME);

		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
		}
	}

	public void setRingMonitorId(short id)
	{
		this.ringMonitorId = id;
	}

	public short getRingMonitorId()
	{
		return this.ringMonitorId;
	}

	public Resolver getResolver()
	{
		return this.resolver;
	}
}
