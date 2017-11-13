package Node;

import IO.Network.TCP.Server;

import java.rmi.RemoteException;

public class FileManager implements FileManagerInterface, Runnable
{
	Server tcpServer;

	public FileManager ()
	{
		this.tcpServer = null;
	}

	public void start ()
	{
		this.tcpServer = new Server(IO.Network.Constants.FILE_RECEIVE_PORT);
	}

	@Override
	public void checkFiles() throws RemoteException
	{

	}

	@Override
	public void pullFile(short dstID, String filename) throws RemoteException
	{

	}

	@Override
	public void pushFile(String filename, int fileSize, FileType type, String remoteHost) throws RemoteException
	{

	}

	@Override
	public void run()
	{

	}
}
