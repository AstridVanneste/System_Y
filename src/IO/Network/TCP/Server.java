package IO.Network.TCP;

import IO.File;
import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.lang.StrictMath.abs;

public class Server implements Runnable
{
	private static long TIMEOUT = 50000; //TIMEOUT IN MS (50 seconds)

	private boolean stop;
	private int portNum;
	private ServerSocket socket;
	private HashMap<String, ConnectionHandler> incomingConnections;

	public Server (int port)
	{
		this.stop = false;
		this.portNum = port;
		this.socket = null;
		this.incomingConnections = new HashMap<String, ConnectionHandler> ();
	}

	public void start() throws IOException
	{
		this.socket = new ServerSocket(this.portNum);

		Thread ownThread = new Thread(this);
		ownThread.start();
	}

	public void send(String remoteHost, String data)
	{
		this.send(remoteHost, data.getBytes());
	}

	public void send(String remoteHost, byte[] data)
	{
		if (this.incomingConnections.containsKey(remoteHost))
		{
			try
			{
				this.incomingConnections.get(remoteHost).write(data);
			}
			catch (IOException ioe)
			{
				System.err.println("Networking.TCP.Server.send()\tAn Exception was thrown while trying to send data");
				ioe.printStackTrace();
			}
		}
		else
		{
			System.err.println("IO.Network.TCP.Server.receive()\tRemote host " + remoteHost + " was not found in active connections.");
		}
	}

	public void send(String remoteHost, List<Byte> data)
	{
		if (this.incomingConnections.containsKey(remoteHost))
		{
			byte[] dataArray = new byte [data.size()];
			int i = 0;

			for (byte b : data)
			{
				dataArray[i] = b;
				i++;
			}

			try
			{
				this.incomingConnections.get(remoteHost).write(dataArray);
			}
			catch (IOException ioe)
			{
				System.err.println("IO.Network.TCP.Server.send()\tAn Exception was thrown while trying to send data");
				ioe.printStackTrace();
			}
		}
		else
		{
			System.err.println("IO.Network.TCP.Server.receive()\tRemote host " + remoteHost + " was not found in active connections.");
		}
	}

	public byte[] receive(String remoteHost)
	{
		if (this.incomingConnections.containsKey(remoteHost))
		{
			return this.incomingConnections.get(remoteHost).readBytes();
		}
		else
		{
			System.err.println("IO.Network.TCP.Server.receive()\tRemote host " + remoteHost + " was not found in active connections.");
			return new byte[0];
		}
	}

	public synchronized void stop() throws IOException
	{
		this.socket.close();
	}

	public void run()
	{
		try
		{
			while (!this.stop && !this.socket.isClosed())
			{
				Socket clientSocket = this.socket.accept();
				ConnectionHandler ch = new ConnectionHandler(clientSocket);
				ch.start();

				this.incomingConnections.put(clientSocket.getRemoteSocketAddress().toString(), ch);
			}
		}
		catch (IOException ioe)
		{
			System.err.println("IO.Network.TCP.Server.run()\tException was thrown in call to accept() or close()");
			ioe.printStackTrace();
		}
	}

	public Set<String> getActiveConnections ()
	{
		return this.incomingConnections.keySet();
	}

	public boolean hasData (String remoteHost)
	{
		if(this.getActiveConnections().contains(remoteHost))
		{
			return this.incomingConnections.get(remoteHost).hasData();
		}
		else
		{
			System.out.println("No remote connection on " + remoteHost);
			return false;
		}
	}

	public String toString()
	{
		StringBuilder resBuilder = new StringBuilder();
		resBuilder.append("TCP Server listening on port ");
		resBuilder.append(this.portNum);
		resBuilder.append(" (TCP)");
		resBuilder.append('\n');
		resBuilder.append("Running: ");
		resBuilder.append(this.stop);
		resBuilder.append('\n');
		resBuilder.append("Connected Clients:");
		resBuilder.append('\n');

		if (this.incomingConnections.size() > 0)
		{
			for (String remoteHost : this.incomingConnections.keySet())
			{
				resBuilder.append(remoteHost);
				resBuilder.append('\n');
			}
		}
		else
		{
			resBuilder.append("None");
		}

		return resBuilder.toString();
	}

	public void sendFile(String remoteHost, String filename, ProtocolHeader header)
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

					this.send(remoteHost,bytes);
				}
				else
				{
					byte[] bytes = new byte[ProtocolHeader.HEADER_LENGTH + (int) file.available()];

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
					}

					this.send(remoteHost, bytes);
				}
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public long receiveFile(String remoteHost, String filename, long fileLength)
	{
		File file = new File(filename);
		boolean firstSegment = true;
		boolean timeout = false;
		int transactionID = -1;
		long timer = 0;
		long offset = 0;

		System.out.println("Receiving file " + filename);

		try
		{
			while (!(offset >= fileLength) && !timeout)
			{
				System.out.println("While-check passed, filelength: " + Long.toString(fileLength));
				if (this.hasData(remoteHost))
				{
					System.out.println("Had data for given remote host");

					Datagram datagram = new Datagram(this.receive(remoteHost));

					if (datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE && datagram.getHeader().getRequestCode() == ProtocolHeader.REQUEST_FILE)
					{
						if (firstSegment)
						{
							System.out.println("First Segment");
							transactionID = datagram.getHeader().getTransactionID();
							firstSegment = false;
							file.write(datagram.getData()); //write first bytes to empty previous values at the same time
						}
						else if (transactionID != datagram.getHeader().getTransactionID())
						{
							throw new IOException("Transaction ID was " + transactionID + " but changed to " + datagram.getHeader().getTransactionID());
						}
						else
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
					System.out.println("Entering time-out routine");
					if(timer == 0)
					{
						System.out.println("Set timer to 0");
						timer = System.nanoTime();
					}
					else if(((System.nanoTime() - timer)/1000000) > TIMEOUT)
					{
						System.out.println("Reached time-out");
						timeout = true;
					}
				}
			}

			this.stopConnectionHandler(remoteHost);
			//this.incomingConnections.remove(remoteHost);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return offset;
	}

	public void stopConnectionHandler (String remoteHost)
	{
		this.incomingConnections.get(remoteHost).stop();
		this.incomingConnections.remove(remoteHost);
	}
}