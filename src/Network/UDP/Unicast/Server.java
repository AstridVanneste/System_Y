
package Network.UDP.Unicast;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class Server implements UDPServer, Runnable
{
	private int portNum;
	private DatagramSocket socket;
	private LinkedList<DatagramPacket> packetBuffer;
	private Thread thread;

	public Server(int portNum)
	{
		this.portNum = portNum;
		this.socket = null;
		this.packetBuffer = new LinkedList<>();
	}

	@Override
	public void start()
	{
		try
		{
			this.socket = new DatagramSocket(this.portNum);
		}
		catch(SocketException e)
		{
			System.err.println("An Exception was thrown while creating a new DatagramSocket.");
			e.printStackTrace();
		}

		thread = new Thread(this);
		thread.start();
	}

	@Override
	public int getPort()
	{
		return this.portNum;
	}

	@Override
	public void setPort(int port)
	{
		if (!this.socket.isBound())
		{
			this.portNum = port;
		}
		else
		{
			throw new RuntimeException("Tried changing the port on a server that's already running.");
		}
	}

	@Override
	public void send(String remoteHost,
	                 int port,
	                 byte[] data)
	{
		try
		{
			this.socket.send(new DatagramPacket(data, 0, data.length, InetAddress.getByName(remoteHost), port));
		}
		catch (UnknownHostException e)
		{
			System.err.println("Error host not found when trying to create packet");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.err.println("Error when sending packet");
			e.printStackTrace();
		}
		//Client.printByteArray(data);
		//System.out.println("");
	}

	@Override
	public void send(String remoteHost,
	                 int port,
	                 String data)
	{
		byte[] bytes = data.getBytes();
		this.send(remoteHost, port, bytes);

	}

	@Override
	public void send(String remoteHost,
	                 int port,
	                 List<Byte> data)
	{
		byte[] bytes = new byte[data.size()];

		for(int i = 0; i < data.size(); i++)
		{
			bytes[i] = data.get(i);
		}

		this.send(remoteHost, port, bytes);
	}

	@Override
	public byte[] receiveData()
	{
		if(this.receivePacket() != null)
		{
			return this.receivePacket().getData();
		}
		else
		{
			throw new RuntimeException("this.receivePacket() returned empty (NULL)");
		}
	}

	@Override
	public DatagramPacket receivePacket()
	{
		if(!packetBuffer.isEmpty())
		{
			DatagramPacket packet = packetBuffer.getFirst();
			packetBuffer.removeFirst();
			return packet;
		}
		else
		{
			throw new RuntimeException("Packet buffer was empty");
		}
	}

	@Override
	public void stop()
	{
		if (this.socket != null)
		{
			socket.close();
		}

	}

	@Override
	public void run()
	{
		while(!this.socket.isClosed())
		{
			byte[] buffer = new byte[1500];
			DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
			try
			{
				this.socket.receive(incomingPacket);
				this.packetBuffer.add(incomingPacket);
			}
			catch(IOException e)
			{
				System.err.println("Error when trying to receive a packet");
				e.printStackTrace();
			}
		}
	}

	public boolean isEmpty()
	{
		return packetBuffer.isEmpty();
	}
}