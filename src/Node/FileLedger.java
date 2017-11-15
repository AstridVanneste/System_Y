package Node;

import java.util.List;

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
	private List<Short> copies;

	public FileLedger (String fileName, short localID) {
		this.fileName = fileName;
		this.localID = localID;
		this.ownerID = -1;
		this.copies = new List<Short>();	// which datastructure? (unique downloaders!)
	}

	public void addDownloader (short ID) {
		copies.add(ID);
	}

	public void removeDownloader (short ID) {
		if (copies.contains(ID))
		{
			copies.remove(ID);
		}
		else
			System.out.println("This ID has not downloaded the file");
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
