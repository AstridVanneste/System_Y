package NameServer;

import java.util.TreeMap;

/**
 * Created by Astrid on 20-Oct-17.
 */
public class NameServer
{
	private static NameServer nameServer;	//singleton instance of nameserver

	TreeMap<Integer,String> map;			//can be accessed throughout entire NameServer package


	private NameServer()
	{
		this.map = new TreeMap<>();
	}

	public static NameServer getNameServer()
	{
		if(nameServer == null)
		{
			nameServer = new NameServer();
		}
		return nameServer;
	}

}
