
package IO.Network.UDP.Unicast;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class Server implements Runnable
{
	private int portNum;
	private DatagramSocket socket;
	private LinkedList<DatagramPacket> packetBuffer;

	public Server(int portNum)
	{
		this.portNum = portNum;
		this.socket = null;
		this.packetBuffer = new LinkedList<>();
	}

	/**
	 * Starts the UDP server.
	 * @throws IOException
	 */
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

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Returns the port that the server is running on.<br>
	 * @return	The port that the server runs on.<br>
	 */
	public int getPort()
	{
		return this.portNum;
	}

	/**
	 *
	 * Sets the port the server is running on.<br>
	 * <b>WARNING:</b> Before the port is changed, the server needs to be stopped.<br>
	 * <b>WARNING:</b> After the port is changed, the server needs to be restarted.<br>
	 * @param	port	The new port the server should listen on.<br>
	 */
	public synchronized void setPort(int port)
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

	/**
	 * Sends all bytes in the data array.
	 * @param remoteHost	The remote host that the data should be sent to.
	 * @param data			The data to be sent.
	 */
	public synchronized void send(String remoteHost, int port, byte[] data)
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
		//Publisher.printByteArray(data);
		//System.out.println("");
	}

	/**
	 * Sends a string
	 * @param remoteHost	The remote host that the data should be sent to.
	 * @param data			The data to be sent.
	 */
	public void send(String remoteHost, int port, String data)
	{
		byte[] bytes = data.getBytes();
		this.send(remoteHost, port, bytes);

	}

	/**
	 * Sends all bytes in the data array.
	 * @param remoteHost	The remote host that the data should be sent to.
	 * @param data			The data to be sent.
	 */
	public void send(String remoteHost, int port, List<Byte> data)
	{
		byte[] bytes = new byte[data.size()];

		for(int i = 0; i < data.size(); i++)
		{
			bytes[i] = data.get(i);
		}

		this.send(remoteHost, port, bytes);
	}

	/**
	 * reads all bytes from the interal receive buffer
	 * @return	All data in the buffer for the specified host.
	 */
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

	/**
	 * read packet from internal receive buffer.
	 * @return	The first packet in the internal receive buffer.
	 */
	public synchronized DatagramPacket receivePacket()
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

	/**
	 * Stops the server
	 * @throws IOException
	 */
	public synchronized void stop()
	{
		if (this.socket != null)
		{
			socket.close();
		}
	}

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

	/**
	 * Returns true if receive buffer is empty.
	 * @return
	 */
	public boolean isEmpty()
	{
		return packetBuffer.isEmpty();
	}
}