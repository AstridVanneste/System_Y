package IO.Network.TCP;

import IO.File;
import IO.Network.Constants;
import IO.Network.Datagrams.ProtocolHeader;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Server implements TCPServer
{
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

	@Override
	public void start() throws
			IOException
	{
		this.socket = new ServerSocket(this.portNum);

		Thread ownThread = new Thread(this);
		ownThread.start();
	}

	@Override
	public void send(String remoteHost, String data)
	{
		this.send(remoteHost, data.getBytes());
	}

	@Override
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
				System.err.println("Networking.TCP.Publisher.send()\tAn Exception was thrown while trying to send data");
				ioe.printStackTrace();
			}
		}
		else
		{
			System.err.println("IO.Network.TCP.Publisher.receive()\tRemote host " + remoteHost + " was not found in active connections.");
		}
	}

	@Override
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
				System.err.println("IO.Network.TCP.Publisher.send()\tAn Exception was thrown while trying to send data");
				ioe.printStackTrace();
			}
		}
		else
		{
			System.err.println("IO.Network.TCP.Publisher.receive()\tRemote host " + remoteHost + " was not found in active connections.");
		}
	}

	@Override
	public byte[] receive(String remoteHost)
	{
		if (this.incomingConnections.containsKey(remoteHost))
		{
			return this.incomingConnections.get(remoteHost).readBytes();
		}
		else
		{
			System.err.println("IO.Network.TCP.Publisher.receive()\tRemote host " + remoteHost + " was not found in active connections.");
			return new byte[0];
		}
	}

	@Override
	public void stop() throws IOException
	{
		this.socket.close();
	}

	@Override
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
			System.err.println("IO.Network.TCP.Publisher.run()\tException was thrown in call to accept() or close()");
			ioe.printStackTrace();
		}
	}

	public Set<String> getActiveConnections ()
	{
		return this.incomingConnections.keySet();
	}

	public boolean hasData (String remoteHost)
	{
		return this.incomingConnections.get(remoteHost).hasData();
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

	@Override
	public void sendFile(String filename, String remoteHost, ProtocolHeader header)
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

					this.send(remoteHost, bytes);
				}


			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}