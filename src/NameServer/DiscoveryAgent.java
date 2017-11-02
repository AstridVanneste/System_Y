package NameServer;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;
import Util.Serializer;

import java.net.DatagramPacket;

public class DiscoveryAgent implements Runnable
{
	private boolean quit;
	private Subscriber multicastSub;

	public DiscoveryAgent ()
	{
		this.quit = false;
		this.multicastSub = new Subscriber(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT);
		System.out.println("Set up multicast sub to listen on " + Constants.DISCOVERY_MULTICAST_IP + ":" + Integer.toString(Constants.DISCOVERY_NAMESERVER_PORT));
	}

	public void init ()
	{
		multicastSub.start();
		System.out.println("Started listening");
		Thread t = new Thread (this);
		t.start();
	}

	@Override
	public void run()
	{
		while (!this.quit)
		{
			System.out.println("Multicast sub buffer length: " + Integer.toString(this.multicastSub.getBufferLength()));
			if (this.multicastSub.getBufferLength() > 0)
			{
				System.out.println("Multicast sub received data");

				DatagramPacket packet = this.multicastSub.receivePacket();
				Datagram request = new Datagram(packet.getData());

				if (request.getHeader().getRequestCode() == ProtocolHeader.REQUEST_DISCOVERY_CODE)
				{
					byte[] data = request.getData();
					byte[] nameLenBytes = new byte [4];
					System.arraycopy(data, 0, nameLenBytes, 0, 4);
					int nameLen = Util.Serializer.bytesToInt(nameLenBytes);

					if (data.length != 8 + nameLen)
					{
						System.err.println("Packet data had length of " + Integer.toString(data.length) + ", was supposed to be " + Integer.toString(8 + nameLen));
						continue;
					}


					byte[] nameBytes = new byte [nameLen];
					System.arraycopy(data, 4, nameLenBytes, 0, nameLen);
					String nodeName = new String (nameBytes, Constants.ENCODING);

					if (NameServer.getInstance().map.containsKey(NameServer.getHash(nodeName)))
					{
						// Return failure
						System.err.println("Nameserver already has record for hash " + Integer.toString(NameServer.getHash(nodeName)));
						ProtocolHeader replyHeader = new ProtocolHeader(request.getHeader());
						replyHeader.setReplyCode(0x0002);
						Datagram replyDatagram = new Datagram(replyHeader);

						Client replyClient = new Client();
						replyClient.start();
						replyClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, replyDatagram.serialize());
						replyClient.stop();
						continue;
					}

					byte[] unicastIPBytes = new byte [4];
					System.arraycopy(data, 4 + nameLen, unicastIPBytes, 0, 4);
					String unicastIp = Serializer.bytesToIPString(unicastIPBytes);

					if (NameServer.getInstance().map.containsValue(unicastIp))
					{
						// Return failure
						System.err.println("Nameserver already has a record for IP " + unicastIp);
						ProtocolHeader replyHeader = new ProtocolHeader(request.getHeader());
						replyHeader.setReplyCode(0x0003);
						Datagram replyDatagram = new Datagram(replyHeader);

						Client replyClient = new Client();
						replyClient.start();
						replyClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, replyDatagram.serialize());
						replyClient.stop();
						continue;
					}

					// Return succes
					short nodeId = (short) NameServer.getHash(nodeName);
					short nextId = NameServer.getInstance().map.higherKey(nodeId);
					short prevId = NameServer.getInstance().map.lowerKey(nodeId);
					short numNodes = (short) NameServer.getInstance().map.size();

					byte[] replyData = new byte [8];
					replyData[7] = (byte) ((nodeId >>> 8) & 0x00FF);
					replyData[6] = (byte) (nodeId & 0x00FF);

					replyData[5] = (byte) ((numNodes >>> 8) & 0x00FF);
					replyData[4] = (byte) (numNodes & 0x00FF);

					replyData[3] = (byte) ((nextId >>> 8) & 0x00FF);
					replyData[2] = (byte) (nextId & 0x00FF);

					replyData[1] = (byte) ((prevId >>> 8) & 0x00FF);
					replyData[0] = (byte) (prevId & 0x00FF);

					ProtocolHeader replyHeader = new ProtocolHeader(request.getHeader());
					replyHeader.setReplyCode(0x0001);
					Datagram replyDatagram = new Datagram(replyHeader);
					replyDatagram.setData(replyData);

					Client replyClient = new Client();
					replyClient.start();
					replyClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, replyDatagram.serialize());
					replyClient.stop();
				}
			}
		}
	}

	public void stop ()
	{
		this.quit = true;
		this.multicastSub.stop();
	}
}
