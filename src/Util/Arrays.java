package Util;

public class Arrays
{
	/**
	 * I would make this generic, but generics in Java suck ass.
	 * Long live the one, true, programming language, C++, and its holy templates.
	 * @param original
	 * @return
	 */
	@Deprecated
	public static byte[] reverse (byte[] original)
	{
		byte reversed [] = new byte [original.length];

		for (int i = 0; i < original.length; i++)
		{
			reversed[reversed.length - i - 1] = original[i];
		}

		return reversed;
	}
}
