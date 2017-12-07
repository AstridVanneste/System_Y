package IO.Network.TCP;

import IO.*;
import IO.File;
import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Client //implements Runnable
{
	private static long TIMEOUT = 50000; //TIMEOUT IN MS (50 seconds)

	private boolean stop;
	private int portNum;
	private String IP;
	private Socket clientSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private LinkedList<byte[]> buffer;

	public Client (String IP, int port)
	{
		this.stop = false;
		this.portNum = port;
		this.IP = IP;
		this.buffer = new LinkedList<byte[]>();
	}

	/**
	 * Returns the port that the client is running on
	 * @return
	 */
	public void start()
	{
		try
		{
			this.clientSocket = new Socket(this.IP, this.portNum);
			this.in = new DataInputStream(clientSocket.getInputStream());
			this.out = new DataOutputStream(clientSocket.getOutputStream());
			//Thread ownThread = new Thread(this);
			//ownThread.start();
		}
		catch(IOException e)
		{
			System.err.println("IO exception when starting client TCP socket");
			e.printStackTrace();
		}
	}

	/**
	 * Sends all bytes in the data array
	 * @param data
	 */
	public void send(byte[] data)
	{
		try
		{
			//System.out.println("writing " + data.length + " bytes to outputStream");
			this.out.write(data);
		}
		catch(IOException e)
		{
			System.err.println("Error when writing data to outputstream");
			e.printStackTrace();
		}
	}

	/**
	 * Sends all bytes in the data list.
	 * @param data
	 */
	public void send(List<Byte> data)
	{
		byte[] arrayData = new byte[data.size()];

		for (int i = 0; i < data.size(); i++)
		{
			arrayData[i] = data.get(i);
		}

		try
		{
			out.write(arrayData);
		}
		catch(IOException e)
		{
			System.err.println("Error when writing data to outputstream");
			e.printStackTrace();
		}
	}

	/**
	 * reads all bytes from the interal receive buffer
	 * @return
	 */
	public synchronized byte[] receive()
	{
		byte[] data = this.buffer.getFirst();
		this.buffer.removeFirst();

		return data;
	}

	/**
	 * Stops the client
	 */
	public void stop()
	{
		try
		{
			this.stop = true;
			clientSocket.close();
		}
		catch(IOException e)
		{
			System.err.println("Error when closing client socket");
			e.printStackTrace();
		}
	}

	/**
	 * Send complete file
	 * @param filename
	 */
	public void sendFile(String filename)
	{
		Random random = new Random();
		ProtocolHeader header = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION,0,random.nextInt(),ProtocolHeader.REQUEST_FILE,ProtocolHeader.REPLY_FILE);
		File file = new File(filename);
		try
		{
			int length = 0;
			while(file.available() > 0)
			{

				if(file.available() > Constants.MAX_TCP_FILE_SEGMENT_SIZE)
				{
					Datagram data = new Datagram(header, file.read(Constants.MAX_TCP_FILE_SEGMENT_SIZE));
					this.send(data.serialize());
					length++;

				}
				else
				{
					System.out.println("setting end reply code");
					header.setReplyCode(ProtocolHeader.REPLY_FILE_END);
					Datagram data = new Datagram(header,file.read((int) file.available()));

					this.send(data.serialize());
					length++;
					System.out.println(length + " packets send");
				}


			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Returns true if there is data in the buffer.
	 * @return
	 */
	public boolean hasData()
	{
		try
		{
			return this.in.available() > 0;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			return false;
		}
	}


	/*
	public void run()
	{
		while (!this.stop)
		{
			try
			{
				if (this.hasData())
				{
					System.out.println("CLIENT " + this.in.available() + " Bytes available to TCP");
					byte[] data = new byte [this.in.available()];
					int numBytes = this.in.read(data);

					this.buffer.add(data);

					System.out.println("CLIENT Added " + data.length + " Bytes to internal buffer.");
				}
			}
			catch (IOException ioe)
			{
				System.err.println("TCPClient: An exception occurred while trying to read data.");
			}
		}
	}
	*/

	/**
	 * Returns the local port from where the client will send data
	 * @return
	 */
	public int getLocalPort()
	{
		return this.clientSocket.getLocalPort();
	}
}