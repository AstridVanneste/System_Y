package IO.Network;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Astrid on 02-Oct-17.
 */
public class Constants
{

	public final static int TCP_PORT = 1997;
	//public final static int UDP_PORT_CLIENT = 2000;
	public final static int UDP_PORT_SERVER = 2001;

	/**
	 * Publisher IP for testing purposes.<br>
	 * Update at the beginning of every lab session.<br>
	 * If you don't:<br>
	 * <img src="http://godlessmom.com/wp-content/uploads/2015/03/youre-going-to-have-a-bad-time.png" alt="You're going to have a bad time.">
	 */
	public final static String TCP_SERVER_IP = "localhost";
	public final static String UDP_SERVER_IP = "localhost";


	/**
	 * Default Encoding
	 * When a set of bytes is decoded to a String, an encoding needs to be specified.
	 * Please use this.
	 */
	public static final Charset ENCODING = StandardCharsets.UTF_8;
}