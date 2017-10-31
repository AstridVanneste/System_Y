package NameServer;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.net.DatagramPacket;

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
			if (this.multicastSub.hasData())
			{
				DatagramPacket packet = this.multicastSub.receivePacket();
				Datagram request = new Datagram(packet.getData());

				if (request.getHeader().getRequestCode() == ProtocolHeader.REQUEST_DISCOVERY_CODE)
				{
					byte[] data = request.getData();
					byte[] nameLenBytes = new byte [4];
					System.arraycopy(data, 0, nameLenBytes, 0, 4);

					int nameLen = ProtocolHeader.byteArrayToInt(nameLenBytes);
					byte[] nameBytes = new byte [nameLen];
					System.arraycopy(data, 4, nameLenBytes, 0, nameLen);
					String nodeName = new String (nameBytes, Constants.ENCODING);

					if (NameServer.getInstance().map.containsKey(NameServer.getHash(nodeName)))
					{
						// Return failure
						continue;
					}



					/*
					ProtocolHeader header = request.getHeader();
					Datagram reply;
					byte[] data = request.getData();
					byte[] length = new byte [4];

					for (int i = 0; i < 4; i++)
					{
						length[i] = data[i];
					}

					int lengthInt = ProtocolHeader.byteArrayToInt(length);
					byte[] nameArray = new byte [lengthInt];
					for (int i = 0; i < lengthInt; i++)
					{
						nameArray[i] = data[i + 4];
					}

					String name = new String (nameArray, Constants.ENCODING);
					int hash = NameServer.getHash(name);

					byte[] replyData = new byte [12];   // 3  ID's = 12 bytes

					if (NameServer.getInstance().map.containsKey(hash))
					{
						header.setReplyCode(ProtocolHeader.REPLY_DUPLICATE_ID);

						reply = new Datagram(header);
					}
					else if (NameServer.getInstance().map.containsValue(packet.getAddress().toString()))
					{
						header.setReplyCode(ProtocolHeader.REPLY_DUPLICATE_IP);

						reply = new Datagram(header);
					}
					else
					{
						header.setReplyCode(ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);
						byte[] nodeId = ProtocolHeader.intToByteArray(hash);
						byte[] prevNodeId = ProtocolHeader.intToByteArray(NameServer.getInstance().map.lowerKey(hash));
						byte[] nextNodeId = ProtocolHeader.intToByteArray(NameServer.getInstance().map.higherKey(hash));

						for (int i = 0; i < 4; i++)
						{
							replyData[i] = nodeId[i];
							replyData[i + 4] = prevNodeId[i];
							replyData[i + 8] = nextNodeId[i];
						}

						reply = new Datagram(header, replyData);
					}

					Client cl = new Client();
					cl.start();
					cl.send(packet.getAddress().toString(), discoveryPort, reply.serialize());
					cl.stop();
					*/
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
