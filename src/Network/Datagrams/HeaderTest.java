package Network.Datagrams;

public class HeaderTest
{
	public static void main (String[] args)
	{
		byte[] num = new byte[] {0,1,0,0};
		 int res = Datagram.byteArrayToInt(num);
		 System.out.println("Num: " + res);
	}
}
