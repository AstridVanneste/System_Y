import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;

import java.net.DatagramPacket;
import java.util.Random;

public class DiscoveryTestClient
{
	public static void main (String[] args)
	{
		System.out.println("Setting up datagram");

		String nodeName = "thomas";
		Random r = new Random();
		ProtocolHeader header = new ProtocolHeader((byte) 1, 8 + nodeName.length(), r.nextInt(), (short) 0x0001, (short) 0x0000);
		//byte[] requestData = new byte [8 + nodeName.length()];
		//System.arraycopy(Util.Serializer.intToBytes(nodeName.length()), 0, requestData, 0, 4);
		//System.arraycopy(nodeName.getBytes(), 0, requestData, 4, nodeName.length());
		//System.arraycopy(Util.Serializer.ipStringToBytes("192.168.0.244"), 0, requestData, 4 + nodeName.length(), 4);
		//Datagram d = new Datagram(header);
		//d.setData(requestData);

		System.out.println("Set up datagram, sending...");

		Client udpClient = new Client();
		//Subscriber discoverySub = new Subscriber(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT);
		//discoverySub.start();
		udpClient.start();
		udpClient.send("224.0.0.1", 2000, header.serialize());
		udpClient.stop();

		/*
		System.out.println("Sent datagram, listening...");

		while (true)
		{
			if (discoverySub.getBufferLength() > 0)
			{
				System.out.println("Received data");
				DatagramPacket dp = discoverySub.receivePacket();
			}
			else
			{
				//System.out.println("Received no data");
			}
		}
		*/
	}
}
