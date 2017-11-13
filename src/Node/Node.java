package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.UDP.Multicast.*;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.Server;
import NameServer.*;
import Util.Arrays;
import NameServer.ShutdownAgent;
import NameServer.ShutdownAgentInterface;
import Util.Serializer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;


public class Node implements Runnable
{
	public static final String NODE_INTERACTION_NAME = "NODE_INTERACTION";

	private LifeCycleManager lifeCycleManager;
	private FailureAgent failureAgent;



	private static Node instance;


	public Node()
	{
		this.lifeCycleManager = new LifeCycleManager();
		this.failureAgent = new FailureAgent();

	}

	public static Node getInstance()
	{
		if(Node.instance == null)
		{
			Node.instance = new Node();
			Node.instance.start();
		}
		return Node.instance;
	}

	public void start()
	{
		System.out.println("give me a name");
		Scanner scanner = new Scanner(System.in);
		lifeCycleManager.setName(scanner.nextLine());
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		try
		{
			lifeCycleManager.subscribeOnMulticast();

			NodeInteractionInterface stub = (NodeInteractionInterface) UnicastRemoteObject.exportObject(lifeCycleManager,0);
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind(Node.NODE_INTERACTION_NAME, stub);

		}
		catch(RemoteException re)
		{
			System.err.println("Exception when creating stub");
			re.printStackTrace();
		} catch (AlreadyBoundException e)
		{
			e.printStackTrace();
		}

		lifeCycleManager.accessRequest();

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Multicast for NS
	 * NS will process this message and has to confirm that the node may access the network
	 */


	public void run()
	{
		while(true)
		{
			lifeCycleManager.multicastListener();
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}


	public LifeCycleManager getLifeCycleManager()
	{
		return this.lifeCycleManager;
	}

	public FailureAgent getFailureAgent()
	{
		return this.failureAgent;
	}


}