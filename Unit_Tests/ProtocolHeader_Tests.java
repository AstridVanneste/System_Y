import IO.Network.Datagrams.ProtocolHeader;
import org.junit.Test;

import java.math.BigInteger;
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
			//System.out.println("============== ROUND " + Integer.toString(i) + " ==============");
			int transID = (rng.nextInt() & 0x7FFFFFFF);

			//System.out.println("Transaction ID: 0x" + Integer.toHexString((int) transID));

			ProtocolHeader ph1 = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION, 12, (int) transID, ProtocolHeader.REQUEST_DISCOVERY_CODE, ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);

			assertEquals("Version didn't match when constructing from separate fields", ph1.getVersion(), ProtocolHeader.CURRENT_VERSION);
			assertEquals("Data Length didn't match when constructing from separate fields", ph1.getDataLength(), 12);
			assertEquals("Transaction ID didn't match when constructing from separate fields", ph1.getTransactionID(), transID);
			assertEquals("Request Code didn't match when constructing from separate fields", ph1.getRequestCode(), ProtocolHeader.REQUEST_DISCOVERY_CODE);
			assertEquals("Request Code didn't match when constructing from separate fields", ph1.getRequestCode(), ProtocolHeader.REPLY_SUCCESSFULLY_ADDED);

			BigInteger bigInt = ph1.serializeBigInt();

			//System.out.println("BigInteger (from test): 0x" + bigInt.toString(16));

			ProtocolHeader ph2 = new ProtocolHeader(bigInt);

			assertEquals("Version didn't match when constructing from BigInteger (0x" + bigInt.toString(16) + ")", ProtocolHeader.CURRENT_VERSION, ph2.getVersion());
			assertEquals("Data Length didn't match when constructing from BigInteger (0x" + bigInt.toString(16) + ")", 12, ph2.getDataLength());
			assertEquals("Transaction ID didn't match when constructing from BigInteger (0x" + bigInt.toString(16) + ")", transID, ph2.getTransactionID());
			assertEquals("Request Code didn't match when constructing from BigInteger (0x" + bigInt.toString(16) + ")", ProtocolHeader.REQUEST_DISCOVERY_CODE, ph2.getRequestCode());
			assertEquals("Request Code didn't match when constructing from BigInteger (0x" + bigInt.toString(16) + ")", ProtocolHeader.REPLY_SUCCESSFULLY_ADDED, ph2.getRequestCode());
			//System.out.println("=====================================");
		}
	}
}
