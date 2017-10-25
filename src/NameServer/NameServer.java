package NameServer;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeMap;

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
	    /*if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}*/
	    try {
            shutdownAgent = new ShutdownAgent();
            resolver = new Resolver();
			Registry registry = LocateRegistry.createRegistry(1098);
			registry.bind("SHUTDOWNAGENT", shutdownAgent);
			registry.bind("RESOLVER", resolver);
        }
        catch (Exception e)
	    {
            System.out.println(e.getMessage());
        }
	}

	/**
	 * Binds new RMI service to registry
	 */
/*
	public void bind()
	{
	    try
	    {

	    }
	    catch (Exception e)
	    {
            System.out.println(e.getMessage());
       }

	}
*/
	public static int getHash(String name)
	{

		return Math.abs(name.hashCode()%32768); //todo: CHECK IF FORMULA CORRECT!!!
	}


}
