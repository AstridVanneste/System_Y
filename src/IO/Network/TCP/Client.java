package IO.Network.TCP;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Client
{
	//private static final long TIMEOUT_MS = 50000;

	private int portNum;
	private String ip;
	private Socket socket;
	private DataOutputStream outStream;

	public Client (String ip, int portNum)
	{
		this.ip = ip;
		this.portNum = portNum;
		this.socket = null;
		this.outStream = null;
	}

	public void start ()
	{
		try
		{
			this.socket = new Socket(this.ip, this.portNum);
			this.outStream = new DataOutputStream(this.socket.getOutputStream());
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void stop ()
	{
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void sendFile (String filename)
	{
		try
		{
			FileInputStream stream = new FileInputStream(filename);

			while (stream.available() > 0)
			{
				byte[] fileBuffer = new byte[1460];
				int bytesRead = stream.read(fileBuffer);

				byte[] trimmedBuffer = new byte [bytesRead];
				System.arraycopy(fileBuffer, 0, trimmedBuffer, 0, bytesRead);

				this.outStream.write(trimmedBuffer, 0, trimmedBuffer.length);
			}

			stream.close();
			this.outStream.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void send(byte[] data)
	{
		try
		{
			this.outStream.write(data);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void send(List<Byte> data)
	{
		// Does nothing, exists to ensure compatibility
		System.err.println("[IO.Network.TCP.Client.send(List<Byte> data)]\tWARNING: Called dummy method on client socket: " + ip + ":" + Integer.toString(this.portNum) + "\n[IO.Network.TCP.Client.send(List<Byte> data)]\tParameters: data: " + data.toString());
	}

	public boolean hasData ()
	{
		System.err.println("[IO.Network.TCP.Client.hasData()]\tWARNING: Called dummy method on client socket: " + ip + ":" + Integer.toString(this.portNum));
		return true;
	}

	public int getLocalPort ()
	{
		return this.portNum;
	}
}