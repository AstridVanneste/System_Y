package Node;

import Network.UDP.Multicast.*;
import Network.Datagrams.ProtocolHeader;
import Network.UDP.Unicast.Client;
import Network.UDP.Unicast.UDPClient;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.*;
import IO.Network.UDP.Unicast.Client;
import IO.Network.UDP.Unicast.UDPClient;
import NameServer.ResolverInterface;
//import NameServer.DiscoveryAgentInterface;
import NameServer.ShutdownAgentInterface;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Node{
	private String ip;
	private String name;
	private String previousNeighbour;
	private String nextNeighbour;
	private Subscriber sub;
	private String multicastIP = "224.0.0.1";
	private int multicastPort = 1997;
	private int id;
	private Subscriber subscriber;
	private ResolverInterface resolverInterface;
	private ShutdownAgentInterface shutdownAgentInterface;
	//private DiscoveryAgentInterface discoveryAgentInterface;

	public Node(String name){
		this.resolverInterface=resolverInterface;
		this.shutdownAgentInterface=shutdownAgentInterface;

		/*System.out.print("Enter the hostName : ");
		Scanner input = new Scanner(System.in);
		name = input.nextLine();*/

		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.id = getHash(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.name = name;
		//this.ip = giveIp;
		sub = new Subscriber(ip, 5000);

	}

	public void accessRequest () {

		ProtocolHeader header = new ProtocolHeader();
		header.setVersion((byte)1);
		header.setDataLength(0);
		header.setTransactionID(1);         //is this 'unique' enough?
		header.setRequestCode(0);

		byte[] serial = header.serialize();

		header.setHeader(serial);

		System.out.println(header.toString());

		UDPClient udpClient = new Client();
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

	public void setNeighbours(int amountNeighbours){
		if(amountNeighbours == 0){
			previousNeighbour = this.ip;
			nextNeighbour = this.ip;
		}


		if(amountNeighbours == 1){
			//getIP
			//previousNeighbour = nextNeighbour;
		}



		if(amountNeighbours == 2){
			//previousNeighbour =;
			//nextNeighbour=;
		}
	}

	public String getData(){




		return null;
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