package Util;

public class Serializer
{
	public static byte[] ipStringToBytes (String ip)
	{
		String[] sepBytes = ip.split("\\.");

		if (sepBytes.length != 4)
		{
			throw new RuntimeException("IP address '" + ip + "' wasn't formatted properly, expected IPv4 Address: 'www.xxx.yyy.zzz'");
		}

		byte[] data = new byte [4];
		for (int i = 0; i < 4; i++)
		{
			data[i] = (byte) Short.parseShort(sepBytes[i]);
		}
		return data;
	}

	public static String bytesToIPString (byte[] ipBytes)
	{
		if (ipBytes.length != 4)
		{
			throw new RuntimeException("Only IPv4 Addresses are supported, the given address had a length of " + Integer.toString(ipBytes.length) + " instead of 4.");
		}
		return Integer.toString(ipBytes[0]) + "." + Integer.toString(ipBytes[1]) + "." + Integer.toString(ipBytes[2]) + "." + Integer.toString(ipBytes[3]);
	}

	public static byte[] intToBytes (int value)
	{
		byte[] result = new byte[4];

		result[0] = (byte)(value & 0x000000FF);
		result[1] = (byte)((value >>> 8)& 0x000000FF);
		result[2] = (byte)((value >>> 16) & 0x000000FF);
		result[3] = (byte)((value >>> 24)& 0x000000FF);

		return result;
	}

	public static int bytesToInt(byte[] data)
	{
		for(int i = 0; i< data.length; i++)
		{
			//System.out.println("BYTE " + i + " VALUE " + data[i]);
		}
		return (data[3]) | (data[2] << 8) | (data[1] << 16) | (data[0] << 24);
	}

	public static byte[] shortToBytes (short s)
	{
		byte[] result = new byte [2];

		result[0] = (byte) (s & 0x00FF);
		result[1] = (byte) ((s >>> 8) & 0x00FF);

		return result;
	}

	public static short bytesToShort(byte[] data)
	{
		for(int i = 0; i< data.length; i++)
		{
			//System.out.println("BYTE " + i + " VALUE " + data[i]);
		}
		return (short) ((data[1]) | (data[0] << 8));
	}
}