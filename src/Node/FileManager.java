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
import java.util.Map;
import java.util.Random;

/**
 * This class will handle everything concerning the files.
 * To get a functional filemanager you need to set the root directory and then call the start() method.
 */
public class FileManager implements FileManagerInterface
{
	// Please leave the trailing '/' at the end of these constants, it denotes that they are directories
	private static final String LOCAL_FILE_PREFIX = "Local\\";
	private static final String OWNED_FILE_PREFIX = "Owned\\";
	private static final String DOWNLOADED_FILE_PREFIX = "Downloads\\";
	private static final String REPLICATED_FILE_PREFIX = "Replicated\\";

	private Server tcpServer;
	private String rootDirectory;
	private HashMap <String, FileLedger> fileLedgers;
	private boolean running;

	public FileManager ()
	{
		this.tcpServer = null;
		this.rootDirectory = System.getProperty("user.home");
		this.fileLedgers = new HashMap<>();
		this.running = false;
	}

	/**
	 * Starts the filemanager:
	 * - starts TCP server
	 * - creates all necessary folders for the filemanager functions (/LOCAL, /OWNED and /DOWNLOADS)
	 */
	public void start ()
	{
		this.running = true;

		try
		{
			this.tcpServer = new Server(IO.Network.Constants.FILE_RECEIVE_PORT);
			this.tcpServer.start();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		File folder = new File(getFullPath("",FileType.LOCAL_FILE));
		if(!folder.exists())
		{
			folder.mkdir();
		}

		folder = new File(getFullPath("",FileType.OWNED_FILE));
		if(!folder.exists())
		{
			folder.mkdir();
		}

		folder = new File(getFullPath("",FileType.DOWNLOADED_FILE));
		if(!folder.exists())
		{
			folder.mkdir();
		}

		folder = new File(getFullPath("",FileType.REPLICATED_FILE));
		if(!folder.exists())
		{
			folder.mkdir();
		}

		//start replicating files.
		System.out.println("Starting to replicate files");
		try
		{
			this.checkFiles(FileType.LOCAL_FILE);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Stop the filemanager:
	 * - stops TCP server
	 * - deletes all owned files.
	 * - deletes all replicated files.
	 */
	public void stop ()
	{
		if(this.running)
		{
			this.shutdown();
		}

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

		File folder = new File(getFullPath("",FileType.OWNED_FILE));

		for(File file: folder.listFiles())
		{
			file.delete();
		}


		folder = new File(getFullPath("",FileType.REPLICATED_FILE));

		for(File file: folder.listFiles())
		{
			file.delete();
		}

	}

	public void shutdown ()
	{
		for (Map.Entry<String, FileLedger> pair : this.fileLedgers.entrySet())
		{
			if (pair.getValue().getOwnerID() == Node.getInstance().getId())
			{
				// I'm the owner
				// Copy the file to my previous
				// Tell him he's the new owner
				this.sendFile(Node.getInstance().getPreviousNeighbour(), pair.getKey(), FileType.OWNED_FILE, FileType.OWNED_FILE);
			}
		}

		String localFolder = this.getFolder(FileType.LOCAL_FILE);

		for (File localFile : (new File(localFolder)).listFiles())
		{
			// The file is local to my system
			// Find the owner
			// Notify the owner that I'm leaving

			short ownerID = -1;

			try
			{
				ownerID = Node.getInstance().getResolverStub().getOwnerID(localFile.getName());
				Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(ownerID));
				FileManagerInterface ownerInterface = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
				ownerInterface.notifyLeaving(localFile.getName(), FileType.LOCAL_FILE);
			}
			catch (RemoteException | NotBoundException e)
			{
				Node.getInstance().getFailureAgent().failure(ownerID);
				e.printStackTrace();
			}
		}

		String replicatedFolder = this.getFolder(FileType.LOCAL_FILE);

		for (File replicatedFile : (new File(localFolder)).listFiles())
		{
			// The file is replicated to my system
			// Find the owner
			// Notify the owner that I'm leaving

			short ownerID = -1;

			try
			{
				ownerID = Node.getInstance().getResolverStub().getOwnerID(replicatedFile.getName());
				Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(ownerID));
				FileManagerInterface ownerInterface = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
				ownerInterface.notifyLeaving(replicatedFile.getName(), FileType.REPLICATED_FILE);
			}
			catch (RemoteException | NotBoundException e)
			{
				Node.getInstance().getFailureAgent().failure(ownerID);
				e.printStackTrace();
			}
		}

		this.running = false;
	}

	public void notifyLeaving (String filename, FileType type)
	{
		String fullPath = this.getFullPath(filename, FileType.OWNED_FILE);

		if ((this.fileLedgers.get(filename).getNumDownloads() > 0) && (type == FileType.LOCAL_FILE))
		{
			File fileObj = new File (fullPath);
			fileObj.delete();
			this.fileLedgers.remove(filename);
		}
		else
		{
			if(type == FileType.LOCAL_FILE)
			{
				this.fileLedgers.get(filename).setLocalID(Node.DEFAULT_ID);
			}

			if(type == FileType.LOCAL_FILE || type == FileType.REPLICATED_FILE)
			{
				this.fileLedgers.get(filename).setReplicatedId(Node.getInstance().getPreviousNeighbour());
				this.sendFile(Node.getInstance().getPreviousNeighbour(), filename, FileType.OWNED_FILE, FileType.REPLICATED_FILE);
			}
		}
	}

	@Override
	public void deleteFile(String filename, FileType type) throws IOException
	{
		File file = new File(this.getFullPath(filename,type));
		if(file.exists())
		{
			file.delete();
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
			this.sendFile(dstID, filename, FileType.OWNED_FILE, FileType.DOWNLOADED_FILE);
		}
		else
		{
			throw new IOException("No file with name " + filename + " in " + OWNED_FILE_PREFIX);
		}

		this.fileLedgers.get(filename).addDownloader(dstID); //add person who requests to downloaders
	}

	@Override
	public void addFileLedger(FileLedger fileLedger) throws IOException
	{
		if(this.fileLedgers.containsKey(fileLedger.getFileName()))
		{
			throw new IOException("Already have a fileLedger with filename " + fileLedger.getFileName());
		}
		else
		{
			this.fileLedgers.put(fileLedger.getFileName(), fileLedger);
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
					//send replication to your previous neighbour. this node becomes the owner of the file
					System.out.println("OWNER IS SAME AS LOCAL");
					String replicatedIP = Node.getInstance().getResolverStub().getIP(Node.getInstance().getPreviousNeighbour());
					Registry registry = LocateRegistry.getRegistry(replicatedIP);
					FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);

					this.sendFile(Node.getInstance().getPreviousNeighbour(),file.getName(),FileType.LOCAL_FILE, FileType.REPLICATED_FILE);
					FileLedger fileLedger = new FileLedger(file.getName(),Node.getInstance().getId(), Node.getInstance().getId(), Node.getInstance().getPreviousNeighbour());
					this.addFileLedger(fileLedger);
				}
				else if(ownerId != Node.getInstance().getId())
				{
					Registry registry = LocateRegistry.getRegistry(ownerIP);
					FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);

					System.out.println("sending file to " + ownerId + " filename: " + file.getName());
					this.sendFile(ownerId,file.getName(),FileType.LOCAL_FILE,FileType.OWNED_FILE );

					if(type == FileType.LOCAL_FILE)
					{
						FileLedger fileLedger = new FileLedger(file.getName(), Node.getInstance().getId(), ownerId, Node.DEFAULT_ID);
						fileManager.addFileLedger(fileLedger);
					}
					else if(type == FileType.OWNED_FILE)
					{
						FileLedger fileLedger = this.fileLedgers.get(file.getName());
						if(!(fileLedger.getReplicatedId() == Node.DEFAULT_ID))
						{
							String IP = Node.getInstance().getResolverStub().getIP(fileLedger.getReplicatedId());
							Registry reg = LocateRegistry.getRegistry(IP);
							FileManagerInterface stub = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
							stub.deleteFile(file.getName(), FileType.REPLICATED_FILE);
						}
						fileLedger.setOwnerID(ownerId);
						fileManager.addFileLedger(fileLedger);
						file.delete();
						this.fileLedgers.remove(file.getName());
					}

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
		System.out.println("receiving file of type " + type);
		filename = this.getFullPath(filename, type);

		this.tcpServer.receiveFile(remoteHost, filename);

	}

