package NameServer;

import Network.UDP.Unicast.Server;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Astrid on 20-Oct-17.
 */
public class NameServer
{
	private static NameServer nameServer;	//singleton instance of nameserver

	TreeMap<Integer,String> map;			//can be accessed throughout entire NameServer package
	private Remote shutdownAgent;
	private Remote resolver;
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
	    try
	    {
            this.shutdownAgent = new ShutdownAgent();
            this.resolver = new Resolver();
            this.bind();
        }
        catch (Exception e)
	    {
            System.out.println(e.getMessage());
        }
	}

	/**
	 * Binds new RMI service to registry
	 */
	public void bind()
	{
	    try
	    {
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("shutdownAgent", shutdownAgent);
			registry.rebind("RESOLVER", resolver);
	    }
	    catch (Exception e)
	    {
            System.out.println(e.getMessage());
        }
	}
}
