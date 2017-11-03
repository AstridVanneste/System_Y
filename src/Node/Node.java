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
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class Node
{
	private String ip;
	private String name;
	private String previousNeighbour;
	private String nextNeighbour;
	private short id;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	//private DiscoveryAgentInterface discoveryAgentInterface;
	private Client udpClient;
	private Random rand;

	/**
	 * Initialize the new node with his RMI-applications, name, ip and ID
	 */
	public Node(String name, ResolverInterface resolverInterface, ShutdownAgentInterface shutdownAgentInterface)
	{
		rand = new Random();
		udpClient = new Client();

		this.resolverInterface=resolverInterface;
		this.shutdownAgentInterface=shutdownAgentInterface;

		try
		{
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.id = getHash(ip);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Grant access to the network by sending a multicast on ip 224.0.0.1 and port 1997
	 * NS will process this message
	 */
	public void accessRequest ()
	{
		udpClient.start();

		byte version = (byte)0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		int transactionID = rand.nextInt();
		int dataLength = name.length() + 8;
		ProtocolHeader header = new ProtocolHeader(version,dataLength,transactionID,requestCode,replyCode);

		byte [] data = new byte[name.length() + 8];
		byte [] nameLengthInByte = Serializer.intToBytes(name.length());
		byte [] nameInByte = name.getBytes();

		byte[] ipInByte = new byte[4];
		String[] ipInParts = ip.split("\\.");																		//https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
		ipInByte[0]=(byte)Integer.parseInt(ipInParts[0]);
		ipInByte[1]=(byte)Integer.parseInt(ipInParts[1]);
		ipInByte[2]=(byte)Integer.parseInt(ipInParts[2]);
		ipInByte[3]=(byte)Integer.parseInt(ipInParts[3]);

		System.arraycopy(nameLengthInByte,	0, data,0,						nameLengthInByte.length);
		System.arraycopy(nameInByte,		0, data,4,						nameInByte.length);
		System.arraycopy(ipInByte,			0, data,nameInByte.length + 4 ,	ipInByte.length);

		Datagram datagram = new Datagram(header, data);

		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize() );

		udpClient.stop();
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
		previousNeighbour = this.ip;
		nextNeighbour = this.ip;
	}

	/**
	 * If this node has 1 neighbours
	 */
	private void setNeighbours(String neighbourIp)
	{
		previousNeighbour = neighbourIp;
		nextNeighbour = neighbourIp;
	}

	/**
	 * If this node has 2 neighbours
	 */
	private void setNeighbours(String previousNeighbour, String nextNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
		this.nextNeighbour = nextNeighbour;
	}

	/**
	 * After been accepted in network send a message to his two neighbours.
	 * These neighbours will correct their neighbours
	 */
	public void sendNeighbours()
	{
		udpClient.start();

		byte version = (byte)0;
		short replyCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		short requestCode = (short)2;
		int transactionID = rand.nextInt();
		int dataLength = 2;
		ProtocolHeader header = new ProtocolHeader(version,dataLength,transactionID,requestCode,replyCode);

		byte[] idInBytes = Serializer.intToBytes(getID());

		byte[] ipToSend = new byte[4];
		String[] ipInParts = ip.split("\\.");																		//https://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
		ipToSend[0]=(byte)Integer.parseInt(ipInParts[0]);
		ipToSend[1]=(byte)Integer.parseInt(ipInParts[1]);
		ipToSend[2]=(byte)Integer.parseInt(ipInParts[2]);
		ipToSend[3]=(byte)Integer.parseInt(ipInParts[3]);

		byte [] data = new byte[8];
		System.arraycopy(idInBytes,0,data,0,idInBytes.length);
		System.arraycopy(ipToSend,0,data,4,ipToSend.length);

		Datagram datagram = new Datagram(header, data);

		udpClient.send(nextNeighbour,5000,datagram.serialize());
		udpClient.send(previousNeighbour,5000,datagram.serialize());

		udpClient.stop();
	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 */
	private void changeNeighbours(int id, String ip)
	{
		if(getID()< id)
		{
			nextNeighbour = ip;
		}
		if(getID()> id)
		{
			previousNeighbour = ip;
		}
	}

	/**
	 * The node can receive two types of data (up to now)
	 * 1) reply of the NS on the access request of me -> change my neighbours
	 * 2) request of the new node to update my neighbours
	 */
	public void getData()
	{
		udpClient.start();
		udpClient.run();

		byte[] receivedData = udpClient.receiveData();

		if(receivedData[10] == 0x0000)
		{
			System.out.println("Package received with code 0x0000");
			if (receivedData[16] == 0)
			{
				setNeighbours();
			}
			if (receivedData[16] == 1)
			{
				try
				{
					String ip1 = new String(new byte[]{receivedData[5]}, "UTF-8");
					String ip2 = new String(new byte[]{receivedData[6]}, "UTF-8");
					String ip3 = new String(new byte[]{receivedData[7]}, "UTF-8");
					String ip4 = new String(new byte[]{receivedData[8]}, "UTF-8");
					setNeighbours(ip1.concat(".").concat(ip2).concat(".").concat(ip3).concat(".").concat(ip4));
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

			}
			if (receivedData[16] >= 2)
			{
				try
				{
					String ip1 = new String(new byte[]{receivedData[5]}, "UTF-8");
					String ip2 = new String(new byte[]{receivedData[6]}, "UTF-8");
					String ip3 = new String(new byte[]{receivedData[7]}, "UTF-8");
					String ip4 = new String(new byte[]{receivedData[8]}, "UTF-8");

					String ip5 = new String(new byte[]{receivedData[9]}, "UTF-8");
					String ip6 = new String(new byte[]{receivedData[10]}, "UTF-8");
					String ip7 = new String(new byte[]{receivedData[11]}, "UTF-8");
					String ip8 = new String(new byte[]{receivedData[12]}, "UTF-8");

					setNeighbours(ip1.concat(".").concat(ip2).concat(".").concat(ip3).concat(".").concat(ip4), ip5.concat(".").concat(ip6).concat(".").concat(ip7).concat(".").concat(ip8));
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

			}
		}
		else if(receivedData[10] == 0x00000002)
		{
			System.out.println("Package received with code 0x0002");

			byte[] idbyte = Arrays.copyOfRange(receivedData, 0,4);
			int idInt = java.nio.ByteBuffer.wrap(idbyte).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
			//String idString = Integer.toString(idInt);

			try
			{
				String ip1 = new String(new byte[]{receivedData[4]}, "UTF-8");
				String ip2 = new String(new byte[]{receivedData[5]}, "UTF-8");
				String ip3 = new String(new byte[]{receivedData[6]}, "UTF-8");
				String ip4 = new String(new byte[]{receivedData[7]}, "UTF-8");

				String ipNeighbour = ip1.concat(".").concat(ip2).concat(".").concat(ip3).concat(".").concat(ip4);
				changeNeighbours(idInt,ipNeighbour);

			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}

	}
		udpClient.stop();

	}

	/**
	 * Calculate the hashcode of the name
	 * @return ID
	 */
	private static short getHash(String name)
	{
		return (short) Math.abs(name.hashCode() % 32768);
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

	private String getPreviousNeighbour()
	{
		return previousNeighbour;
	}

	private void setPreviousNeighbour(String previousNeighbour)
	{
		this.previousNeighbour = previousNeighbour;
	}

	private String getNextNeighbour()
	{
		return nextNeighbour;
	}

	private void setNextNeighbour(String nextNeighbour)
	{
		this.nextNeighbour = nextNeighbour;
	}
}
