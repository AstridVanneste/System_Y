package Node;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileManagerInterface extends Remote
{
	public void checkFiles () throws RemoteException;

	/**
	 * Remote method.
	 * Caller = Node that's about to send a file.
	 * Callee = Node that's about to receive a file.
	 * @param filename      The filename used to retrieve and store the file.
	 * @param fileSize      The size of the file in bytes.
	 * @param type          The type of file (local, owner or download)
	 * @param remoteHost    The port that the caller will use to send the file.
	 */
	public void pushFile(String filename, int fileSize, FileType type, String remoteHost) throws IOException;

	public void pullFile (short dstID, String filename) throws RemoteException;
}
