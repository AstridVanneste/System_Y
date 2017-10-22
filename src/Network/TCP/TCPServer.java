package Network.TCP;

import java.io.IOException;
import java.util.List;

public interface TCPServer extends Runnable
{
	/**
	 * Starts the TCP server.
	 * @throws IOException	An IOException can be thrown by the ServerSocket constructor
	 */
	public void start () throws IOException;

	/**
	 * Sends a string. to the specified remote host.<br>
	 * @param remoteHost	The remote host that the data should be sent to.<br>
	 * @param data			The data to be sent.<br>
	 */
	public void send (String remoteHost, String data);

	/**
	 * Sends all bytes in the data array to the specified remote host.<br>
	 * @param remoteHost	The remote host that the data should be sent to.<br>
	 * @param data			The data to be sent.<br>
	 */
	public void send (String remoteHost, byte[] data);

	/**
	 * Sends all bytes in the data list to specified the remote host.<br>
	 * @param remoteHost	The remote host that the data should be sent to.<br>
	 * @param data			The data to be sent.<br>
	 */
	public void send (String remoteHost, List<Byte> data);

	/**
	 * Reads all bytes from the interal receive buffer.<br>
	 * @param	remoteHost	The remote host from whose buffer we want to read.<br>	//todo: Check Grammar
	 * @return	All data in the buffer for the specified host.<br>
	 */
	public byte[] receive (String remoteHost);

	/**
	 * read numBytes bytes from the internal receive buffer.<br>
	 * @param remoteHost	The remote host from whose buffer we want to read.<br> //todo: Check Grammar
	 * @param numBytes		The number of bytes to read.<br>
	 * @return				The first numBytes bytes in the inputStream for the requested host.<br>
	 */
	public byte[] receive (String remoteHost, int numBytes);

	/**
	 * Stops the server.<br>
	 * @throws IOException The close() method of the ServerSocket can throw an IOException
	 */
	public void stop() throws IOException;

	/**
	 * Called by the server's start() method.<br>
	 * Implements Runnable.<br>
	 * @see Runnable
	 */
	public void run ();

	public String toString();
}