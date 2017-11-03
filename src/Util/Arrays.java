package Util;

public class Arrays
{
	public static byte[] reverse (byte[] input)
	{
		byte[] reversed = new byte [input.length];

		for (int i = 0; i < input.length; i++)
		{
			reversed[reversed.length  - i - 1] = input[i];
		}

		return reversed;
	}
}
