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
	 * @param fileSize      The size of the file in bytes.
	 * @param type          The type of file (local, owner or download)
	 * @param remoteHost    The port that the caller will use to send the file.
	 */
	public void pushFile(String filename, long fileSize, FileType type, String remoteHost) throws IOException;

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
	 */
	public void addFileLedger(FileLedger fileLedger) throws IOException;

	/**
	 *
	 * @param filename
	 */
	public void notifyLeaving (String filename) throws RemoteException;

	public void deleteFile(String filename, FileType type) throws IOException;
}
