package Network.TCP;

import java.util.List;

public interface TCPClient
{
	/**
	 * Starts the TCP client
	 */
	public void start ();

	/**
	 * Sends all bytes in the data array
	 * @param data
	 */
	public void send (byte[] data);

	/**
	 * Sends all bytes in the data list.
	 * @param data
	 */
	public void send (List<Byte> data);

	/**
	 * reads all bytes from the interal receive buffer
	 * @return
	 */
	public byte[] receive ();

	/**
	 * read numBytes bytes from the internal receive buffer.
	 * @param numBytes
	 * @return
	 */
	public byte[] receive (int numBytes);

	/**
	 * Stops the client
	 */
	public void stop();
}
