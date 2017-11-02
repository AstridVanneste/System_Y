package IO;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Long.min;

public class File
{
	private String filename;
	private long filePos;

	public File (String filename)
	{
		this.filename = filename;
		this.filePos = 0;
	}

	/**
	 * Uses the new Java NIO API to read files as fast as possible
	 * The maximum filesize is Integer.MAX_VALUE bytes (So about 4GB).
	 * @return      A byte array containing all bytes of the file we read.
	 * @throws IOException  An IOException can be thrown by the FileChannel.read() method, or because the file is too large.
	 */
	public byte[] read ()   throws
			IOException
	{
		FileInputStream inputStream = new FileInputStream(this.filename);

		FileChannel channel = inputStream.getChannel();

		if (channel.size() > Integer.MAX_VALUE)
		{
			throw new IOException("File is to large( " + Long.toString(channel.size()) + " bytes), maximum file size is " + Integer.toString(Integer.MAX_VALUE) + " bytes ( " + Integer.toString(Integer.MAX_VALUE / (1024 * 1024 * 1024)) + " Gigabytes)");
		}

		ByteBuffer buffer =  ByteBuffer.allocate((int) min(channel.size(), Integer.MAX_VALUE));

		while (channel.position() < channel.size())
		{
			channel.read(buffer);
		}

		this.filePos = channel.size();

		channel.close();

		inputStream.close();

		return buffer.array();
	}

	/**
	 * Uses the new Java NIO API to read files as fast as possible
	 * The maximum filesize is Integer.MAX_VALUE bytes (So about 4GB).
	 * @param numBytes  The amount of bytes to read.
	 * @return      A byte array containing all bytes of the file we read.
	 * @throws IOException  An IOException can be thrown by the FileChannel.read() method, or because the file is too large.
	 */
	public byte[] read (int numBytes)   throws
			IOException
	{
		FileInputStream inputStream = new FileInputStream(this.filename);

		FileChannel channel = inputStream.getChannel();

		if (channel.size() > Integer.MAX_VALUE)
		{
			throw new IOException("File is to large( " + Long.toString(channel.size()) + " bytes), maximum file size is " + Integer.toString(Integer.MAX_VALUE) + " bytes ( " + Integer.toString(Integer.MAX_VALUE / (1024 * 1024 * 1024)) + " Gigabytes)");
		}

		channel = channel.position(this.filePos);

		ByteBuffer buffer =  ByteBuffer.allocate(numBytes);

		//while (channel.position() < channel.size())
		//{
		channel.read(buffer);
		//}

		this.filePos = min(this.filePos + numBytes, channel.size());

		channel.close();

		inputStream.close();

		return buffer.array();
	}

	/**
	 * Uses the new Java NIO API to write files as fast as possible.
	 * Creates a file if it does not yet exist.
	 * @throws IOException An IOException can be thrown by the FileOutputStream Constructor, the FileChannel.write or the FileChannel.close method.
	 */
	public void write (byte[] data) throws
			IOException
	{
		FileOutputStream outputStream = new FileOutputStream(this.filename);

		FileChannel channel = outputStream.getChannel();

		channel.write(ByteBuffer.wrap(data));

		channel.close();

		outputStream.close();
	}

	/**
	 * Uses the new Java NIO API to append to files as fast as possible.
	 * @param data  The data to be appended to the file.
	 * @throws IOException An IOException can be thrown by the FileOutputStream Constructor, the FileChannel.write or the FileChannel.close method.
	 */
	public void append (byte[] data)	throws
			IOException
	{
		FileOutputStream outputStream = new FileOutputStream(this.filename, true);

		FileChannel channel = outputStream.getChannel();

		channel.position(channel.size());

		channel.write(ByteBuffer.wrap(data));

		channel.close();

		outputStream.close();
	}

	/**
	 * Returns the size of the associated channel, in bytes.
	 * @return
	 * @throws IOException
	 */
	public long size () throws IOException
	{
		FileInputStream inputStream = new FileInputStream(this.filename);

		long size = inputStream.getChannel().size();

		inputStream.close();

		return size;
	}

	/**
	 *	Returns the amount of bytes that can still be read from a file
	 *	@return The amount of bytes that can still be read from the file.
	 *	@throws IOException
	 */
	public int available () throws IOException
	{
		FileInputStream inputStream = new FileInputStream(this.filename);
		FileChannel channel = inputStream.getChannel();
		int avail =  (int) min(Integer.MAX_VALUE,channel.size()) - (int) this.filePos;
		channel.close();
		inputStream.close();
		return avail;
	}

	/**
	 * Segments a file in a number of segments of segmentSize bytes.
	 * @param input         The input data to be split into segments.
	 * @param segmentSize   The size of an individual segment.
	 * @return              An array of segments, each with a size of segmentSize
	 */
	public static byte[][] segment (byte[] input, int segmentSize)
	{
		int numSegments = (int) Math.floor(input.length / segmentSize) + 1;
		byte[][] result = new byte [numSegments][segmentSize];

		for (int i = 0; i < numSegments; i++)
		{
			for (int j = 0; j < segmentSize; j++)
			{
				if (((i * segmentSize) + j) >= input.length)
				{
					result[i][j] = 0;
				}
				else
				{
					result[i][j] = input[(i * segmentSize) + j];
				}
			}
		}

		return result;
	}

	public boolean exists ()
	{
		return (new File(this.filename)).exists();
	}

	public String toString () {
		try
		{
			return "File: " + this.filename + "\n" + "Position: " + Long.toString(this.filePos) + "\n" + "Available: " + Integer.toString(this.available());
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return "File: " + this.filename + "\n" + "Position: " + Long.toString(this.filePos);
		}
	}

	public static String mapToCSV(Map<Short, String> map)
	{
		String CSV = "";

		Set<Short> keySet = map.keySet();

		for(short ID: keySet)
		{
			CSV += ID + ";";
			CSV += map.get(ID) + "\n";
		}
		return CSV;
	}

	public static TreeMap<Short,String> CSVToMap(String CSV)
	{
		TreeMap<Short, String> map = new TreeMap<>();

		for(String s: CSV.split("\n"))
		{
			String[] KV = s.split(";");
			map.put(Short.parseShort(KV[0]),KV[1]);
		}


		return map;
	}
}
