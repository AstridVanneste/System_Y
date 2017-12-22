import Util.Serializer;
import org.junit.Test;
import static org.junit.Assert.*;

public class Serializer_Tests
{
	@Test
	public void IPTest () throws Exception
	{
		String ipString = "192.168.0.1";
		int[] ipBytes = Serializer.ipStringToBytes(ipString);
		assertEquals("String -> Bytes", 192, ipBytes[0]);
		assertEquals("String -> Bytes", 168, ipBytes[1]);
		assertEquals("String -> Bytes", 0, ipBytes[2]);
		assertEquals("String -> Bytes", 1, ipBytes[3]);

		String reconstructedIpString = Serializer.bytesToIPString(ipBytes);
		assertEquals("Bytes -> String", ipString, reconstructedIpString);
	}

	@Test
	public void IntTest () throws Exception
	{
		int integerNumber = 0x876543F5;
		byte component1 = (byte) 0x87;
		byte component2 = (byte) 0x65;
		byte component3 = (byte) 0x43;
		byte component4 = (byte) 0xF5;

		byte[] intBytes = Serializer.intToBytes(integerNumber);

		assertEquals("Int ->  Bytes", intBytes[0], component1);
		assertEquals("Int ->  Bytes", intBytes[1], component2);
		assertEquals("Int ->  Bytes", intBytes[2], component3);
		assertEquals("Int ->  Bytes", intBytes[3], component4);

		int reconstructedIntegerNumber = Serializer.bytesToInt(intBytes);

		assertEquals("Bytes -> Int", integerNumber, reconstructedIntegerNumber);
	}

	@Test
	public void ShortTest () throws Exception
	{
		short shortNumber = 0x1234;
		byte component1 = 0x12;
		byte component2 = 0x34;

		byte[] shortBytes = Serializer.shortToBytes(shortNumber);

		assertEquals("Short -> Bytes", shortBytes[0], component1);
		assertEquals("Short -> Bytes", shortBytes[1], component2);

		short reconstructedShort = Serializer.bytesToShort(shortBytes);

		assertEquals("Bytes -> Short", shortNumber, reconstructedShort);
	}
}