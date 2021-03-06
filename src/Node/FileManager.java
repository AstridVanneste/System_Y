package Node;

import IO.Network.Constants;
import IO.Network.TCP.Client;
import IO.Network.TCP.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * This class will handle everything concerning the files.
 * To get a functional filemanager you need to set the root directory and then call the start() method.
 */
public class FileManager implements FileManagerInterface
{
	private static final int MAX_PERMITS = Integer.MAX_VALUE;
	// NOTE: Thomas changed these back to '/' instead of '\\' to make sure our software remains cross-platform
	// Changing them to be windows-specific might have worked, but it isn't a proper solution
	private static final String LOCAL_FILE_PREFIX = "Local/";
	private static final String OWNED_FILE_PREFIX = "Owned/";
	private static final String DOWNLOADED_FILE_PREFIX = "Downloads/";
	private static final String REPLICATED_FILE_PREFIX = "Replicated/";

	private Server tcpServer;
	private String rootDirectory;
	private ConcurrentHashMap<String, FileLedger> fileLedgers;
	private boolean running;
	private Semaphore sendSemaphore;

	public FileManager()
	{
		this.tcpServer = null;
		this.rootDirectory = System.getProperty("user.home");
		this.fileLedgers = new ConcurrentHashMap<String, FileLedger>();
		this.running = false;
		this.sendSemaphore = new Semaphore(MAX_PERMITS, true);

		try
		{
			this.sendSemaphore.acquire(this.sendSemaphore.availablePermits());
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	/**
	 * Starts the filemanager:
	 * - starts TCP server
	 * - creates all necessary folders for the filemanager functions (/LOCAL, /OWNED and /DOWNLOADS)
	 */
	public void start()
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

		File folder = new File(getFullPath("", FileType.LOCAL_FILE));
		if (!folder.exists())
		{
			folder.mkdir();
		}

		folder = new File(getFullPath("", FileType.OWNED_FILE));
		if (!folder.exists())
		{
			folder.mkdir();
		}
		for(File file: folder.listFiles())
		{
			file.delete();
		}

		folder = new File(getFullPath("", FileType.DOWNLOADED_FILE));
		if (!folder.exists())
		{
			folder.mkdir();
		}

		folder = new File(getFullPath("", FileType.REPLICATED_FILE));
		if (!folder.exists())
		{
			folder.mkdir();
		}
		for(File file: folder.listFiles())
		{
			file.delete();
		}

		this.sendSemaphore.release(MAX_PERMITS);

		//start replicating files.
		this.checkFiles(FileType.LOCAL_FILE);

	}

	/**
	 * Stop the filemanager:
	 * - stops TCP server
	 * - deletes all owned files.
	 * - deletes all replicated files.
	 */
	public void stop()
	{
		if (this.running)
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

		File folder = new File(getFullPath("", FileType.OWNED_FILE));

		for (File file : folder.listFiles())
		{
			file.delete();
		}


		folder = new File(getFullPath("", FileType.REPLICATED_FILE));

		for (File file : folder.listFiles())
		{
			file.delete();
		}

	}

	public void shutdown()
	{
		this.running = false;

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
				ownerInterface.notifyLeaving(localFile.getName(), FileType.LOCAL_FILE, Node.getInstance().getId());
			}
			catch (RemoteException | NotBoundException e)
			{
				System.err.println("FileManager.shutdown()");
				Node.getInstance().getFailureAgent().failure(ownerID);
				e.printStackTrace();
			}
		}

		Iterator<String> keyIt = this.fileLedgers.keySet().iterator();

		while(keyIt.hasNext())
		{
			String key = keyIt.next();
			if (this.fileLedgers.get(key).getOwnerID() == Node.getInstance().getId())
			{
				// I'm the owner
				// Copy the file to my previous
				// Tell him he's the new owner
				this.sendFile(Node.getInstance().getPreviousNeighbour(), key, FileType.OWNED_FILE, FileType.OWNED_FILE);
			}
		}

		String replicatedFolder = this.getFolder(FileType.REPLICATED_FILE);

