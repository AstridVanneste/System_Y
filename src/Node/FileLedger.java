package Node;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FileLedger
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
	private Set<Short> copies;

	/**
	 * Each file has a ledger-object
	 * @param fileName name of file of this logging-object
	 * @param localID ID of node that has the file locally
	 */
	public FileLedger (String fileName, short localID)
	{
		this.fileName = fileName;
		this.localID = localID;
		this.ownerID = -1;
		this.copies = new HashSet();			//multiple nodes can ask at the same moment to download the file..
	}

	/**
	 * Add a downloader of this file
	 * @param ID
	 * @return  true if successfully added
	 * 			false if ID was already in the list
	 */
	public synchronized boolean addDownloader (short ID)
	{
		return copies.add(ID);
	}

	/**
	 * Remove the downloader of this file
	 * @param ID
	 * @return  true if successfully removed
	 * 			false if ID was not in the list
	 */
	public synchronized boolean removeDownloader (short ID)
	{
		return copies.remove(ID);
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

}
