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

public class Client implements TCPClient, Runnable
{
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

	@Override
	public void start()
	{
		try
		{
			this.clientSocket = new Socket(this.IP, this.portNum);
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			Thread ownThread = new Thread(this);
			ownThread.start();
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
		byte[] data = this.buffer.getFirst();
		this.buffer.removeFirst();

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
	public void sendFile(String filename, ProtocolHeader header)
	{
		File file = new File(filename);
		try
		{

			while(file.available() != 0)
			{
				int length;

				if(file.available() > Constants.MAX_TCP_FILE_SEGMENT_SIZE)
				{
					byte[] bytes = new byte[Constants.MAX_TCP_SEGMENT_SIZE];

					int i = 0;

					for(byte b: header.serialize())
					{
						bytes[i] = b;
						i++;
					}

					for(byte b: file.read(Constants.MAX_TCP_FILE_SEGMENT_SIZE))
					{
						bytes[i] = b;
						i++;
					}

					this.send(bytes);
				}
				else
				{
					byte[] bytes = new byte[ProtocolHeader.HEADER_LENGTH + file.available()];

					int i = 0;

					for(byte b: header.serialize())
					{
						bytes[i] = b;
						i++;
					}

					for(byte b: file.read(file.available()))
					{
						bytes[i] = b;
						i++;
					}

					this.send(bytes);
				}


			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	@Override
	public void receiveFile(String filename, int transactionID)
	{
		File file = new File(filename);

		try
		{
			while (this.hasData())
			{
				Datagram datagram = new Datagram(this.receive());

				if (datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE)
				{
					if (datagram.getHeader().getTransactionID() == transactionID)
					{
						file.append(datagram.getData());
					}
				}
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}

	}

	@Override
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

	@Override
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
				System.err.println("An exception occurred while trying to read data.");
			}
		}
	}
}