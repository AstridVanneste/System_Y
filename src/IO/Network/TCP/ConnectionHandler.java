package IO.Network.TCP;

import IO.Network.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class ConnectionHandler implements Runnable
{
	private boolean stop;
	private DataInputStream in;
	private DataOutputStream out;
	private LinkedList<byte[]> inputBuffer;
	private Socket socket;
	private Thread thread;

	public ConnectionHandler (Socket socket)
	{
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
			this.thread = new Thread(this);
			this.thread.setName("ConnectionHandler to " + this.socket.getRemoteSocketAddress().toString());
			this.thread.start();
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
			this.stop = true;
			this.in.close();
			this.out.close();
			this.socket.close();
			this.thread.join();
		}
		catch (IOException ioe)
		{
			System.err.println("An exception was thrown while trying to close socket.");
			ioe.printStackTrace();
		}
		catch (InterruptedException ie)
		{
			System.err.println("An exception was thrown while trying to join thread");
			ie.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		while (!this.stop && !this.socket.isClosed())
		{
			try
			{
				if (this.in.available() > 0)
				{
					byte[] data = new byte[this.in.available()];
					int numBytes = this.in.read(data);

					if (numBytes > 0)
					{
						this.inputBuffer.add(data);
					}
				}
			}
			catch (IOException ioe)
			{
				//System.err.println("ConnectionHandler: An exception occurred while trying to read data: '" + ioe.getMessage() + "'");
				//ioe.printStackTrace();
			}
		}
	}
}