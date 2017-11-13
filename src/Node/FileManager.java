package Node;

import IO.Network.TCP.Server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;

public class FileManager implements FileManagerInterface, Runnable
{
	// Please leave the trailing '/' at the end of these constants, it denotes that they are directories
	private static final String LOCAL_FILE_PREFIX = "Local/";
	private static final String OWNED_FILE_PREFIX = "Owned/";
	private static final String DOWNLOADED_FILE_PREFIX = "Downloads/";

	private Server tcpServer;

	public FileManager ()
	{
		this.tcpServer = null;
	}

	public void start ()
	{
		try
		{
			this.tcpServer = new Server(IO.Network.Constants.FILE_RECEIVE_PORT);
			this.tcpServer.start();
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
			if (this.tcpServer != null)
			{
				this.tcpServer.stop();
			}
			else
			{
				System.err.println("[ERROR]\tTried to stop FileManager when TCP server was NULL");
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
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
	public void pushFile(String filename, int fileSize, FileType type, String remoteHost) throws IOException
	{
		switch (type)
		{
			case LOCAL_FILE:
				filename = LOCAL_FILE_PREFIX + filename;
				break;

			case OWNED_FILE:
				filename = OWNED_FILE_PREFIX + filename;
				break;

			case DOWNLOADED_FILE:
				filename = DOWNLOADED_FILE_PREFIX + filename;
				break;

			default:
				throw new InvalidParameterException ("File Type " + type.toString() +  " is not a valid filetype, possibilities are LOCAL_FILE, OWNED_FILE and DOWNLOADED_FILE.");
		}

		//int bytesWritten = this.tcpServer.receiveFile(remoteHost, filename, fileSize);
		int bytesWritten = 0;

		if (bytesWritten != fileSize)
		{
			throw new IOException ("[ERROR]\tDid not receive all (" + Integer.toString(fileSize) + ") Bytes, only got " + Integer.toString(bytesWritten) + " Bytes.");
		}
	}

	@Override
	public void run()
	{

	}
}
