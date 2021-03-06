package IO.Network.UDP.Multicast;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

/**
 * Created by Astrid on 09-Oct-17.
 */
public class Subscriber implements Runnable
{
	private int portNum;
	private String ip;
	private MulticastSocket socket;
	private LinkedList<DatagramPacket> packetBuffer;

	public Subscriber(String ip, int portNum)
	{
		this.portNum = portNum;
		this.ip = ip;
		this.socket = null;
		this.packetBuffer = new LinkedList<DatagramPacket>();
	}

	/**
	 * Start Multicast Subscriber
	 */
	public void start()
	{
		try
		{
			this.socket = new MulticastSocket(this.portNum);
			//this.socket = new MulticastSocket();
			this.socket.setReceiveBufferSize(1 << 17);
			this.socket.joinGroup(InetAddress.getByName(this.ip));

			Thread t = new Thread (this);
			t.start();
		}
		catch (IOException ioe)
		{
			System.err.println("An Exception was thrown while creating a MulticastSocket.");
			ioe.printStackTrace();
		}
	}

	/**
	 * reads all bytes from the interal receive buffer
	 * @return	All data in the buffer for the specified host.
	 */
	public byte[] receiveData()
	{
		return this.receivePacket().getData();
	}

	/**
	 * read packet from internal receive buffer.
	 * @return				The first packet in the internal receive buffer.
	 */
	public synchronized DatagramPacket receivePacket()
	{
		if(!this.packetBuffer.isEmpty())
		{
			DatagramPacket packet = this.packetBuffer.get(0);
			this.packetBuffer.remove(0);
			return packet;
		}
		else
		{
			throw new RuntimeException("Packet buffer was empty");
		}
	}

	/**
	 * Stops the Multicast Subscriber.
	 */
	public synchronized void stop()
	{
		if(this.socket != null)
		{
			try
			{
				this.socket.leaveGroup(InetAddress.getByName(this.ip));
				this.socket.close();
			}
			catch (IOException ioe)
			{
				System.err.println("An Exception was thrown while trying to leave a multicast group");
				ioe.printStackTrace();
			}
		}
	}

	@Override
	public void run()
	{
		while (!this.socket.isClosed())
		{

			byte[] buffer = new byte[500];
			DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
			try
			{
				this.socket.receive(incomingPacket);

				//this.packetBuffer.add(incomingPacket);


				byte[] actualData = new byte[incomingPacket.getLength()];
				System.arraycopy(incomingPacket.getData(), 0, actualData, 0, incomingPacket.getLength());

				DatagramPacket trimmedPacket = incomingPacket;
				trimmedPacket.setData(actualData);



				this.packetBuffer.add(trimmedPacket);
			}
			catch (SocketException se)
			{
				se.printStackTrace();
			}
			catch (IOException e)
			{
				System.err.println("Error when trying to receive a packet");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns true if the receiveBuffer contains data.
	 * @return
	 */
	public boolean hasData()
	{
		return !this.packetBuffer.isEmpty();
	}

	/**
	 * Returns the amount of data present in the receiveBuffer.
	 * @return
	 */
	public int getBufferLength()
	{
		return this.packetBuffer.size();
	}
}