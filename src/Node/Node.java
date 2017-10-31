package Node;

import IO.Network.Datagrams.Datagram;
import IO.Network.UDP.Multicast.*;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.UDPClient;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;


public class Node{
	private String ip;
	private String name;
	private String previousNeighbour;
	private String nextNeighbour;
	private String multicastIP = "224.0.0.1";
	private int multicastPort = 1997;
	private int id;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	//private DiscoveryAgentInterface discoveryAgentInterface;
	private Client udpClient;

	public Node(String name, ResolverInterface resolverInterface, ShutdownAgentInterface shutdownAgentInterface){
		this.resolverInterface=resolverInterface;
		this.shutdownAgentInterface=shutdownAgentInterface;

		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.id = getHash(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.name = name;
		udpClient = new Client();
	}

	public void accessRequest () {

		ProtocolHeader header = new ProtocolHeader();
		header.setVersion((byte)1);
		header.setDataLength(ip.length() + name.length());
		header.setTransactionID(1);         //is this 'unique' enough?
		header.setRequestCode(0);

		byte[] serial = header.serialize();

		header.setHeader(serial);

		System.out.println(header.toString());


		udpClient.start();

		// Send header with access request
		udpClient.send(multicastIP, multicastPort, serial );

		// Send name + ip as data
		byte [] data = new byte[ip.length() + name.length()];
		System.arraycopy(name, 0 , data,0,name.length());
		System.arraycopy(ip,0,data,name.length(),ip.length());
		udpClient.send(multicastIP, multicastPort, data );



	}


	public void subscribeOnMulticast () {
		subscriber = new Subscriber(multicastIP,multicastPort);
		subscriber.start();

	}

	public void unsubscribeMulticast () {
		subscriber.stop();
	}

	public static int getHash(String name) {

		return Math.abs(name.hashCode() % 32768);

	}

	public void setNeighbours(){
		previousNeighbour = this.ip;
		nextNeighbour = this.ip;
	}

	public void setNeighbours(String neighbourIp){
		previousNeighbour = neighbourIp;
		nextNeighbour = neighbourIp;

	}

	public void setNeighbours(String previousNeighbour, String nextNeighbour){
		this.previousNeighbour = previousNeighbour;
		this.nextNeighbour = nextNeighbour;
	}


	public void sendNeighbours(){
		byte version = (byte)0;
		short replyCode = (short)0;
		short requestCode = (short)2;
		ProtocolHeader header = new ProtocolHeader(version,2,2,requestCode,replyCode);
		//put id in a byte buffer
		byte[] id = ByteBuffer.allocate(4).putInt(getID()).array();

		byte[] ipToSend = new byte[4];
		String[] ipInParts = ip.split(".");
		ipToSend[0]=(byte)Integer.parseInt(ipInParts[0]);
		ipToSend[1]=(byte)Integer.parseInt(ipInParts[1]);
		ipToSend[2]=(byte)Integer.parseInt(ipInParts[2]);
		ipToSend[3]=(byte)Integer.parseInt(ipInParts[3]);


		Datagram datagram = new Datagram(header, ipToSend);//needs to be replaced by ip + id

		udpClient.send(nextNeighbour,5000,datagram.serialize());
		udpClient.send(previousNeighbour,5000,datagram.serialize());

	}



	public void changeNeighbours(int id, String ip){
		if(getID()< id){
			nextNeighbour = ip;

		}
		if(getID()> id){
			previousNeighbour = ip;

		}
	}


	public void getData()
	{
		udpClient.run();
		byte[] receivedData = udpClient.receiveData();
		if(receivedData[10] == 0x0000){
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
				} catch (UnsupportedEncodingException e)
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
				} catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

			}
		}

		udpClient.stop();

	}



	public int getID(){
		return Math.abs(name.hashCode() % 32768);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPreviousNeighbour() {
		return previousNeighbour;
	}

	public void setPreviousNeighbour(String previousNeighbour) {
		this.previousNeighbour = previousNeighbour;
	}

	public String getNextNeighbour() {
		return nextNeighbour;
	}

	public void setNextNeighbour(String nextNeighbour) {
		this.nextNeighbour = nextNeighbour;
	}
}
