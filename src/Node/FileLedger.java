package Node;

import Util.General;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FileLedger implements Serializable
{
	/*
	 * 	This class handles about logging the distribution of each file.
	 * 	Each node has some local files. Each file has a FileLedger-object with localID = his ID.
	 * 	Next, he will calculate the new ownerID and give the file + FileLedger-object to that node.
	 * 	The ownerID becomes the ID of the new node.
	 *
	 * 	When someone downloads the file from the owner, the owner has to update the copies-list.
	 *
	 * 	When the owner gets a new nextNeighbour, he has to check whether the new node becomes the owner
	 * 	If so:
	 * 		The new owner becomes the ownerID
	 * 		The previous owner becomes a downloader in the copies-list
	 * 	If not:
	 *		Do nothing
	 */

	private String fileName;
	private short ownerID;
	private short localID;
	private short replicatedID;
	private Set<Short> copies;

	/**
	 * Each file has a ledger-object
	 * @param fileName name of file of this logging-object
	 * @param localID ID of node that has the file locally
	 * @param ownerID ID of the owner of the node
	 * @param replicatedID ID of another replication of the file (if owner = local)
	 */
	public FileLedger (String fileName, short localID, short ownerID, short replicatedID)
	{
		this.fileName = fileName;
		this.localID = localID;
		this.ownerID = ownerID;
		this.replicatedID = replicatedID;
		this.copies = new HashSet<>();
	}

	/**
	 * Add a downloader of this file
	 * @param ID
	 * @return  true if successfully added
	 * 			false if ID was already in the list
	 */
	public boolean addDownloader (short ID)
	{
		return this.copies.add(ID);
	}

	/**
	 * Remove the downloader of this file
	 * @param ID
	 * @return  true if successfully removed
	 * 			false if ID was not in the list
	 */
	public boolean removeDownloader (short ID)
	{
		return this.copies.remove(ID);
	}

	public void printFileLedger ()
	{
		General.printLineSep();
		System.out.println("FileLedger of file: " + this.fileName);
		System.out.println("Local: " + this.localID);
		System.out.println("Owner: " + this.ownerID);
		System.out.println("Downloads: ");
		for (Short ID : copies){
			System.out.println("Node " + ID);
		}

	}

	public short getOwnerID()
	{
		return this.ownerID;
	}

	public void setOwnerID(short ownerID)
	{
		this.ownerID = ownerID;
	}

	public short getLocalID()
	{
		return this.localID;
	}

	public void setLocalID(short localID)
	{
		this.localID = localID;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public int getNumDownloads ()
	{
		return this.copies.size();
	}
}
