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

	public byte[] receiveData()
	{
		return this.receivePacket().getData();
	}

	public DatagramPacket receivePacket()
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

	public void stop()
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
		while(!this.socket.isClosed())
		{
			//System.out.println("Socket isn't closed, packetbuffer size: " + Integer.toString(this.packetBuffer.size()) + " Received on " + this.ip + ":" + this.portNum);
			byte[] buffer = new byte[500];
			DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
			try
			{
				this.socket.receive(incomingPacket);
				//System.out.println("Received incoming packet, size: "  + Integer.toString(this.packetBuffer.size()) + " Received on " + this.ip + ":" + this.portNum);
				//this.packetBuffer.add(incomingPacket);
				//System.out.println("Added to buffer, size: " + Integer.toString(this.packetBuffer.size()) + " Received on " + this.ip + ":" + this.portNum);

				byte[] actualData = new byte [incomingPacket.getLength()];
				System.arraycopy(incomingPacket.getData(), 0, actualData, 0, incomingPacket.getLength());

				DatagramPacket trimmedPacket = incomingPacket;
				trimmedPacket.setData(actualData);

				this.packetBuffer.add(trimmedPacket);
			}
			catch (SocketException se)
			{
				se.printStackTrace();
			}
			catch(IOException e)
			{
				System.err.println("Error when trying to receive a packet");
				e.printStackTrace();
		}
		}
	}

	public boolean hasData()
	{
		return !this.packetBuffer.isEmpty();
	}

	public int getBufferLength()
	{
		return this.packetBuffer.size();
	}
}