package NameServer;

import java.util.TreeMap;

/**
 * Created by Astrid on 20-Oct-17.
 */
public class NameServer
{
	private static NameServer nameServer;	//singleton instance of nameserver

	TreeMap<Integer,String> map;			//can be accessed throughout entire NameServer package

	private Resolver resolver;


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

	}

	/**
	 * Binds new RMI service to registry
	 */
	public void bind(String name)
	{

	}


}
