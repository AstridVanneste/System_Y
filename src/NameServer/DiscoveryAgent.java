package NameServer;

import Network.Datagrams.Datagram;
import Network.Datagrams.ProtocolHeader;
import Network.UDP.Multicast.Subscriber;

import java.net.DatagramPacket;

public class DiscoveryAgent implements Runnable
{
	public static String discoveryMulticast = "224.0.0.1";
	public static int discoveryPort = 1997;

	private boolean quit;
	private Subscriber multicastSub;

	public DiscoveryAgent ()
	{
		this.quit = false;
		this.multicastSub = new Subscriber(discoveryMulticast, discoveryPort);
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
					ProtocolHeader header = request.getHeader();
					byte[] data = request.getData();
					byte[] length = new byte [4];

					for (int i = 0; i < 4; i++)
					{
						length[i] = data[i];
					}

					int lengthInt = ProtocolHeader.byteArrayToInt(length);

					//int hash = NameServer.getHash();
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
