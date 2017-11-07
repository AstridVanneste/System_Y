package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.UDP.Multicast.*;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.Server;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;
import Util.Arrays;
import Util.Serializer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;


public class Node implements Runnable, NodeInteractionInterface
{
	private static final String NODE_INTERACTION_NAME = "NODE_INTERACTION";

	private String ip;	//?
	private String nsIp;
	private String name;
	private short previousNeighbour;
	private short nextNeighbour;
	private short id;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	private Client udpClient; //?
	private Server udpServer; //?
	private Random rand; //?
	private short numberOfNodes;
	private int startupTransactionId; //?
	private boolean newNode;
	private boolean accessRequestSent; //??


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
		this.rand = new Random();
		this.newNode = true;
		this.accessRequestSent = false;

		//???
		this.resolverInterface = resolverInterface;
		this.shutdownAgentInterface = shutdownAgentInterface;

		this.name = name;

		//ZELF OPVRAGEN
		this.ip = ip;

		// AANVRAGEN OP NAMESERVER (TIJDELIJKE WAARDE)
		this.id = -1;
		System.out.println("Mijn ID is:" + id);
		System.out.println("Debugging: de volgorde van prints moet zijn..");
		System.out.println("init done, registery created, make accessrequest, sent accessrequest, thread started, subscribed, ik ben met success toegevoegd aan het netwerk!");

		this.previousNeighbour = this.id; //??
		this.nextNeighbour = this.id; //??

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
		System.out.println("registry created");

		this.accessRequest();

		Thread thread = new Thread(this);
		thread.start();
		System.out.println("thread started");
	}

	/**
	 * Multicast for NS
	 * NS will process this message and has to confirm that the node may access the network
	 */
	public void accessRequest ()
	{
		if(!accessRequestSent) {
			System.out.println("Make accessrequest");
			this.udpClient = new Client();
			udpClient.start();

			byte version = (byte) 0;
			short replyCode = (short) 0;
			short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
			this.startupTransactionId = rand.nextInt();
			System.out.println(startupTransactionId);
			int dataLength = name.length() + 8;
			ProtocolHeader header = new ProtocolHeader(version, dataLength, startupTransactionId, requestCode, replyCode);

			byte[] data = new byte[name.length() + 8];
			byte[] nameLengthInByte = Serializer.intToBytes(name.length());
			byte[] nameInByte = name.getBytes();

			//HIER IS APARTE METHODE VOOR (ZIE SERIALIZER)
			byte[] ipInByte = new byte[4];
			String[] ipInParts = ip.split("\\.");                                                                    //https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
			ipInByte[0] = (byte) Integer.parseInt(ipInParts[0]);
			ipInByte[1] = (byte) Integer.parseInt(ipInParts[1]);
			ipInByte[2] = (byte) Integer.parseInt(ipInParts[2]);
			ipInByte[3] = (byte) Integer.parseInt(ipInParts[3]);

			System.arraycopy(nameLengthInByte, 0, data, 0, nameLengthInByte.length);
			System.arraycopy(nameInByte, 0, data, 4, nameInByte.length);
			System.arraycopy(ipInByte, 0, data, nameInByte.length + 4, ipInByte.length);

			Datagram datagram = new Datagram(header, data);
			System.out.println(datagram.getHeader().getTransactionID());
			udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize());
			udpClient.stop();
			System.out.println("sent accessrequest");
			accessRequestSent = true;
		}
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

			byte[] newNodeIDInBytes = new byte[2]; //SUGGESTIE: DATAGRAMMEN SUBKLASSEN
			newNodeIDInBytes[0] = data[0];
			newNodeIDInBytes[1] = data[1];
			short newNodeID = Serializer.bytesToShort(newNodeIDInBytes);
			short numberOfNodes = (short)(data[2] << 8 | data[3]); //SERIALIZER

			System.out.println("ontvangen ID: " + newNodeID);
			System.out.println("# nodes van NS: " + numberOfNodes);
			System.out.println("Kloppen de replycodes? " + request.getHeader().getReplyCode()+ " == " + ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);
			System.out.println("TransactionID " + this.startupTransactionId + " vergelijk met server.. " + request.getHeader().getTransactionID());
			System.out.println("Ben ik nieuw?" + newNode);

			if (!newNode)
			{
				System.out.println("Ik, geen nieuwe node, heb data ontvangen. ik doe rmi op nieuwe node");
				if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED)
				{
					changeNeighbours(newNodeID);
				}
			}
			if (newNode)
			{
				//check if message contains error message from NS
				if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_ID) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					System.out.println("ik, nieuwe node, moet naam veranderen");
					askNewName();
					accessRequest();
				}

				//check if successfully added to NS
				if ((request.getHeader().getReplyCode() == (int)ProtocolHeader.REPLY_SUCCESSFULLY_ADDED) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					//nameserver sends the amount of nodes in the tree
					System.out.println("ik ben met success toegevoegd aan netwerk! ben geen nieuwe node meer");
					setNumberOfNodes(numberOfNodes);
					newNode = false;
				}
			}
		}
		/*else{
			System.out.println("merde");
		}*/
	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 */
	private void changeNeighbours(short newID)
	{
		if((this.id < newID) && (newID < nextNeighbour))
		{
			this.nextNeighbour = newID;
			//NodeInteractionInterface.setPreviousNeighbour(this.newID);
			//NodeInteractionInterface.setNextNeighbour(newID);
		}
		if ((previousNeighbour < newID) && (newID < this.id))
		{
			this.previousNeighbour = newID;
		}

		this.numberOfNodes++; //KAN NOOIT KLOPPEN WANT VERWIJDERING GA JE NIET ZIEN (PARAMETER)
	}


	public void run()
	{
		System.out.println("subscribed");
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
		//System.out.println("out of multicastListener. FOUT!");
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

	/**
	 * Calculate the hashcode of the name
	 * @return ID
	 */
	@Deprecated //MOET WEG
	private static short getHash(String name)
	{
		return (short) Math.abs(name.hashCode() % 32768);
	}

	private void askNewName ()
	{
		System.out.println("Please enter a new name: ");
		Scanner scanner = new Scanner(System.in);
		this.name = scanner.nextLine();
		this.setId(getHash(this.name)); //NS
	}

	@Deprecated
	private void askNewIP () //IP ADDRESS GWN OPVRAGEN
	{
		System.out.println("Please enter a new IP address: ");
		Scanner scanner = new Scanner(System.in);
		String ip = scanner.nextLine();
		setIp(ip);
	}

	@Deprecated
	public String getIp()
	{
		return this.ip;
	}

	@Deprecated
	public void setIp(String ip)
	{
		this.ip = ip;
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

	public short getPreviousNeighbour()
	{
		return this.previousNeighbour;
	}

	public void setPreviousNeighbour(short previousNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
	}

	public short getNextNeighbour()
	{
		return this.nextNeighbour;
	}

	public void setNextNeighbour(short nextNeighbour)
	{
		this.nextNeighbour = nextNeighbour;
	}

	public short getId()
	{
		return this.id;
	}

	public void setId(short id)
	{
		this.id = id;
	}

	public Subscriber getSubscriber()
	{
		return subscriber;
	}

	public void setSubscriber(Subscriber subscriber)
	{
		this.subscriber = subscriber;
	}

	public Client getUdpClient()
	{
		return udpClient;
	}

	public void setUdpClient(Client udpClient)
	{
		this.udpClient = udpClient;
	}

	public Random getRand()
	{
		return rand;
	}

	public void setRand(Random rand)
	{
		this.rand = rand;
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
