package IO.Network.TCP;

import IO.File;
import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.StrictMath.abs;

public class Server implements Runnable
{
	private static long TIMEOUT = 50000; //TIMEOUT IN MS (50 seconds)

	private Boolean stop;
	private int portNum;
	private ServerSocket socket;
	private HashMap<String, ConnectionHandler> incomingConnections;
	private Thread thread;

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

		this.thread = new Thread(this);
		this.thread.setName("TCP Server on " + InetAddress.getLocalHost().toString());
		this.thread.start();
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
			//System.err.println("No remote connection on " + remoteHost);
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

	public void receiveFile(String remoteHost, String filename)
	{
		File file = new File(filename);
		boolean firstSegment = true;
		boolean quit = false;
		int transactionID = -1;
		long timer = 0;
		int packets = 0;

		try
		{
			while (!quit)
			{

				if (this.hasData(remoteHost))
				{

					Datagram datagram = new Datagram(this.receive(remoteHost));

					if (datagram.getHeader().getRequestCode() == ProtocolHeader.REQUEST_FILE)
					{
						if(datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE || datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE_END)
						{

							if (firstSegment)
							{
								transactionID = datagram.getHeader().getTransactionID();
								firstSegment = false;
								file.write(datagram.getData()); //write first bytes to empty previous values at the same time
								packets++;

							}
							else if (transactionID != datagram.getHeader().getTransactionID())
							{
								throw new IOException("Transaction ID was " + transactionID + " but changed to " + datagram.getHeader().getTransactionID());
							}
							else
							{
								file.append(datagram.getData());
								packets++;

							}

							if(datagram.getHeader().getReplyCode() == ProtocolHeader.REPLY_FILE_END)
							{
								quit = true;
								System.out.println("last packet detected after " + packets + " packets");
							}
							timer = 0;
						}
						else
						{
							throw new IOException("Invalid reply code (= " + datagram.getHeader().getReplyCode() + ") should be " + ProtocolHeader.REPLY_FILE + " or " + ProtocolHeader.REPLY_FILE_END + " at the end of the file");
						}
					}
					else
					{
						throw new IOException("Invalid request code (= " + datagram.getHeader().getRequestCode() + " should be " + ProtocolHeader.REQUEST_FILE+ ")");
					}
				}
				else
				{
					if(timer == 0)
					{
						timer = System.nanoTime();
					}
					else if(((System.nanoTime() - timer) / 1000000) > TIMEOUT)
					{
						throw new IOException("Timeout when receiving '" + filename + "' from " + remoteHost);
					}
				}
			}

		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		this.stopConnectionHandler(remoteHost);
	}

	@Override
	public void run()
	{
		try
		{
			while (!this.stop)
			{
				//synchronized (this.stop)
				//{

				Socket clientSocket = this.socket.accept();

				ConnectionHandler ch = new ConnectionHandler(clientSocket);
				ch.start();

				this.incomingConnections.put(clientSocket.getRemoteSocketAddress().toString(), ch);
				//}

			}
		}
		catch (IOException ioe)
		{
			System.err.println("IO.Network.TCP.Server.run()\tIOException was thrown in call to accept() or close()");
			ioe.printStackTrace();
		}


	}

	private void stopConnectionHandler (String remoteHost)
	{

		this.incomingConnections.get(remoteHost).stop();
		this.incomingConnections.remove(remoteHost);
	}

	public void stop() throws IOException
	{
		try
		{

			for (String remoteHost : this.incomingConnections.keySet())
			{

				this.stopConnectionHandler(remoteHost);
			}


			this.stop = true;

			this.socket.close();

			this.thread.join();

		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}
}