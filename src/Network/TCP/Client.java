package Network.TCP;

import Network.Constants;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Client implements TCPClient, Runnable
{
	private boolean stop;
	private int portNum;
	private String IP;
	private Socket clientSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private LinkedList<Byte> buffer;
	private Thread ownThread;

	public Client(String IP)
	{
		this.stop = false;
		this.portNum = Constants.TCP_PORT;
		this.IP = IP;
		this.buffer = new LinkedList<Byte>();
	}

	public Client(String IP,
	              int port)
	{
		this.stop = false;
		this.portNum = port;
		this.IP = IP;
		this.buffer = new LinkedList<Byte>();
	}

	@Override
	public void start()
	{
		try
		{
			this.clientSocket = new Socket(this.IP, this.portNum);
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			this.ownThread = new Thread(this);
			this.ownThread.start();
		}
		catch(IOException e)
		{
			System.err.println("IO exception when starting client TCP socket");
			e.printStackTrace();
		}
	}

	@Override
	public void send(byte[] data)
	{
		try
		{
			out.write(data);
		}
		catch(IOException e)
		{
			System.err.println("Error when writing data to outputstream");
			e.printStackTrace();
		}
	}

	@Override
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

	@Override
	public byte[] receive()
	{
		byte[] data = new byte [this.buffer.size()];

		for (int i = 0; i < this.buffer.size(); i++)
		{
			data[i] = this.buffer.get(i);
		}

		return data;
	}

	@Override
	public byte[] receive(int numBytes)
	{
		byte[] data = new byte[numBytes];

		for (int i = 0; i < numBytes; i++)
		{
			data[i] = this.buffer.get(i);
		}

		return data;
	}

	@Override
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

	@Override
	public void run()
	{
		while (!this.stop)
		{
			try
			{
				if (this.in.available() > 0)
				{
					System.out.println("CLIENT " + this.in.available() + " Bytes available to TCP");
					byte[] data = new byte [this.in.available()];
					int numBytes = this.in.read(data);

					for (int i = 0; i < numBytes; i++)
					{
						this.buffer.add(data[i]);
					}

					System.out.println("CLIENT Added " + data.length + " Bytes to internal buffer.");
				}
			}
			catch (IOException ioe)
			{
				System.err.println("An exception occurred while trying to read data.");
			}
		}
	}
}
