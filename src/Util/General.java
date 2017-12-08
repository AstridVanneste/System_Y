package Util;

public class General
{
	private static final int LINE_SEP_LENGTH = 160;

	public static void printLineSep ()
	{
		for (int i = 0; i < LINE_SEP_LENGTH; i++)
		{
			System.out.print('=');
		}
		System.out.print('\n');
	}
}
