package Network.UDP.Multicast;

import java.io.IOException;
import java.net.DatagramPacket;

public interface UDPSubscriber
{
	/**
	 * Starts the UDP server.
	 */
	public void start ();

	/**
	 * reads all bytes from the interal receive buffer
	 * @return	All data in the buffer for the specified host.
	 */
	public byte[] receiveData ();

	/**
	 * read packet from internal receive buffer.
	 * @return				The first packet in the internal receive buffer.
	 */
	public DatagramPacket receivePacket ();

	/**
	 * Stops the server
	 * @throws IOException
	 */
	public void stop() throws IOException;

	/**
	 * Called by the server's start() method.
	 * Implements Runnable
	 * @see Runnable
	 */
	public void run ();

	public String toString();
}
