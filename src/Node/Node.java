package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.UDP.Multicast.*;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.Server;
import NameServer.*;
//import NameServer.DiscoveryAgentInterface;
import Util.Arrays;
import Util.Serializer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;


public class Node implements Runnable, NodeInteractionInterface
{
	private static final String NODE_INTERACTION_NAME = "NODE_INTERACTION";

	private String nsIp;
	private String name;
	private short id;
	private short previousNeighbour;
	private short nextNeighbour;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	private NodeInteractionInterface nodeInteractionInterface;
	private short numberOfNodes;
	private int startupTransactionId;
	private boolean newNode;


	/**
	 * Initialize the new node with his RMI-applications, name, ip and ID
	 * @param name: name of new node
	 * @param ip: ip of new node
	 * @param resolverInterface
	 * @param shutdownAgentInterface
	 */
	public Node(String name, String ip, ResolverInterface resolverInterface, ShutdownAgentInterface shutdownAgentInterface)
	{
		this.numberOfNodes = 0;
		this.newNode = true;

		this.resolverInterface = resolverInterface;
		this.shutdownAgentInterface = shutdownAgentInterface;

		this.name = name;
		this.id = -1;

		System.out.println("init done");
	}

	public void start()
	{
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		try
		{
			subscribeOnMulticast();

			NodeInteractionInterface stub = (NodeInteractionInterface) UnicastRemoteObject.exportObject(this,0);
			Registry registry = LocateRegistry.createRegistry(1098);
			registry.rebind(Node.NODE_INTERACTION_NAME, stub);
		}
		catch(RemoteException re)
		{
			System.err.println("Exception when creating stub");
			re.printStackTrace();
		}

		this.accessRequest();

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Multicast for NS
	 * NS will process this message and has to confirm that the node may access the network
	 */
	public void accessRequest () {
		System.out.println("Make accessrequest");

		Client udpClient = new Client();
		udpClient.start();

		byte version = (byte) 0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		Random rand = new Random();
		this.startupTransactionId = rand.nextInt()%127;
		System.out.println(startupTransactionId);
		int dataLength = name.length() + 8;
		ProtocolHeader header = new ProtocolHeader(version, dataLength, startupTransactionId, requestCode, replyCode);

		byte[] data = new byte[name.length() + 8];
		byte[] nameLengthInByte = Serializer.intToBytes(name.length());
		byte[] nameInByte = name.getBytes();

		byte[] ipInByte = new byte[4];

		try {
			ipInByte = Serializer.ipStringToBytes(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		System.arraycopy(nameLengthInByte, 0, data, 0, nameLengthInByte.length);
		System.arraycopy(nameInByte, 0, data, 4, nameInByte.length);
		System.arraycopy(ipInByte, 0, data, nameInByte.length + 4, ipInByte.length);

		Datagram datagram = new Datagram(header, data);
		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize());
		udpClient.stop();

		System.out.println("sent accessrequest");
	}


	/**
	 * Old nodes and new nodes listen on multicast for the acceptance of a new node
	 * Old nodes will set their new neighbour (new node) with RMI
	 * New node will update his number of nodes and wait until his neighbours has called his rmi-methods
	 *
	 */
	public void multicastListener()
    {
		if(subscriber.hasData())
		{
			System.out.println("data received!");

			DatagramPacket packet = subscriber.receivePacket();
			nsIp = packet.getAddress().getHostAddress();

			Datagram request = new Datagram(packet.getData());
			byte[] data = request.getData();

			short newNodeID = Serializer.bytesToShort(new byte [] {data[0],data[1]});
			numberOfNodes = Serializer.bytesToShort(new byte[] {data[2],data[3]});

			if (!newNode)
			{
				if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED)
				{
					changeNeighbours(newNodeID);
					this.numberOfNodes = Serializer.bytesToShort(new byte[]{data[2],data[3]});
				}
			}
			if (newNode)
			{
				//check if message contains error message from NS
				if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_ID) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					askNewName();
					accessRequest();
				}

				//check if successfully added to NS
				if ((request.getHeader().getReplyCode() == (int)ProtocolHeader.REPLY_SUCCESSFULLY_ADDED) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{

					//nameserver sends the amount of nodes in the tree
					setNumberOfNodes(numberOfNodes);
					this.id = newNodeID;
					setNeighbours();
					newNode = false;
				}
			}
		}
	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 */
	private void changeNeighbours(short newID)
	{
		if((this.id < newID) && (newID < nextNeighbour))
		{
			this.nextNeighbour = newID;

			Registry reg = null;
			try
			{
				reg = LocateRegistry.getRegistry(resolverInterface.getIP(newID));
				Remote neighbourNode = reg.lookup(Node.NODE_INTERACTION_NAME);
				NodeInteractionInterface neighbourInterface = (NodeInteractionInterface) neighbourNode;

				neighbourInterface.setNextNeighbour(nextNeighbour);
				neighbourInterface.setPreviousNeighbour(id);

			} catch (RemoteException e)
			{
				e.printStackTrace();
			} catch (NotBoundException e)
			{
				e.printStackTrace();
			}
		}
		if ((previousNeighbour < newID) && (newID < this.id))
		{
			this.previousNeighbour = newID;
		}

	}


	public void run()
	{
		while(true)
		{
			multicastListener();
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Subscribe this node on the multicast-address 224.0.0.1
	 */
	public void subscribeOnMulticast ()
	{
		this.subscriber = new Subscriber(Constants.DISCOVERY_MULTICAST_IP,Constants.DISCOVERY_CLIENT_PORT);
		this.subscriber.start();
	}

	/**
	 * Unsubscribe this node on the multicast-address 224.0.0.1
	 */
	public void unsubscribeMulticast ()
	{
		this.subscriber.stop();
	}

	/**
	 * If this node has no neighbours
	 */
	private void setNeighbours()
	{
		this.previousNeighbour = this.id;
		this.nextNeighbour = this.id;
	}

	@Override
	public void setNextNeighbour (short id) throws RemoteException{
		nextNeighbour = id;
	}

	@Override
	public short getNextNeighbour () throws RemoteException{
		return  0;
	}

	@Override
	public void setPreviousNeighbour (short id) throws RemoteException{
		previousNeighbour = id;
	}

	@Override
	public short getPreviousNeighbour () throws RemoteException{
		return 0;
	}


	/**
	 * If this node has 1 neighbours
	 */
	private void setNeighbours(short neighbourId)
	{
		this.previousNeighbour = neighbourId;
		this.nextNeighbour = neighbourId;
	}



	/**
	 * If this node has 2 neighbours
	 */
	private void setNeighbours(short previousNeighbour, short nextNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
		this.nextNeighbour = nextNeighbour;
	}

	private void askNewName ()
	{
		System.out.println("Please enter a new name: ");
		Scanner scanner = new Scanner(System.in);
		this.name = scanner.nextLine();
		this.id = NameServer.getHash(name);
	}

	public String getName()
	{
		return this.name;
	}

	//MOET VEEL WEG
	public void setName(String name)
	{
		this.name = name;
	}

	public short getId()
	{
		return this.id;
	}

	public void setId(short id)
	{
		this.id = id;
	}

	public short getNumberOfNodes()
	{
		return numberOfNodes;
	}

	public void setNumberOfNodes(short numberOfNodes)
	{
		this.numberOfNodes = numberOfNodes;
	}

}
