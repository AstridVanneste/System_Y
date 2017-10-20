package Network.UDP.Multicast;

import java.io.IOException;
import java.net.DatagramPacket;

public class Client implements UDPClient
{
	@Override
	public void start()
	{

	}

	@Override
	public byte[] receiveData()
	{
		return new byte[0];
	}

	@Override
	public DatagramPacket receivePacket()
	{
		return null;
	}

	@Override
	public void stop() throws IOException
	{

	}

	@Override
	public void run()
	{

	}
}