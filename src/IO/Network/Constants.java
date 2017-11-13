package IO.Network;

import IO.Network.Datagrams.ProtocolHeader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Astrid on 02-Oct-17.
 */
public class Constants
{
	/**
	 * The Multicast IP that's going to be used for discovery
	 */
	public static final String DISCOVERY_MULTICAST_IP = "224.0.0.1";

	public static final int DISCOVERY_NAMESERVER_PORT = 2000;

	public static final int DISCOVERY_CLIENT_PORT = 2001;

	public static final int FILE_RECEIVE_PORT = 2002;

	/**
	 * Default Encoding
	 * When a set of bytes is decoded to a String, an encoding needs to be specified.
	 * Please use this.
	 */
	public static final Charset ENCODING = StandardCharsets.UTF_8;

	/**
	 * Segment size for sending a TCP segment
	 */
	public static final int MAX_TCP_SEGMENT_SIZE = 1460;
	public static final int MAX_TCP_FILE_SEGMENT_SIZE = MAX_TCP_SEGMENT_SIZE - ProtocolHeader.HEADER_LENGTH;
}