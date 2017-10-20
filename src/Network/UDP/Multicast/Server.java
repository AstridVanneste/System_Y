package Network.UDP.Multicast;

import java.io.IOException;
import java.util.List;

public class Server implements UDPServer
{
	private String ip;
	private int portNum;

	public Server (String ip, int portNum)
	{
		this.ip = ip;
		this.portNum = portNum;
	}
	@Override
	public void start() throws IOException
	{

	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public void setPort(int port) {

	}

	@Override
	public void send(String remoteHost, int port, String data) {

	}

	@Override
	public void send(String remoteHost, int port, byte[] data) {

	}

	@Override
	public void send(String remoteHost, int port, List<Byte> data) {

	}

	@Override
	public void stop() throws IOException {

	}

	@Override
	public void run() {

	}
}