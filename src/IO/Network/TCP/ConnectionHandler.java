package IO.Network.TCP;

import IO.Network.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class ConnectionHandler implements Runnable
{
	private static final int BUFFER_SIZE = 1 << 14; // Buffer is 16K in size

	private boolean stop;
	private DataInputStream in;
	private DataOutputStream out;
	private LinkedList<byte[]> inputBuffer;
	private Socket socket;

	public ConnectionHandler (Socket socket)
	{
		System.out.println("Created ConnectionHandler on socket " + socket.getRemoteSocketAddress());
		this.stop = false;
		this.socket = socket;
		this.inputBuffer = new LinkedList<byte[]>();
	}

	public void start ()
	{
		try
		{
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
			Thread ownThread = new Thread(this);
			ownThread.start();
		}
		catch (IOException ioe)
		{
			System.err.println("IO.Network.TCP.ConnetionHandler.start()\tAn IOException was thrown while creating the Streams for the socket");
		}
	}

	public byte[] readBytes()
	{
		byte[] data = this.inputBuffer.getFirst();
		this.inputBuffer.removeFirst();
		return data;
	}

	boolean hasData ()
	{
		return this.inputBuffer.size() > 0;
	}

	public void write (byte[] data) throws IOException
	{
		this.out.write(data);
	}

	public void write (String data) throws IOException
	{
		this.out.write(data.getBytes());
	}

	public void stop ()
	{
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			System.err.println("An exception occurred while trying to close socket.");
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
					byte[] data = new byte [this.in.available()];
					int numBytes = this.in.read(data);
					this.inputBuffer.add(data);
				}
			}
			catch (IOException ioe)
			{
				System.err.println("An exception occurred while trying to read data.");
			}
		}
	}
}