	/**
	 * Sends a file to a certain node.
	 * @param dstID
	 * @param filename
	 * @param srcType
	 */
	public void sendFile(short dstID, String filename, FileType srcType, FileType dstType)
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

		Client client = new Client(dstIP,Constants.FILE_RECEIVE_PORT);
		client.start();
		client.sendFile(this.getFullPath(filename,srcType));
		int localPort =  client.getLocalPort();
		String remoteHost = "";

		try
		{
			SocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),localPort);

			remoteHost = socket.toString();
		}

		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}

		try
		{
			Registry reg = LocateRegistry.getRegistry(dstIP);
			FileManagerInterface fileManager = (FileManagerInterface)reg.lookup(Node.FILE_MANAGER_NAME);
			//IO.File file = new IO.File(this.getFullPath(filename,type));
			File file = new File(this.getFullPath(filename,srcType));
			fileManager.pushFile(filename,file.length(),dstType,remoteHost);
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
			FileManagerInterface fileManager =(FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);
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
	private String getFullPath(String filename, FileType type)
	{
		return this.getFolder(type) + filename;
	}

	/**
	 * Return the folder for the specified file type
	 * @param type
	 * @return
	 */
	public String getFolder (FileType type)
	{
		switch (type)
		{
			case LOCAL_FILE:
				return this.rootDirectory + LOCAL_FILE_PREFIX;

			case OWNED_FILE:
				return this.rootDirectory + OWNED_FILE_PREFIX;

			case DOWNLOADED_FILE:
				return this.rootDirectory + DOWNLOADED_FILE_PREFIX;

			case REPLICATED_FILE:
				return this.rootDirectory + REPLICATED_FILE_PREFIX;

			default:
				throw new InvalidParameterException ("File Type " + type.toString() +  " is not a valid filetype, possibilities are LOCAL_FILE, OWNED_FILE and DOWNLOADED_FILE.");
		}
	}

	public void setRootDirectory(String rootDirectory)
	{
		File folder = new File(rootDirectory);

		if(!folder.exists())
		{
			folder.mkdir();
		}
		this.rootDirectory = rootDirectory;
	}

	public String getRootDirectory()
	{
		return this.rootDirectory;
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
			builder.append(file.getName());
			builder.append('\n');
		}

		builder.append("OWNED:\n");
		folder = new File(this.getFolder( FileType.OWNED_FILE));

		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
			builder.append("OWNER: " + fileLedgers.get(file.getName()).getOwnerID() + "	LOCAL: " + fileLedgers.get(file.getName()).getLocalID() + "\n");
		}

		builder.append("REPLICATED\n");
		folder = new File(this.getFolder(FileType.REPLICATED_FILE));
		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		builder.append("DOWNLOADS:\n");
		folder = new File(this.getFullPath("", FileType.DOWNLOADED_FILE));

		for(File file: folder.listFiles())
		{
			builder.append(file.getName() + "\n");
			try
			{
				builder.append("OWNER: " + Node.getInstance().getResolverStub().getOwnerID(file.getName()) + "\n");
			}
			catch(RemoteException re)
			{
				re.printStackTrace();
			}
		}

		return builder.toString();
	}

	public boolean isRunning()
	{
		return  this.running;
	}
}
