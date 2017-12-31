package IO.Network.TCP;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import Node.Node;

public class Server implements Runnable
{
	private boolean running;
	private int portNum;
	private Thread thread;
	private HashMap<String, ConnectionHandler> connectionHandlers;
	private ServerSocket socket;

	public Server (int portNum)
	{
		this.running = false;
		this.portNum = portNum;
		this.thread = new Thread(this);
		this.connectionHandlers = new HashMap<String, ConnectionHandler>();
		this.socket = null;
	}

	public void start() throws IOException
	{
		this.socket = new ServerSocket(this.portNum);
		this.running = true;
		this.thread.setName("TCP Server Thread - Node: " + Node.getInstance().getName());
		this.thread.start();
	}

	public void send(String remoteHost, String data)
	{
		// Does nothing, exists to ensure compatibility
		System.err.println("[IO.Network.TCP.Server.send(String, String)]\tWARNING: Tried to send on TCP Server");
	}

	public void send(String remoteHost, byte[] data)
	{
		// Does nothing, exists to ensure compatibility
		System.err.println("[IO.Network.TCP.Server.send(String, byte[])]\tWARNING: Tried to send on TCP Server");
	}

	public void send(String remoteHost, List<Byte> data)
	{
		// Does nothing, exists to ensure compatibility
		System.err.println("[IO.Network.TCP.Server.send(String, List<Byte>)]\tWARNING: Tried to send on TCP Server");
	}

	public byte[] receive(String remoteHost)
	{
		if (this.connectionHandlers.containsKey(remoteHost))
		{
			return this.connectionHandlers.get(remoteHost).readBytes();
		}
		else
		{
			System.err.println("[IO.Network.TCP.Server.receive(String)]\tERROR: Tried to read from non-existant connection");
			return new byte [0];
		}
	}

	public Set<String> getActiveConnections ()
	{
		return this.connectionHandlers.keySet();
	}

	public boolean hasData (String remoteHost)
	{
		if (this.connectionHandlers.containsKey(remoteHost))
		{
			return this.connectionHandlers.get(remoteHost).hasData();
		}
		else
		{
			System.err.println("[IO.Network.TCP.Server.hasData(String)]\tERROR: Tried to call hasData() on non-existing socket");
			return false;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("TCP Socket on port ").append(Integer.toString(this.portNum));

		for (String remoteHost : this.connectionHandlers.keySet())
		{
			builder.append("Remote Connection to ").append(remoteHost);
			builder.append('\n');
		}

		return builder.toString();
	}

	public void receiveFile(String remoteHost, String filename)
	{
		boolean done = false;
		FileOutputStream fileStream = null;

		if (!this.connectionHandlers.containsKey(remoteHost))
		{
			System.err.println("[IO.Network.TCP.Server.receiveFile()]\tERROR: Tried to receive file on non-existant remote connection " + remoteHost);
			return;
		}

		try
		{
			fileStream = new FileOutputStream(filename);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}

		while (!done)
		{
			if (this.hasData(remoteHost))
			{
				byte[] fileBuffer = this.connectionHandlers.get(remoteHost).readBytes();

				try
				{
					fileStream.write(fileBuffer);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (!this.hasData(remoteHost) && !this.connectionHandlers.get(remoteHost).isRunning())
			{
				done = true;
				this.connectionHandlers.get(remoteHost).stop();
				this.connectionHandlers.remove(remoteHost);
			}
		}

		try
		{
			fileStream.flush();
			fileStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		while (this.running)
		{
			try
			{
				Socket clientSocket = this.socket.accept();
				String remoteHost = clientSocket.getRemoteSocketAddress().toString();
				System.out.println("Got incoming connection on " + remoteHost);
				this.connectionHandlers.put(remoteHost, new ConnectionHandler(clientSocket));
				this.connectionHandlers.get(remoteHost).start();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void stopConnectionHandler (String remoteHost)
	{
		if (this.connectionHandlers.containsKey(remoteHost))
		{
			this.connectionHandlers.get(remoteHost).stop();
		}
		else
		{
			System.err.println("[IO.Network.TCP.Server.hasData(String)]\tERROR: Tried to call stop() on non-existing socket");
		}
	}

	public void stop() throws IOException
	{
		Iterator<String> remoteHostIt = this.connectionHandlers.keySet().iterator();

		while(remoteHostIt.hasNext())
		{
			String remoteHost = remoteHostIt.next();
			this.connectionHandlers.get(remoteHost).stop();
			this.connectionHandlers.remove(remoteHost);
		}

		//System.out.println("Finished ConnectionHandlers");

		this.running = false;
		this.socket.close();

		//System.out.println("Closed socket");

		try
		{
			this.thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		//System.out.println("Joined Thread");
	}
}
