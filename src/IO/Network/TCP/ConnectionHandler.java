package IO.Network.TCP;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class ConnectionHandler implements Runnable
{
	private boolean running;
	private LinkedList<byte[]> packetList;
	private Thread thread;
	private DataInputStream inStream;
	private Socket socket;

	public ConnectionHandler (Socket socket)
	{
		this.running = false;
		this.packetList = new LinkedList<byte[]>();
		this.thread = new Thread(this);
		this.inStream = null;
		this.socket = socket;
	}

	public void start ()
	{
		this.running = true;

		try
		{
			this.inStream = new DataInputStream(this.socket.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		this.thread.setName("ConnectionHandler thread - " + this.socket.getRemoteSocketAddress().toString());
		this.thread.start();
	}

	public byte[] readBytes()
	{
		return this.packetList.removeFirst();
	}

	public boolean hasData ()
	{
		return this.packetList.size() > 0;
	}

	public boolean isRunning ()
	{
		return this.running;
	}

	public void write (byte[] data) throws IOException
	{
		// Does nothing, exists for compatibility reasons
		System.err.println("[IO.Network.TCP.ConnectionHandler.write(byte[])]\tWARNING: Tried to write to TCP ConnectionHandler.");
	}

	public void write (String data) throws IOException
	{
		// Does nothing, exists for compatibility reasons
		System.err.println("[IO.Network.TCP.ConnectionHandler.write(byte[])]\tWARNING: Tried to write to TCP ConnectionHandler.");
	}

	public void run()
	{
		while (this.running)
		{
			byte[] buffer = new byte [1460];

			try
			{
				int numRead = this.inStream.read(buffer);

				if (numRead > 0)
				{
					byte[] trimmedBuffer = new byte [numRead];
					System.arraycopy(buffer, 0, trimmedBuffer, 0, numRead);
					this.packetList.add(trimmedBuffer);
				}

				if (numRead == -1)
				{
					this.running = false;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void stop ()
	{
		this.running = false;

		try
		{
			this.socket.close();
			this.inStream.close();

			this.thread.join();
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
