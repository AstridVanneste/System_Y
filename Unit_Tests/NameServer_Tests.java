import NameServer.NameServer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NameServer_Tests
{
	@Test
	public void HashTest () throws Exception
	{
		String str1 = "Thomas";
		String str2 = "Astrid";
		String str3 = "Axel";
		String str4 = "Fergan";
		String str5 = "Gedistribueerde Systemen";

		int hash1 = 17628;
		int hash2 = 875;
		int hash3 = 23358;
		int hash4 = 25217;
		int hash5 = 11987;

		assertEquals(str1, hash1, NameServer.getHash(str1));
		assertEquals(str2, hash2, NameServer.getHash(str2));
		assertEquals(str3, hash3, NameServer.getHash(str3));
		assertEquals(str4, hash4, NameServer.getHash(str4));
		assertEquals(str5, hash5, NameServer.getHash(str5));
	}
}
