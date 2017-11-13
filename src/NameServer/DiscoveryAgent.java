package NameServer;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;
import Util.Serializer;

import java.net.DatagramPacket;

/**
 * The discovery agent just runs in the background listening for discovery requests
 * and answers them when needed...
 */
public class DiscoveryAgent implements Runnable
{
	private boolean quit;
	private Subscriber multicastSub;

	public DiscoveryAgent ()
	{
		this.quit = false;
		this.multicastSub = new Subscriber(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT);
	}

	public void init ()
	{
		//System.out.println("Init discovery Agent");
		//System.out.println("'" + NameServer.getInstance().toString() + "'");
		multicastSub.start();
		Thread t = new Thread (this);
		t.start();
	}

	@Override
	public void run()
	{
		while (!this.quit)
		{
			synchronized (this.multicastSub)
			{
				if (this.multicastSub.getBufferLength() > 0)
				{
					DatagramPacket packet = this.multicastSub.receivePacket();
					Datagram request = new Datagram(packet.getData());
					System.out.println(request.getHeader().getTransactionID());
					if (request.getHeader().getRequestCode() == ProtocolHeader.REQUEST_DISCOVERY_CODE)
					{
						byte[] data = request.getData();
						byte[] nameLenBytes = new byte[4];
						System.arraycopy(data, 0, nameLenBytes, 0, 4);
						int nameLen = Util.Serializer.bytesToInt(nameLenBytes);

						if (data.length != 8 + nameLen)
						{
							System.err.println("Packet data had length of " + Integer.toString(data.length) + ", was supposed to be " + Integer.toString(8 + nameLen));
							continue;
						}

						byte[] nameBytes = new byte[nameLen];
						System.arraycopy(data, 4, nameBytes, 0, nameLen);
						String nodeName = new String(nameBytes, Constants.ENCODING);

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

						short[] unicastIPBytes = new short[4];
						//System.arraycopy(data, 4 + nameLen, unicastIPBytes, 0, 4);

						for (int i = 4 + nameLen; i < 8 + nameLen; i++)
						{
							unicastIPBytes[i - (4 + nameLen)] = data[i];
						}

						String unicastIp = Serializer.bytesToIPString(unicastIPBytes);

						System.out.println("Name: " + nodeName + " , IP: " + unicastIp );

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
						short numNodes = (short) NameServer.getInstance().map.size();

						System.out.println("Name: " + nodeName + " , IP: " + unicastIp + " , ID: " + nodeId);

						NameServer.getInstance().map.put(nodeId, unicastIp);
						NameServer.getInstance().writeMapToFile();

						byte[] replyData = new byte[4];
						replyData[0] = (byte) ((nodeId >>> 8) & 0x00FF);
						replyData[1] = (byte) (nodeId & 0x00FF);

						replyData[2] = (byte) ((numNodes >>> 8) & 0x00FF);
						replyData[3] = (byte) (numNodes & 0x00FF);

						ProtocolHeader replyHeader = new ProtocolHeader(request.getHeader());
						replyHeader.setReplyCode(ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);
						Datagram replyDatagram = new Datagram(replyHeader);
						replyDatagram.setData(replyData);
						System.out.println("TransactionID: " + replyHeader.getTransactionID());

						Client replyClient = new Client();
						replyClient.start();
						//for (int i = 0; i < 10; i++)    // For testing purposes only, because UDP seems to be broken, normally, this should only be run once
						//{
						replyClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, replyDatagram.serialize());
						//}
						replyClient.stop();
					}
					else
					{
						System.err.println("[" + Thread.currentThread().getName() + "] Received multicast packet did not have correct request code, needs to be " + Integer.toHexString(ProtocolHeader.REQUEST_DISCOVERY_CODE) + ", got " + request.getHeader().getRequestCode());
					}
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
