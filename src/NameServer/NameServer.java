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

	private Resolver resolver;
	private Remote shutdownAgent;


	private NameServer()
	{
		this.map = new TreeMap<>();
        //this.resolver = new Resolver();

    }

	public static NameServer getNameServer()
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
	    try {
            Remote shutdownAgent = new ShutdownAgent();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
	}

	/**
	 * Binds new RMI service to registry
	 */
	public void bind(String name)
	{
	    try
		{

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("shutdownAgent", shutdownAgent);

	    } catch (Exception e)
		{

            System.out.println(e.getMessage());

        }

	}

	public static int getHash(String name)
	{

		return Math.abs(name.hashCode()%32768); //todo: CHECK IF FORMULA CORRECT!!!
	}


}
