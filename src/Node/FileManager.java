package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.TCP.Client;
import IO.Network.TCP.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Random;

/**
 * This class will handle everything concerning the files. To get a functional filemanager you need to call the start()
 * method and set the root folder of the files.
 */
public class FileManager implements FileManagerInterface, Runnable
{
	// Please leave the trailing '/' at the end of these constants, it denotes that they are directories
	private static final String LOCAL_FILE_PREFIX = "Local/";
	private static final String OWNED_FILE_PREFIX = "Owned/";
	private static final String DOWNLOADED_FILE_PREFIX = "Downloads/";

	private Server tcpServer;
	private String rootDirectory;
	private HashMap<String,FileLedger> files;


	public FileManager ()
	{
		this.tcpServer = null;
		this.rootDirectory = "";
		this.files = new HashMap<>();	//todo: figure out how to pass on existing fileLedgers
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

	/**
	 * Stop the filemanager
	 */
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

	/**
	 * Request to download file. The file will be pushed (push()) to the node requesting it.
	 * @param dstID
	 * @param filename
	 * @throws IOException
	 */
	@Override
	public void pullFile(short dstID, String filename) throws IOException
	{

		IO.File file = new IO.File(OWNED_FILE_PREFIX + filename);
		if(file.exists())
		{
			this.sendFile(dstID, filename, FileType.OWNED_FILE);
		}
		else
		{
			throw new IOException("No file with name " + filename + " in " + OWNED_FILE_PREFIX);
		}

	}

	/**
	 * Checks the owner of the files of a given type. When necessary it will push a file to the owner.
	 * @param type
	 * @throws RemoteException
	 */
	@Override
	public void checkFiles(FileType type) throws RemoteException
	{
		File folder = new File(this.getFullPath("",type));

		File[] fileList = folder.listFiles();

		for(File file: fileList)
		{
			short ownerId = Node.DEFAULT_ID;
			String ownerIP = "";

			try
			{
				ownerId = Node.getInstance().getResolverStub().getOwnerID(file.getName());
				ownerIP = Node.getInstance().getResolverStub().getIP(ownerId);
			}
			catch(RemoteException re)
			{
				re.printStackTrace();
			}

			try
			{
				if(type == FileType.LOCAL_FILE && ownerId == Node.getInstance().getId())
				{
					//you become the new owner of the file...
					//send replication to your previous neighbour
					Registry registry = LocateRegistry.getRegistry(ownerIP);
					FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_INTERACTION_NAME);
					fileManager.pushFile(file.getName(),file.length(),FileType.LOCAL_FILE, Node.getInstance().getResolverStub().getIP(ownerId));
				}
				else if(ownerId != Node.getInstance().getId())
				{
					Registry registry = LocateRegistry.getRegistry(ownerIP);
					FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_INTERACTION_NAME);
					fileManager.pushFile(file.getName(),file.length(),type,  Node.getInstance().getResolverStub().getIP(ownerId));
				}
			}
			catch (RemoteException re)
			{
				re.printStackTrace();
				Node.getInstance().getFailureAgent().failure(ownerId);
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch (NotBoundException e)
			{
				e.printStackTrace();
				Node.getInstance().getFailureAgent().failure(ownerId);
			}
		}
	}


	@Override
	public void pushFile(String filename, long fileSize, FileType type, String remoteHost) throws IOException
	{
		filename = this.getFullPath(filename, type);

		long bytesWritten = this.tcpServer.receiveFile(remoteHost, filename, fileSize);
		//int bytesWritten = 0;

		if (bytesWritten != fileSize)
		{
			throw new IOException ("[ERROR]\tDid not receive all (" + Long.toString(fileSize) + ") Bytes, only got " + Long.toString(bytesWritten) + " Bytes.");
		}
	}

	@Override
	public void run()
	{

	}

	/**
	 * Sends a file to a certain node.
	 * @param dstID
	 * @param filename
	 * @param type
	 */
	private void sendFile(short dstID, String filename, FileType type)
	{
		String dstIP = "";

		try
		{
			dstIP = Node.getInstance().getResolverStub().getIP(dstID);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		Random random = new Random();
		ProtocolHeader header = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION, 0, random.nextInt(),ProtocolHeader.REQUEST_FILE, ProtocolHeader.REPLY_FILE);
		Client client = new Client(dstIP,Constants.FILE_RECEIVE_PORT);
		client.sendFile(filename, header);
		int localPort =  client.getLocalPort();
		String remoteHost = "";

		try
		{
			SocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost(),localPort);

			remoteHost = socket.toString();
		}

		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}

		try
		{
			Registry reg = LocateRegistry.getRegistry(dstIP);
			FileManagerInterface fileManager = (FileManagerInterface)reg.lookup(Node.FILE_INTERACTION_NAME);
			IO.File file = new IO.File(this.getFullPath(filename,type));
			fileManager.pushFile(filename,file.size(),type,remoteHost);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			Node.getInstance().getFailureAgent().failure(dstID);
		}
		catch(NotBoundException nbe)
		{
			nbe.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Requests a certain file from the owner.
	 * @param filename
	 */
	public void requestFile(String filename)
	{
		String ownerIP = "";

		try
		{
			short ownerId = Node.getInstance().getResolverStub().getOwnerID(filename);
			ownerIP = Node.getInstance().getResolverStub().getIP(ownerId);

		}
		catch (RemoteException re)
		{
			re.printStackTrace();
		}

		try
		{
			Registry registry = LocateRegistry.getRegistry(ownerIP);
			FileManagerInterface fileManager =(FileManagerInterface) registry.lookup(Node.FILE_INTERACTION_NAME);
			fileManager.pullFile(Node.getInstance().getId(), filename);
			/*
			the owner will later call a push() method on this node to actually receive the file.
			 */
		}
		catch (RemoteException re)
		{
			re.printStackTrace();
		}
		catch (NotBoundException nbe)
		{
			nbe.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

	}


	/**
	 * Returns the full path of a file.
	 * @param filename
	 * @param type
	 * @return
	 */
	public String getFullPath(String filename, FileType type)
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

		filename += this.rootDirectory;

		return filename;
	}

	public void setRootDirectory(String rootDirectory)
	{
		this.rootDirectory = rootDirectory;
	}

	/**
	 * Returns a clear readable list of all the files of the node.
	 * @return
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("FILES:\n");
		builder.append("LOCAL:\n");

		File folder = new File(this.getFullPath("", FileType.LOCAL_FILE));

		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		builder.append("OWNED:\n");
		folder = new File(this.getFullPath("", FileType.OWNED_FILE));

		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		builder.append("DOWNLOADS:\n");
		folder = new File(this.getFullPath("", FileType.DOWNLOADED_FILE));

		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		return builder.toString();
	}
}
