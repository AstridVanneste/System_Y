package IO.Network.TCP;

import IO.File;
import IO.Network.Datagrams.ProtocolHeader;

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
	 * Stops the client
	 */
	public void stop();


	/**
	 * sends complete file
	 * @param filename
	 * @param header
	 */
	public void sendFile(String filename, ProtocolHeader header);

	/**
	 * receives complete file and writes it to given path.
	 * @param filename
	 * @param transactionID
	 */
	public void receiveFile(String filename, int transactionID);

	public boolean hasData();
}