		for (File replicatedFile : (new File(replicatedFolder)).listFiles())
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
				ownerInterface.notifyLeaving(replicatedFile.getName(), FileType.REPLICATED_FILE, Node.getInstance().getId());
			}
			catch (RemoteException | NotBoundException e)
			{
				System.err.println("FileManager.shutdown()");
				Node.getInstance().getFailureAgent().failure(ownerID);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyLeaving(String filename, FileType type, short id)
	{
		/*
		try
		{
			System.out.println("Node is leaving with file " + filename + ", type: " + type + " called by " + getClientHost());
		}
		catch (ServerNotActiveException snae)
		{
			snae.printStackTrace();
		}
		*/

		String fullPath = this.getFullPath(filename, FileType.OWNED_FILE);

		/* File was never downloaded
		 * Local file holder is leaving
		 * Delete file from owner
		 */
		if ((type == FileType.LOCAL_FILE) && (this.fileLedgers.get(filename).getNumDownloads() == 0))
		{
			//System.out.println("File was local and never downloaded.");
			File fileObj = new File(fullPath);
			fileObj.delete();

			// If a file that needs to be deleted has been replicated, delete the replica
			if (this.fileLedgers.get(filename).getReplicatedId() != Node.DEFAULT_ID)
			{
				try
				{
					Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(this.fileLedgers.get(filename).getReplicatedId()));
					FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
					remoteFileManager.deleteFile(filename, FileType.REPLICATED_FILE);
				}
				catch (NotBoundException | IOException re)
				{
					re.printStackTrace();
				}
			}

			Node.getInstance().getAgentHandler().deleteFile(filename);
			//System.out.println("removing ledger " + filename + " from the list thread: " + Thread.currentThread().getName());
			this.fileLedgers.remove(filename);
		}
		else if ((type == FileType.LOCAL_FILE) || (type == FileType.REPLICATED_FILE))
		{
			/*
			 *  File was local and downloaded at least once
			 */
			//System.out.println("notifyLeaving, file was local or replicated with >= 1 downloads");

			if (type == FileType.LOCAL_FILE)
			{
				this.fileLedgers.get(filename).setLocalID(Node.DEFAULT_ID);
				//System.out.println("Fetched fileledger, set local ID to default:" + this.fileLedgers.get(filename).toString() + " thread " + Thread.currentThread().getName());
			}

			if(Node.getInstance().getPreviousNeighbour() != id)												//todo: this if statement was added and needs to be tested
			{
				this.fileLedgers.get(filename).setReplicatedId(Node.getInstance().getPreviousNeighbour());
			}
			else
			{
				try
				{
					short replicatedId = Node.getInstance().getResolverStub().getPrevious(Node.getInstance().getPreviousNeighbour());
					this.fileLedgers.get(filename).setReplicatedId(replicatedId);
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}

			//System.out.println("Fetched fileledger, set replicated ID to previous: " + this.fileLedgers.get(filename).toString() + " thread " + Thread.currentThread().getName());

			if (fileLedgers.get(filename).getLocalID() == fileLedgers.get(filename).getOwnerID())
			{
				//System.out.println("Local and owner are equal (" + Integer.toString(fileLedgers.get(filename).getLocalID()) + ")");
				this.sendFile(this.fileLedgers.get(filename).getReplicatedId(), filename, FileType.LOCAL_FILE, FileType.REPLICATED_FILE);
			}
			else
			{
				//System.out.println("Local and owner aren't equal (" + Integer.toString(fileLedgers.get(filename).getLocalID()) + ")");
				this.sendFile(this.fileLedgers.get(filename).getReplicatedId(), filename, FileType.OWNED_FILE, FileType.REPLICATED_FILE);
			}
		}
		else
		{
			//System.out.println("None of the conditions matched, notifyLeaving");
		}
	}

	/**
	 * Deletes a file on a remote.
	 *
	 * @param filename
	 * @param type
	 * @throws IOException
	 */
	@Override
	public void deleteFile(String filename, FileType type) throws IOException
	{
		File file = new File(this.getFullPath(filename, type));

		if (file.exists())
		{
			file.delete();
		}
	}

	@Override
	public void lockSlot()
	{
		try
		{
			this.sendSemaphore.acquire(1);
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	@Override
	public void unlockSlot()
	{
		this.sendSemaphore.release(1);
	}

	/**
	 * Request to download file. The file will be pushed (push()) to the node requesting it.
	 *
	 * @param dstID
	 * @param filename
	 * @throws IOException
	 */
	@Override
	public void pullFile(short dstID, String filename) throws IOException
	{
		IO.File file;
		if(this.fileLedgers.get(filename).getReplicatedId() == Node.DEFAULT_ID)
		{
			file = new IO.File(getFullPath(filename, FileType.OWNED_FILE));
		}
		else
		{
			file = new IO.File(getFullPath(filename, FileType.LOCAL_FILE));
		}

		if (file.exists())
		{
			if(this.fileLedgers.get(filename).getReplicatedId() == Node.DEFAULT_ID)
			{
				this.sendFile(dstID, filename, FileType.OWNED_FILE, FileType.DOWNLOADED_FILE);
			}
			else
			{
				this.sendFile(dstID, filename, FileType.LOCAL_FILE, FileType.DOWNLOADED_FILE);
			}

		}
		else
		{
			throw new IOException("No file with name " + filename + " in " + OWNED_FILE_PREFIX);
		}

		this.fileLedgers.get(filename).addDownloader(dstID); //add person who requests the file to downloaders
	}

	/**
	 * @param fileLedger
	 * @throws IOException
	 */
	@Override
	public void addFileLedger(FileLedger fileLedger) throws IOException
	{
		if (this.fileLedgers.containsKey(fileLedger.getFileName()))
		{
			throw new IOException("Node " + Short.toString(Node.getInstance().getId()) + " already has a fileLedger with filename " + fileLedger.getFileName());
		}
		else
		{
			this.fileLedgers.put(fileLedger.getFileName(), fileLedger);
			//System.out.println("adding fileledger of " + fileLedger.getFileName() + "thread " + Thread.currentThread().getName());
		}
	}

	/**
	 * Checks the owner of the files of a given type. When necessary it will push a file to the owner.
	 *
	 * @param type
	 */
	public void checkFiles(FileType type)
	{
		//System.out.println(Thread.currentThread().getName() + " FileManager.checkFiles(" + type + ") " + Node.getInstance().getResolverStub());
		File folder = new File(this.getFullPath("", type));

		File[] fileList;

		if(type != FileType.OWNED_FILE)
		{
			fileList = folder.listFiles();
		}
		else //in case of owned files it is important to use the fileLedgers instead of list of files in owned folder because this does not take into account the owned files in the local folder
		{
			fileList = new java.io.File[this.fileLedgers.size()];

			int i = 0;
			for(String filename: this.fileLedgers.keySet())
			{
				if(this.fileLedgers.get(filename).getReplicatedId() == Node.DEFAULT_ID)
				{
					fileList[i] = new java.io.File(getFullPath(filename, FileType.OWNED_FILE));
				}
				else
				{
					fileList[i] = new java.io.File(getFullPath(filename, FileType.LOCAL_FILE));
				}
				i++;
			}
		}

		for (File file : fileList)
		{
			short ownerId = Node.DEFAULT_ID;
			String ownerIP = "";

			//Node.getInstance().getController().addFile(new TableFile(file.getName(), "Not supported yet"));
			String filename = file.getName();
			//System.out.println("checking file: " + filename + " File.exists() " + file.exists());

			try
			{
				ownerId = Node.getInstance().getResolverStub().getOwnerID(filename);
				ownerIP = Node.getInstance().getResolverStub().getIP(ownerId);
			}
			catch (RemoteException re)
			{
				re.printStackTrace();
			}

			try
			{
				if (type == FileType.LOCAL_FILE && ownerId == Node.getInstance().getId())    // We have the file locally and we are the owner
				{                                                                           // Replicate it elsewhere
					//you become the new owner of the file...
					//send replication to your previous neighbour. this node becomes the owner of the file

					String replicatedIP = Node.getInstance().getResolverStub().getIP(Node.getInstance().getPreviousNeighbour());
					Registry registry = LocateRegistry.getRegistry(replicatedIP);
					FileManagerInterface fileManager = (FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);
					this.fileLedgers.put(file.getName(), new FileLedger(file.getName(), Node.getInstance().getId(), Node.getInstance().getId(), Node.getInstance().getPreviousNeighbour()));
					//System.out.println("adding fileledger " + file.getName() + " thread " + Thread.currentThread().getName());

					this.sendFile(Node.getInstance().getPreviousNeighbour(), file.getName(), FileType.LOCAL_FILE, FileType.REPLICATED_FILE);
				}
				else if ((type != FileType.DOWNLOADED_FILE) && (type != FileType.REPLICATED_FILE) && (ownerId != Node.getInstance().getId()))     // We aren't the owner, The file isn't downloaded or replicated (So owned, local)
				{
					if (type == FileType.OWNED_FILE)                                // We own the file
					{
						FileLedger fileLedger = this.fileLedgers.get(file.getName());   // Fetch the Ledger

						/*
						for (String filenameStr : this.fileLedgers.keySet())
						{
							System.out.println("File " + filenameStr + " is present");
						}
						*/

						if (!(fileLedger.getReplicatedId() == Node.DEFAULT_ID))          // The file is replicated somewhere
						{
							String replicaIP = Node.getInstance().getResolverStub().getIP(fileLedger.getReplicatedId());
							Registry reg = LocateRegistry.getRegistry(replicaIP);
							FileManagerInterface stub = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
							stub.deleteFile(file.getName(), FileType.REPLICATED_FILE);  // Delete the replicated file
						}
					}

					if(type == FileType.OWNED_FILE)
					{
						if(this.fileLedgers.get(file.getName()).getReplicatedId() != Node.DEFAULT_ID)
						{
							this.sendFile(ownerId,file.getName(),FileType.LOCAL_FILE,FileType.OWNED_FILE);
						}
						else
						{
							this.sendFile(ownerId, file.getName(), type, FileType.OWNED_FILE);
							file.delete();
						}
					}
					else
					{
						this.sendFile(ownerId, file.getName(), type, FileType.OWNED_FILE);
					}
				}

				if(type == FileType.LOCAL_FILE)
				{
					//System.out.println("Adding " + file.getName() + " to the list of files");
					Node.getInstance().getAgentHandler().advertiseFile(file.getName());
				}
			}
			catch (RemoteException re)
			{
				System.err.println("FileManager.checkFiles()");
				re.printStackTrace();
				Node.getInstance().getFailureAgent().failure(ownerId);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch (NotBoundException e)
			{
				e.printStackTrace();
				System.err.println("FileManager.checkFiles()");
				Node.getInstance().getFailureAgent().failure(ownerId);
			}
		}
	}

	@Override
	public void pushFile(String filename, FileType type, String remoteHost) throws IOException
	{
		filename = this.getFullPath(filename, type);

		/*
		if (type == FileType.OWNED_FILE)
		{
			if (this.hasFile(filename, type))
			{
				//this.sendFile(Node.getInstance().getPreviousNeighbour(), filename, FileType.LOCAL_FILE, FileType.REPLICATED_FILE);
			}
			else
			{
				System.out.println("File was supposed to be present, but it wasn't");
			}
		}
		*/

		this.tcpServer.receiveFile(remoteHost, filename);
	}

	/**
	 * Sends a file to a certain node. It will also send the ledgers via the standard rules.
	 *
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
		catch (RemoteException re)
		{
			re.printStackTrace();
		}

		FileManagerInterface remoteFileManager = null;
		Client client = null;

		try
		{
			Registry reg = LocateRegistry.getRegistry(dstIP);
			remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);

			remoteFileManager.lockSlot();

			client = new Client(dstIP, Constants.FILE_RECEIVE_PORT);
			client.start();

			remoteFileManager.unlockSlot();
		}
		catch (RemoteException | NotBoundException re)
		{
			re.printStackTrace();
		}

		if(srcType == FileType.OWNED_FILE)
		{
			if (this.fileLedgers.get(filename).getReplicatedId() == Node.DEFAULT_ID)
			{
				client.sendFile(this.getFullPath(filename,FileType.OWNED_FILE));
			}
			else
			{
				client.sendFile(this.getFullPath(filename, FileType.LOCAL_FILE));
			}
		}
		else
		{
			client.sendFile(this.getFullPath(filename, srcType));
		}

		client.stop();

		int localPort = client.getLocalPort();
		String remoteHost = "";

		try
		{
			InetSocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), localPort);

			remoteHost = socket.toString();
		}
		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}

		//String remoteHost = client.getLocalPort();

		try
		{
			Registry reg = LocateRegistry.getRegistry(dstIP);
			//remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
			//IO.File file = new IO.File(this.getFullPath(filename,type));
			//File file = new File(this.getFullPath(filename, srcType));
			remoteFileManager.pushFile(filename, dstType, remoteHost);

			if (dstType == FileType.OWNED_FILE)
			{
				if (srcType == FileType.LOCAL_FILE)
				{
					remoteFileManager.addFileLedger(new FileLedger(filename, Node.getInstance().getId(), dstID, Node.DEFAULT_ID));
				}
				else if (srcType == FileType.OWNED_FILE)
				{
					FileLedger ledger = this.fileLedgers.get(filename);
					ledger.setOwnerID(dstID);
					remoteFileManager.addFileLedger(ledger);
					this.fileLedgers.remove(filename);
					//System.out.println("removing ledger " + filename + "from list thread " + Thread.currentThread().getName());
				}
			}
		}
		catch (RemoteException re)
		{
			re.printStackTrace();
			System.err.println("FileManager.sendFile()");
			Node.getInstance().getFailureAgent().failure(dstID);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Requests a certain file from the owner.
	 *
	 * @param filename
	 */
	public void requestFile(String filename) //todo: make sure you don't download files you already have
	{
		String ownerIP = "";
		//System.out.println("Requesting file " + filename);

		//System.out.println("Requesting file " + filename);

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

			FileManagerInterface remoteFileManager = (FileManagerInterface) registry.lookup(Node.FILE_MANAGER_NAME);
			remoteFileManager.pullFile(Node.getInstance().getId(), filename);
			/*
			the owner will later call a push() method on this node to actually receive the file.
			 */
		}
		catch (NotBoundException | IOException nbe)
		{
			nbe.printStackTrace();
		}
	}

	/**
	 * Returns the full path of a file.
	 *
	 * @param filename
	 * @param type
	 * @return
	 */
	public String getFullPath(String filename, FileType type)
	{
		return this.getFolder(type) + filename;
	}

	/**
	 * Return the folder for the specified file type
	 *
	 * @param type
	 * @return
	 */
	public String getFolder(FileType type)
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
				throw new InvalidParameterException("File Type " + type.toString() + " is not a valid filetype, possibilities are LOCAL_FILE, OWNED_FILE and DOWNLOADED_FILE.");
		}
	}

	public void setRootDirectory(String rootDirectory)
	{
		File folder = new File(rootDirectory);

		if (!folder.exists())
		{
			folder.mkdir();
		}
		this.rootDirectory = rootDirectory;
	}

	public void deleteFileLedger(String fileName)
	{
		fileLedgers.remove(fileName);
	}

	@Override
	public void deleteFileLedgerRemote(String fileName) throws RemoteException
	{
		fileLedgers.remove(fileName);
	}

	@Override
	public void copyFile(String filename,short dstID, FileType srcType, FileType dstType) throws RemoteException
	{
		String dstIP = "";
		Client client = new Client(dstIP, Constants.FILE_RECEIVE_PORT);
		client.start();

		int localPort = client.getLocalPort();
		String remoteHost = "";

		try
		{
			InetSocketAddress socket = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), localPort);

			remoteHost = socket.toString();
		}
		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}

		//String remoteHost = client.getLocalPort();

		try
		{
			dstIP = Node.getInstance().getResolverStub().getIP(dstID);

			client.sendFile(this.getFullPath(filename, srcType));
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		try
		{
			Registry reg = LocateRegistry.getRegistry(dstIP);
			FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
			remoteFileManager.pushFile(filename,dstType, remoteHost);
		}
		catch(NotBoundException | IOException nbe)
		{
			nbe.printStackTrace();
			Node.getInstance().getFailureAgent().failure(dstID);
		}
	}


	public String getRootDirectory()
	{
		return this.rootDirectory;
	}

	/**
	 * Returns a clear readable list of all the files of the node.
	 *
	 * @return
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("FILES:\n");
		builder.append("LOCAL:\n");

		File folder = new File(this.getFullPath("", FileType.LOCAL_FILE));

		for (File file : folder.listFiles())
		{
			builder.append(file.getName());
			builder.append('\n');
		}

		builder.append("\nOWNED:\n");
		folder = new File(this.getFolder(FileType.OWNED_FILE));

		for (File file : folder.listFiles())
		{
			builder.append(file.getName() + "\n");
			if (this.fileLedgers.containsKey(file.getName()))
			{
				builder.append("OWNER: " + this.fileLedgers.get(file.getName()).getOwnerID() + "	LOCAL: " + this.fileLedgers.get(file.getName()).getLocalID() +"		REPLICATED: " + this.fileLedgers.get(file.getName()).getReplicatedId() + "\n");
			}
		}

		builder.append("\nREPLICATED:\n");
		folder = new File(this.getFolder(FileType.REPLICATED_FILE));
		for (File file : folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		builder.append("\nDOWNLOADS:\n");
		folder = new File(this.getFullPath("", FileType.DOWNLOADED_FILE));

		for (File file : folder.listFiles())
		{
			builder.append(file.getName() + "\n");
		}

		return builder.toString();
	}

	public synchronized boolean isRunning()
	{
		return this.running;
	}

	public boolean hasFile(String filename, FileType type)
	{
		File file = new File(this.getFullPath(filename, type));
		return file.exists();
	}

	public ConcurrentHashMap<String, FileLedger> getFileLedgers()
	{
		return this.fileLedgers;
	}

	@Override
	public FileLedger getFileLedgerRemote(short id, String fileName)  throws RemoteException
	{
		FileLedger fileLedger = null;
		try
		{
			Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(id));
			FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
			fileLedger = remoteFileManager.getFileLedger(fileName);
		}
		catch (NotBoundException | IOException re)
		{
			re.printStackTrace();
		}
		return fileLedger;
	}

	public void replaceFileLedgerRemote(short id, FileLedger fileLedger)
	{
		try
		{
			Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(id));
			FileManagerInterface remoteFileManager = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
			remoteFileManager.deleteFileLedgerRemote(fileLedger.getFileName());
			remoteFileManager.addFileLedger(fileLedger);
		}
		catch (NotBoundException | IOException re)
		{
			re.printStackTrace();
		}
	}


	@Override
	public void deleteFileInNetwork(String filename) throws RemoteException
	{
		if (this.fileLedgers.containsKey(filename))
		{
			short localId = this.fileLedgers.get(filename).getLocalID();
			short replicatedId = this.fileLedgers.get(filename).getReplicatedId();
			Set<Short> downloads = this.fileLedgers.get(filename).getDownloads();

			if (localId == Node.getInstance().getId())  // Local and owner are same node, delete locally
			{
				try
				{
					this.deleteFile(filename, FileType.LOCAL_FILE);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else    // Delete file, we are the owner
			{
				try
				{
					this.deleteFile(filename, FileType.OWNED_FILE);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (localId != Node.DEFAULT_ID)
			{
				try
				{
					Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(localId));
					FileManagerInterface replicatedFM = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
					replicatedFM.deleteFile(filename, FileType.LOCAL_FILE);
				}
				catch (NotBoundException | IOException nbe)
				{
					System.err.println("[ERROR]\tError while trying to delete replication.");
					nbe.printStackTrace();
				}
			}

			if (replicatedId != Node.DEFAULT_ID)
			{
				try
				{
					Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(replicatedId));
					FileManagerInterface replicatedFM = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
					replicatedFM.deleteFile(filename, FileType.REPLICATED_FILE);
				}
				catch (NotBoundException | IOException nbe)
				{
					System.err.println("[ERROR]\tError while trying to delete replication.");
					nbe.printStackTrace();
				}
			}

			for (short downloadId : downloads)
			{
				if (downloadId != Node.DEFAULT_ID)
				{
					try
					{
						Registry reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(downloadId));
						FileManagerInterface replicatedFM = (FileManagerInterface) reg.lookup(Node.FILE_MANAGER_NAME);
						replicatedFM.deleteFile(filename, FileType.DOWNLOADED_FILE);
					}
					catch (NotBoundException | IOException nbe)
					{
						System.err.println("[ERROR]\tError while trying to delete replication.");
						nbe.printStackTrace();
					}
				}
			}
		}
		else
		{
			System.err.println("[ERROR]\tCalled deleteFileInNetwork() on a non-owner.");
		}
	}

	/**
	 * replaces the current ledger for a file with a new one
	 *
	 * @param ledger
	 * @warning THE CURRENT LEDGER WILL BE REPLACED AND LOST! if you do not want this use addFileLedger()
	 */
	public void replaceFileLedger(FileLedger ledger)
	{
		this.fileLedgers.put(ledger.getFileName(), ledger);
		//System.out.println("replacing fileledger of file " + ledger.getFileName() + " thread " + Thread.currentThread().getName());
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getOwnedFiles()
	{
		return this.fileLedgers.keySet();
	}

	public FileLedger getFileLedger(String name) throws RemoteException
	{
		return fileLedgers.get(name);
	}
}
