package NameServer;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;
import Node.Node;
import Node.NodeInteractionInterface;
import Util.General;
import Util.Serializer;

import java.net.DatagramPacket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
					//System.out.println(request.getHeader().getTransactionID());
					if (request.getHeader().getRequestCode() == ProtocolHeader.REQUEST_DISCOVERY_CODE)
					{
						//System.out.println("DiscoveryAgent.run()");

						// Get data
						// DiscoveryPackage consists of NameLength, Node Name, and Node IP

						byte[] data = request.getData();

						// Get Name Length out of data
						byte[] nameLenBytes = new byte[4];
						System.arraycopy(data, 0, nameLenBytes, 0, 4);
						int nameLen = Util.Serializer.bytesToInt(nameLenBytes);

						if (data.length != 8 + nameLen)
						{
							System.err.println("Packet data had length of " + Integer.toString(data.length) + ", was supposed to be " + Integer.toString(8 + nameLen));
							continue;
						}

						// Get Node Name out of data
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

						// Get Node IP out of data
						int[] unicastIPBytes = new int[4];
						//System.arraycopy(data, 4 + nameLen, unicastIPBytes, 0, 4);

						for (int i = 4 + nameLen; i < 8 + nameLen; i++)
						{
							unicastIPBytes[i - (4 + nameLen)] = data[i] & 0x000000FF;
						}

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
						// Return package consists of NodeID and number of nodes already in network

						short nodeId = NameServer.getHash(nodeName);

						//System.out.println("Name: " + nodeName + ", IP: " + unicastIp + ", ID: " + Short.toString(nodeId));

						NameServer.getInstance().map.put(nodeId, unicastIp);
						short numNodes = (short) NameServer.getInstance().map.size();
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
						//System.out.println("TransactionID: " + replyHeader.getTransactionID());

						Client replyClient = new Client();
						replyClient.start();
						//for (int i = 0; i < 10; i++)    // For testing purposes only, because UDP seems to be broken, normally, this should only be run once
						//{
						replyClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_CLIENT_PORT, replyDatagram.serialize());
						//}
						replyClient.stop();

						Util.General.printLineSep();
						System.out.println("Discovery: " + nodeName + " ID: " + nodeId);
						Util.General.printLineSep();

						if(numNodes == 2)
						{
							NameServer.getInstance().setRingMonitorId(nodeId);
							try
							{
								Registry reg = LocateRegistry.getRegistry(NameServer.getInstance().getResolver().getIP(nodeId));
								NodeInteractionInterface node = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
								node.runRingMonitor();
								Util.General.printLineSep();
								System.out.println("Ring monitor set to: " + nodeId);
								Util.General.printLineSep();
							}
							catch (RemoteException re)
							{
								re.printStackTrace();
							}
							catch (NotBoundException nbe)
							{
								nbe.printStackTrace();
							}
						}
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
