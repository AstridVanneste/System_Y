package Network.Datagrams;

/**
 * Created by Astrid on 25-Oct-17.
 */
public class HeaderTest
{
	public static void main(String args[])
	{
		ProtocolHeader header = new ProtocolHeader();
		header.setVersion((byte)1);
		header.setDataLength(0);
		header.setTransactionID(2);
		header.setRequestCode(1);
		header.setReplyCode(2);

		System.out.println(header.toString());

		header.setHeader(header.serialize());

		byte[] serial = header.serialize();

		for(byte b: serial)
		{
			System.out.print(b);
		}
		System.out.println();

		System.out.println(header.toString());
	}
}
