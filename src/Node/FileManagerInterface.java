package Node;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileManagerInterface extends Remote
{
	/**
	 *
	 * @param type
	 * @throws RemoteException
	 */
	public void checkFiles (FileType type) throws RemoteException;

	/**
	 * Remote method.
	 * Caller = Node that's about to send a file.
	 * Callee = Node that's about to receive a file.
	 * @param filename      The filename used to retrieve and store the file.
	 * @param type          The type of file (local, owner or download)
	 * @param remoteHost    The port that the caller will use to send the file.
	 */
	public void pushFile(String filename, FileType type, String remoteHost) throws IOException;

	/**
	 * Remote method.
	 * Caller = Node that's about to receive a file.
	 * Callee = Node that's about to send a file.
	 * @param dstID			The ID of the node that wants to receive the file.
	 * @param filename		The filename used to retrieve and store the file.
	 * @throws IOException
	 */
	public void pullFile (short dstID, String filename) throws IOException;

	/**
	 * Remote method: Used to pass on a fileLedger from one node to another.
	 * Caller = Node that sends the fileLedger
	 * Callee = Node that has to receive the fileLedger
	 * @param fileLedger
	 * @throws IOException
	 *
	 */
	public void addFileLedger(FileLedger fileLedger) throws IOException;

	/**
	 *
	 * @param filename
	 */
	public void notifyLeaving (String filename, FileType type, short id) throws RemoteException;

	public void deleteFile(String filename, FileType type) throws IOException;

	public void lockSlot() throws RemoteException;

	public void unlockSlot() throws RemoteException;

	/**
	 * use a filename to get the fileledger remotely.
	 * @param name
	 * @return the fileledger corresponding with the received filename
	 * @throws RemoteException
	 */
	public FileLedger getFileLedger(String name) throws RemoteException;

	/**
	 * Remove a fileLedger from the list remotely.
	 * The input parameter is the name of the file that has to be deleted.
	 * @param fileName
	 * @throws RemoteException
	 */
	public void deleteFileLedgerRemote(String fileName) throws RemoteException;


	/**
	 * orders a node to remove a file through a remote call
	 * @param id of the receiving node
	 * @param filename of the to be deleted file
	 * @param filetype of the to be deletd file
	 * @throws RemoteException
	 */
	public void deleteFileRemote(short id, String filename, FileType filetype)  throws RemoteException;

	/**
	 * get a file ledger froma specific file through a remote call
	 * @param id of the node
	 * @param fileName of the to be delivered fileledger
	 * @return
	 * @throws RemoteException
	 */
	public FileLedger getFileLedgerRemote(short id, String fileName)  throws RemoteException;

	/**
	 * Copies a file from a node to another. This method does not take into account any responsibility of ledgers.
	 * Use this method as the most basic way to copy a file between nodes.
	 * @param filename
	 * @param dstID
	 * @param srcType
	 * @param dstType
	 * @throws RemoteException
	 */
	public void copyFile(String filename, short dstID, FileType srcType, FileType dstType) throws RemoteException;
}
