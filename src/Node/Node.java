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
import java.util.Random;
import java.util.Scanner;


public class Node implements Runnable
{
	private String ip;
	private String name;
	private int previousNeighbour;
	private int nextNeighbour;
	private short id;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	//private DiscoveryAgentInterface discoveryAgentInterface;
	private Client udpClient;
	private Server udpServer;
	private Random rand;
	private short numberOfNodes;
	private int startupTransactionId;

	/**
	 * Initialize the new node with his RMI-applications, name, ip and ID
	 */
	public Node(String name, String ip, ResolverInterface resolverInterface, ShutdownAgentInterface shutdownAgentInterface)
	{
		this.numberOfNodes = 0;
		this.rand = new Random();

		this.udpServer = new Server(Constants.UDP_NODE_PORT);

		this.resolverInterface=resolverInterface;
		this.shutdownAgentInterface=shutdownAgentInterface;

		this.name = name;

		this.ip = ip;
		System.out.println(ip);
		this.id = getHash(name);

		this.previousNeighbour = this.id;
		this.nextNeighbour = this.id;
	}

	public void start()
	{
		udpServer.start();
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Multicast for NS
	 * NS will process this message
     * NS has to confirm that the node may access the network before his feature neighbours changes their neighbours
	 */
	public void accessRequest ()
	{
        this.udpClient = new Client();
		udpClient.start();

		byte version = (byte)0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		this.startupTransactionId = rand.nextInt();
		int dataLength = name.length() + 8;
		ProtocolHeader header = new ProtocolHeader(version,dataLength,startupTransactionId,requestCode,replyCode);

		byte [] data = new byte[name.length() + 8];
		byte [] nameLengthInByte = Serializer.intToBytes(name.length());
		byte [] nameLengthInByte2 = Arrays.reverse(nameLengthInByte);
		byte [] nameInByte = name.getBytes();

		byte[] ipInByte = new byte[4];
		String[] ipInParts = ip.split("\\.");																	//https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
		ipInByte[0]=(byte)Integer.parseInt(ipInParts[0]);
		ipInByte[1]=(byte)Integer.parseInt(ipInParts[1]);
		ipInByte[2]=(byte)Integer.parseInt(ipInParts[2]);
		ipInByte[3]=(byte)Integer.parseInt(ipInParts[3]);

		System.arraycopy(nameLengthInByte2,	0, data,0,						nameLengthInByte2.length);
		System.arraycopy(nameInByte,		0, data,4,						nameInByte.length);
		System.arraycopy(ipInByte,			0, data,nameInByte.length + 4 ,	ipInByte.length);

		Datagram datagram = new Datagram(header, data);
		System.out.println(Constants.DISCOVERY_MULTICAST_IP);
		System.out.println(Constants.DISCOVERY_NAMESERVER_PORT);
		System.out.println(datagram.toString());
		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize() );
		udpClient.stop();
	}

