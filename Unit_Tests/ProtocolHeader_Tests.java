import IO.Network.Datagrams.ProtocolHeader;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ProtocolHeader_Tests
{
	@Test
	public void ConstructorAndSerializeTest () throws Exception
	{
		Random rng = new Random();
		for (int i = 0; i < 10000; i++)
		{
			int transID = (rng.nextInt() & 0x7FFFFFFF);

			ProtocolHeader ph1 = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION, 12, (int) transID, ProtocolHeader.REQUEST_DISCOVERY_CODE, ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);

			assertEquals("Version didn't match when constructing from separate fields", ph1.getVersion(), ProtocolHeader.CURRENT_VERSION);
			assertEquals("Data Length didn't match when constructing from separate fields", ph1.getDataLength(), 12);
			assertEquals("Transaction ID didn't match when constructing from separate fields", ph1.getTransactionID(), transID);
			assertEquals("Request Code didn't match when constructing from separate fields", ph1.getRequestCode(), ProtocolHeader.REQUEST_DISCOVERY_CODE);
			assertEquals("Request Code didn't match when constructing from separate fields", ph1.getRequestCode(), ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);
		}
	}
}
