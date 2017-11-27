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

public class Client implements Runnable
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
			Thread ownThread = new Thread(this);
			ownThread.start();
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
	 * @param header
	 */
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

					/*for(byte b: header.serialize())
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
					*/

					Datagram data = new Datagram(header, file.read(Constants.MAX_TCP_FILE_SEGMENT_SIZE));
					this.send(data.serialize());

				}
				else
				{
					/*byte[] bytes = new byte[ProtocolHeader.HEADER_LENGTH + (int) file.available()];

					int i = 0;

					for(byte b: header.serialize())
					{
						bytes[i] = b;
						i++;
					}

					for(byte b: file.read((int) file.available()))
					{
						bytes[i] = b;
						i++;
					}*/

					//System.out.println("last packet has " + file.available() + " bytes");

					Datagram data = new Datagram(header,file.read((int) file.available()));

					this.send(data.serialize());
				}


			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Receives complete file and writes to given path
	 * @param filename
	 */
	public long receiveFile(String filename, long fileLength)
	{
		File file = new File(filename);
		boolean firstSegment = true;
		boolean timeout = false;
		int transactionID = -1;
		long timer = 0;
		long offset = 0;

		try
		{
			while (!(offset >= fileLength) && !timeout)
			{
				if (this.hasData())
				{
					Datagram datagram = new Datagram(this.receive());
					if (datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE && datagram.getHeader().getRequestCode() == ProtocolHeader.REQUEST_FILE)
					{


						if (firstSegment)
						{
							transactionID = datagram.getHeader().getTransactionID();
							firstSegment = false;
							file.write(datagram.getData()); //write first bytes to empty previous values at the same time
						} else if (transactionID != datagram.getHeader().getTransactionID())
						{
							throw new IOException("Transaction ID was " + transactionID + " but changed to " + datagram.getHeader().getTransactionID());
						} else
						{
							file.append(datagram.getData());
						}
						offset += datagram.getData().length;
						timer = 0;
					}
					else
					{
						throw new IOException("Invalid reply code (= " + datagram.getHeader().getReplyCode() + "should be " + ProtocolHeader.REPLY_FILE+ ") or request code (= " + datagram.getHeader().getRequestCode() + " should be " + ProtocolHeader.REQUEST_FILE + ")");
					}
				}
				else
				{
					if(timer == 0)
					{
						timer = System.nanoTime();
					}
					else if(((System.nanoTime() - timer)/1000000) > TIMEOUT)
					{
						timeout = true;
					}
				}
			}

		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return offset;

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

	/**
	 * Returns the local port from where the client will send data
	 * @return
	 */
	public int getLocalPort()
	{
		return this.clientSocket.getLocalPort();
	}
}