	/**
	 * Multicast for nodes
	 * Nodes will process this message
     * Now, the nodes in the network can update their neighbours
	 */
	public void neighbourRequest ()
	{
	    this.udpClient = new Client();
		udpClient.start();

		byte version = (byte)0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		this.startupTransactionId = rand.nextInt();
		int dataLength = name.length() + 8;
		ProtocolHeader header = new ProtocolHeader(version,dataLength,startupTransactionId,requestCode,replyCode);

		byte [] data = new byte[name.length() + 8];
		byte [] nameLengthInByte = Serializer.intToBytes(name.length());
		byte [] nameInByte = name.getBytes();

		byte[] ipInByte = new byte[4];
		String[] ipInParts = ip.split("\\.");																	//https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
		ipInByte[0]=(byte)Integer.parseInt(ipInParts[0]);
		ipInByte[1]=(byte)Integer.parseInt(ipInParts[1]);
		ipInByte[2]=(byte)Integer.parseInt(ipInParts[2]);
		ipInByte[3]=(byte)Integer.parseInt(ipInParts[3]);

		System.arraycopy(nameLengthInByte,	0, data,0,						nameLengthInByte.length);
		System.arraycopy(nameInByte,		0, data,4,						nameInByte.length);
		System.arraycopy(ipInByte,			0, data,nameInByte.length + 4 ,	ipInByte.length);

		Datagram datagram = new Datagram(header, data);

		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, datagram.serialize() );
		udpClient.stop();
	}


	/**
	 * Node listens on multicast for new incoming nodes.
	 * if there is so, update the neighbours
	 */

	public void multicastListener()
    {
		if(subscriber.hasData())
		{
			DatagramPacket packet = subscriber.receivePacket();
			Datagram request = new Datagram(packet.getData());
			byte[] data = request.getData();

			if(request.getHeader().getReplyCode() == ProtocolHeader.REQUEST_DISCOVERY_CODE)
			{
				byte[] nameLength = new byte[4];
				//put the namelength in separate array and wrap it into an int
				nameLength[0] = data[0];
				nameLength[1] = data[1];
				nameLength[2] = data[2];
				nameLength[3] = data[3];

				int num = Serializer.bytesToInt(nameLength);

				//get the name and put it in a byte Array
				byte[] nameArray = new byte[num];
				for(int i = 0;i<num;i++){
					nameArray[i] = data[4+i];
				}

				String name = new String(nameArray);
				changeNeighbours(getHash(name));

			}
		}
	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 */
	private void changeNeighbours(int id)
	{
		if(numberOfNodes>1)
		{
			if((this.id < id) && (id < nextNeighbour))
			{
			    this.udpClient = new Client();
				udpClient.start();

				byte version = (byte)0;
				short replyCode = ProtocolHeader.NO_REPLY;
				short requestCode = ProtocolHeader.REQUEST_NEW_NEIGHBOUR;
				int transactionID = rand.nextInt();
				byte[] neighbourIdInBytes = Serializer.intToBytes(nextNeighbour);
				byte[] idInBytes = Serializer.intToBytes(this.id);
				int dataLength = idInBytes.length + neighbourIdInBytes.length;

				ProtocolHeader header = new ProtocolHeader(version,dataLength,transactionID,requestCode,replyCode);

				byte[] idPacket = new byte[dataLength];

				System.arraycopy(idInBytes,0,idPacket,0,idInBytes.length);
				System.arraycopy(neighbourIdInBytes,0,idPacket,idInBytes.length,neighbourIdInBytes.length);

				Datagram datagram = new Datagram(header, idPacket);

				try
				{
					udpClient.send(resolverInterface.getIP(id),Constants.UDP_NODE_PORT,datagram.serialize());
					udpClient.stop();
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}

				this.nextNeighbour = id;
			}

			if ((previousNeighbour < id) && (id < this.id))
			{
				this.previousNeighbour = id;
			}
		}

		else if (numberOfNodes == 1)
		{
		    this.udpClient = new Client();
			udpClient.start();

            byte version = (byte)0;
            short replyCode = ProtocolHeader.NO_REPLY;
            short requestCode = ProtocolHeader.REQUEST_NEW_NEIGHBOUR;
            int transactionID = rand.nextInt();
            byte[] neighbourIdInBytes = Serializer.intToBytes(nextNeighbour);
            byte[] idInBytes = Serializer.intToBytes(this.id);
            int dataLength = idInBytes.length + neighbourIdInBytes.length;

            ProtocolHeader header = new ProtocolHeader(version,dataLength,transactionID,requestCode,replyCode);

            byte[] idPacket = new byte[dataLength];

            System.arraycopy(idInBytes,0,idPacket,0,idInBytes.length);
            System.arraycopy(neighbourIdInBytes,0,idPacket,idInBytes.length,neighbourIdInBytes.length);

            Datagram datagram = new Datagram(header, idPacket);


            try
            {
                udpClient.send(resolverInterface.getIP(id),Constants.UDP_NODE_PORT,datagram.serialize());
                udpClient.stop();
            }
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
			setNeighbours(id);
		}

		numberOfNodes++;
	}

	/**
	 * The node can receive three types of data (up to now)
	 * 1) reply of the NS on the access request of me -> change my neighbours
     * 2) error replay from NS -> change name
	 * 3) request of the new node to update my neighbours
	 */

	public void run(){
		unicastListener();
	}

	public void unicastListener()
	{

		while(udpServer.isEmpty()){

		}

		DatagramPacket packet = udpServer.receivePacket();

		Datagram request = new Datagram(packet.getData());
		byte[] data = request.getData();

		//check if message contains request from new neighbour
		if (request.getHeader().getReplyCode() == ProtocolHeader.REQUEST_NEW_NEIGHBOUR)
		{
			setNeighbours(
					(int)((data[0] << 24) | (data[1] << 16) | (data[2]) << 8| (data[3])),
					(int)((data[4] << 24) | (data[5] << 16) | (data[6]) << 8| (data[7]))
			);

		}

		//check if message contains error message from nameserver
		if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_ID) &&
				(request.getHeader().getTransactionID() == this.startupTransactionId))
		{
			askNewName();
			accessRequest();
		}

		//check if succesfully added to nameserver
		if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED) &&
				(request.getHeader().getTransactionID() == this.startupTransactionId))
		{
			//nameserver sends the amount of nodes in the tree
			this.numberOfNodes = (short)(data[2] << 8 | data[3]);
			neighbourRequest();
			subscribeOnMulticast();
			multicastListener();
			System.out.println("");

		}

	}

	/**
	 * Subscribe this node on the multicast-address 224.0.0.1
	 */
	public void subscribeOnMulticast ()
	{
		subscriber = new Subscriber(Constants.DISCOVERY_MULTICAST_IP,Constants.DISCOVERY_CLIENT_PORT);
		subscriber.start();

	}

	/**
	 * Unsubscribe this node on the multicast-address 224.0.0.1
	 */
	public void unsubscribeMulticast ()
	{
		subscriber.stop();
	}

	/**
	 * If this node has no neighbours
	 */
	private void setNeighbours()
	{
		previousNeighbour = this.id;
		nextNeighbour = this.id;
	}

	/**
	 * If this node has 1 neighbours
	 */
	private void setNeighbours(int neighbourId)
	{
		previousNeighbour = neighbourId;
		nextNeighbour = neighbourId;
	}

	/**
	 * If this node has 2 neighbours
	 */
	private void setNeighbours(int previousNeighbour, int nextNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
		this.nextNeighbour = nextNeighbour;
	}

	/**
	 * Calculate the hashcode of the name
	 * @return ID
	 */
	private static short getHash(String name)
	{
		return (short) Math.abs(name.hashCode() % 32768);
	}

	private void askNewName ()
	{
		System.out.println("Please enter a new name: ");
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();
		setName(name);
		setID(getHash(name));
	}

	private void askNewIP ()
	{
		System.out.println("Please enter a new IP address: ");
		Scanner scanner = new Scanner(System.in);
		String ip = scanner.nextLine();
		setIp(ip);
	}

	private short getID()
	{
		return id;
	}

	private void setID(short id)
	{
		this.id = id;
	}

	private String getIp()
	{
		return ip;
	}

	private void setIp(String ip)
	{
		this.ip = ip;
	}

	private String getName()
	{
		return name;
	}

	private void setName(String name)
	{
		this.name = name;
	}

	public int getPreviousNeighbour()
	{
		return previousNeighbour;
	}

	public void setPreviousNeighbour(int previousNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
	}

	public int getNextNeighbour()
	{
		return nextNeighbour;
	}

	public void setNextNeighbour(int nextNeighbour)
	{
		this.nextNeighbour = nextNeighbour;
	}

	public short getId()
	{
		return id;
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
