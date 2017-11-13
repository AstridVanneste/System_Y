package Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Serializer
{
	public static short[] ipStringToBytes (String ip)
	{
		String[] sepBytes = ip.split("\\.");

		if (sepBytes.length != 4)
		{
			throw new RuntimeException("IP address '" + ip + "' wasn't formatted properly, expected IPv4 Address: 'www.xxx.yyy.zzz'");
		}

		short[] data = new short [4];
		for (int i = 0; i < 4; i++)
		{
			data[i] = Short.parseShort(sepBytes[i]);
		}
		return data;
	}

	public static String bytesToIPString (short[] ipBytes)
	{
		if (ipBytes.length != 4)
		{
			throw new RuntimeException("Only IPv4 Addresses are supported, the given address had a length of " + Integer.toString(ipBytes.length) + " instead of 4.");
		}
		return Short.toString(ipBytes[0]) + "." + Short.toString(ipBytes[1]) + "." + Short.toString(ipBytes[2]) + "." + Short.toString(ipBytes[3]);
	}

	public static byte[] intToBytes (int value)
	{
		byte[] result = new byte[4];

		result[0] = (byte)((value >>> 24) & 0x000000FF);
		result[1] = (byte)((value >>> 16) & 0x000000FF);
		result[2] = (byte)((value >>> 8) & 0x000000FF);
		result[3] = (byte)(value & 0x000000FF);
		return result;
	}

	public static int bytesToInt(byte[] data)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(data[3]);
		buffer.put(data[2]);
		buffer.put(data[1]);
		buffer.put(data[0]);
		return buffer.getInt(0);
	}

	public static byte[] shortToBytes (short s)
	{
		byte[] result = new byte [2];

		result[0] = (byte) ((s >>> 8) & 0x00FF);
		result[1] = (byte) (s & 0x00FF);

		return result;
	}

	public static short bytesToShort(byte[] data)
	{
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(data[1]);
		buffer.put(data[0]);
		return buffer.getShort(0);
	}
}