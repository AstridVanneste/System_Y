package NameServer;

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
				String remoteHost = packet.getAddress().toString();

			}
		}
	}

	public void stop ()
	{
		this.quit = true;
		this.multicastSub.stop();
	}
}
