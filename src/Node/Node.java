package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.UDP.Multicast.*;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.UDPClient;
import NameServer.NameServer;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;
import Util.Serializer;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class Node
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
	private Random rand;
	private short numberOfNodes;
	private int startupTransactionId;

	/**
	 * Initialize the new node with his RMI-applications, name, ip and ID
	 */
	public Node(String name, ResolverInterface resolverInterface, ShutdownAgentInterface shutdownAgentInterface)
	{
		this.numberOfNodes = 0;
		this.udpClient = new Client();
		this.rand = new Random();

		this.resolverInterface=resolverInterface;
		this.shutdownAgentInterface=shutdownAgentInterface;

		try
		{
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.id = getHash(name);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		this.previousNeighbour = this.id;
		this.nextNeighbour = this.id;
	}

	/**
	 * Grant access to the network by sending a multicast on ip 224.0.0.1 and port 2001
	 * NS will process this message
	 */
	public void accessRequest ()
	{
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

		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize() );
	}

	/**
	 * Node listens on multicast for new incoming nodes.
	 * if there is so, change the neighbours
	 */

	public void multicastListener(){
		if(subscriber.hasData()){
			byte[] subData = subscriber.receiveData();
			if((short)((subData[10] << 8) | (subData[11])) == 0x0001){

				byte[] nameLength = new byte[4];
				//put the namelength in separate array and wrap it into an int
				nameLength[0] = subData[12];
				nameLength[1] = subData[13];
				nameLength[2] = subData[14];
				nameLength[3] = subData[15];

				ByteBuffer wrapped = ByteBuffer.wrap(nameLength); // big-endian by default
				int num = wrapped.getShort();

				//get the name and put it in a byte Array
				byte[] nameArray = new byte[num];
				for(int i = 0;i<num;i++){
					nameArray[i] = subData[16+i];
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
			byte version = (byte)0;
			short replyCode = ProtocolHeader.NO_REPLY;
			short requestCode = ProtocolHeader.REQUEST_NEW_NEIGHBOUR;
			int transactionID = rand.nextInt();
			byte[] idInBytes = Serializer.intToBytes(this.id);
			int dataLength = idInBytes.length;

			ProtocolHeader header = new ProtocolHeader(version,dataLength,transactionID,requestCode,replyCode);

			Datagram datagram = new Datagram(header, idInBytes);


			try
			{
				udpClient.send(resolverInterface.getIP(id),Constants.UDP_NODE_PORT,datagram.serialize());
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
			setNeighbours(id);
		}

		numberOfNodes++;
	}

	/**
	 * The node can receive two types of data (up to now)
	 * 1) reply of the NS on the access request of me -> change my neighbours
	 * 2) request of the new node to update my neighbours
	 */
	public void unicastListener()
	{

		byte[] receivedData = udpClient.receiveData();

		//check if message contains request from new neighbour
		if ((short) ((receivedData[8] << 8) | (receivedData[9])) == 0x0003)
		{
			//if length == 20 , then previous and next neighbour are present in the data
			if ((int)((receivedData[1] << 16) | (receivedData[2] << 8) | (receivedData[3])) == 20){
				setNeighbours(
						(int)((receivedData[12] << 24) | (receivedData[13] << 16) | (receivedData[14]) << 8| (receivedData[15])),
						(int)((receivedData[16] << 24) | (receivedData[17] << 16) | (receivedData[18]) << 8| (receivedData[19]))
				);
			}

			//if length == 16 then the data only contains the previous neighbour
			if ((int)((receivedData[1] << 16) | (receivedData[2] << 8) | (receivedData[3])) == 16){
				setNeighbours(
						(int)((receivedData[12] << 24) | (receivedData[13] << 16) | (receivedData[14]) << 8| (receivedData[15]))
				);

			}

		}

		//check if message contains error message from nameserver
		if (((short) ((receivedData[10] << 8) | (receivedData[11])) == 0x0002) &&
				((int)((receivedData[4] << 24) | (receivedData[5] << 16) | (receivedData[6]) << 8| (receivedData[7])) == this.startupTransactionId))
		{
			askNewName();
			accessRequest();
		}

		//check if succesfully added to nameserver
		if (((short) ((receivedData[10] << 8) | (receivedData[11])) == 0x0001) &&
				((int)((receivedData[4] << 24) | (receivedData[5] << 16) | (receivedData[6]) << 8| (receivedData[7])) == this.startupTransactionId))
		{
			//nameserver sends the amount of nodes in the tree
			this.numberOfNodes = (short)(receivedData[14] << 8 | receivedData[15]);
			subscribeOnMulticast();
			multicastListener();
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
