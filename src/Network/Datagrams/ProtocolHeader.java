package Network.Datagrams;

import java.math.BigInteger;

/**
 * Created by Astrid on 24-10-2017.
 */


public class ProtocolHeader
{
	static final int HEADER_LENGTH = 12;
	private static final int VERSION_LENGTH = 1;
	private static final int DATA_LENGTH_LENGTH = 3;
	private static final int TRANSACTION_ID_LENGTH = 4;
	private static final int REQUEST_CODE_LENGTH = 2;
	private static final int REPLY_CODE_LENGTH = 2;


	private static final int DATA_LENGTH_MASK = 0x00FFFFFF;
	private static final int REQUEST_CODE_MASK = 0x0000FFFF;
	private static final int REPLY_CODE_MASK = 0x0000FFFF;

	public static final int REQUEST_DISCOVERY_CODE = 0x00000000;
	public static final int REQUEST_CLUSTER_HEALTH_REPORT = 0x00000001;
	public static final int REPLY_SUCCESSFULLY_ADDED = 0x00000000;
	public static final int REPLY_DUPLICATE_ID = 0x00000001;
	public static final int REPLY_DUPLICATE_IP = 0x00000002;
	public static final int REPLY_NODE_UP = 0x00000003;
	public static final int REPLY_NODE_DOWN = 0x00000004;

	private byte version;
	private int dataLength;
	private int transactionID; 
	private int requestCode;
	private int replyCode;

	public ProtocolHeader()
	{

	}

	public ProtocolHeader(byte version, int dataLength,int transactionID, short requestCode, short replyCode)
	{
		this.version = version;
		this.dataLength = dataLength & DATA_LENGTH_MASK;
		this.transactionID = transactionID;
		this.requestCode = requestCode & REQUEST_CODE_MASK;
		this.replyCode = replyCode & REPLY_CODE_MASK;
	}

	public ProtocolHeader(byte[] header)
	{
		this.setHeader(header);
	}

	public byte getVersion()
	{
		return this.version;
	}

	public void setVersion(byte version)
	{
		this.version = version;
	}

	public int getDataLength()
	{
		return this.dataLength;
	}

	public void setDataLength(int dataLength)
	{
		this.dataLength = dataLength & DATA_LENGTH_MASK;
	}

	public int getTransactionID()
	{
		return this.transactionID;
	}

	public void setTransactionID(int transactionID)
	{
		this.transactionID = transactionID;
	}

	public int getRequestCode()
	{
		return this.requestCode;
	}

	public void setRequestCode(int requestCode)
	{
		this.requestCode = requestCode & REQUEST_CODE_MASK;
	}

	public int getReplyCode()
	{
		return this.replyCode;
	}

	public void setReplyCode(int replyCode)
	{
		this.replyCode = replyCode & REPLY_CODE_MASK;
	}

	public void setHeader(byte[] header)
	{
		int offset = 0;

		this.version = header[0];

		offset += VERSION_LENGTH;

		byte[] bytes = new byte[4];
		int i = 0;

		for(byte b: this.getSubArray(header,offset, DATA_LENGTH_LENGTH))
		{
			bytes[i] = b;
			i++;
		}

		this.dataLength = byteArrayToInt(bytes);

		offset += DATA_LENGTH_LENGTH;

		this.transactionID = byteArrayToInt(this.getSubArray(header, offset, TRANSACTION_ID_LENGTH));

		offset += TRANSACTION_ID_LENGTH;

		this.requestCode = byteArrayToShort(this.getSubArray(header, offset, REQUEST_CODE_LENGTH));

		offset += REQUEST_CODE_LENGTH;

		this.replyCode = byteArrayToShort(this.getSubArray(header,offset,REPLY_CODE_LENGTH));

	}

	public byte[] getSubArray(byte[] array , int offset, int length)
	{
		byte[] subarray = new byte[length];

		//System.out.println("LENGTH " + length + " START " + start + " ARRAY SIZE " + array.length);

		for(int i = 0; i< length; i++)
		{
			//System.out.println("OFFSET " + offset + " I " + i + " INDEX " + (i + offset) + " VALUE " + array[offset + i]);
			subarray[i] = array[offset + i];

		}

		return subarray;
	}

	public String toString()
	{
		String string = "HEADER\n";

		string += "VERSION:		" + this.version + "\n";
		string += "DATALENGTH:		" + this.dataLength + "\n";
		string += "TRANSACTION ID:	" + this.transactionID + "\n";
		string += "REQUEST CODE:	" + this.requestCode + "\n";
		string += "REPLY CODE:		"	+ this.replyCode + "\n";

		return string;
	}

	public byte[] serialize()
	{
		byte[] serial = new byte[HEADER_LENGTH];

		int offset = 0;

		serial[0] = this.version;

		offset += VERSION_LENGTH;


		byte[] bytes = intToByteArray(this.dataLength);


		for(int i = 0; i < DATA_LENGTH_LENGTH; i++ )
		{
			serial[offset + i] = bytes[i];
		}

		offset += DATA_LENGTH_LENGTH;


		bytes = intToByteArray(this.transactionID);

		for(int i = 0; i < TRANSACTION_ID_LENGTH; i++)
		{
			serial[offset + i] = bytes[3-i];
		}

		offset += TRANSACTION_ID_LENGTH;

		bytes = intToByteArray(this.requestCode);

		for(int i = 0; i < REQUEST_CODE_LENGTH; i++)
		{
			serial[offset + i] = bytes[1-i];
		}

		offset += REQUEST_CODE_LENGTH;

		bytes = intToByteArray(this.replyCode);

		for(int i = 0; i < REPLY_CODE_LENGTH; i++)
		{
			serial[offset + i ] = bytes[1-i];
		}

		return serial;


	}


	public static byte[] intToByteArray(int value)
	{
		byte[] result = new byte[4];

		result[0] = (byte)(value & 0x000000FF);
		result[1] = (byte)((value >>> 8)& 0x000000FF);
		result[2] = (byte)((value >>> 16) & 0x000000FF);
		result[3] = (byte)((value >>> 24)& 0x000000FF);

		return result;
	}

	public static int byteArrayToInt (byte[] data)
	{
		for(int i = 0; i< data.length; i++)
		{
			//System.out.println("BYTE " + i + " VALUE " + data[i]);
		}
		return (data[3]) | (data[2] << 8) | (data[1] << 16) | (data[0] << 24);
	}

	public static short byteArrayToShort(byte[] data)
	{
		for(int i = 0; i< data.length; i++)
		{
			//System.out.println("BYTE " + i + " VALUE " + data[i]);
		}
		return (short) ((data[1]) | (data[0] << 8));
	}

}
