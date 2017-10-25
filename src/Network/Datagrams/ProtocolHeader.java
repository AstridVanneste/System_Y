package Network.Datagrams;

import java.math.BigInteger;

/**
 * Created by Astrid on 24-10-2017.
 */


public class ProtocolHeader
{
	public static final int HEADER_LENGTH = 20;
	public static final int VERSION_LENGTH = 1;
	public static final int DATA_LENGTH_LENGTH = 3;
	public static final int TRANSACTION_ID_LENGTH = 4;
	public static final int REQUEST_CODE_LENGTH = 2;
	public static final int REPLY_CODE_LENGTH = 2;


	public static final int DATA_LENGTH_MASK = 0x00FFFFFF;
	public static final int REQUEST_CODE_MASK = 0x0000FFFF;
	public static final int REPLY_CODE_MASK = 0x0000FFFF;

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

	public long getDataLength()
	{
		return this.dataLength;
	}

	public void setDataLength(int dataLength)
	{
		this.dataLength = dataLength & DATA_LENGTH_MASK;
	}

	public long getTransactionID()
	{
		return this.transactionID;
	}

	public void setTransactionID(int transactionID)
	{
		this.transactionID = transactionID;
	}

	public long getRequestCode()
	{
		return this.requestCode;
	}

	public void setRequestCode(short requestCode)
	{
		this.requestCode = requestCode & REQUEST_CODE_MASK;
	}

	public long getReplyCode()
	{
		return this.replyCode;
	}

	public void setReplyCode(short replyCode)
	{
		this.replyCode = replyCode & REPLY_CODE_MASK;
	}

	public void setHeader(byte[] header)
	{
		int offset = 0;

		this.version = header[0];

		offset += VERSION_LENGTH;

		this.dataLength = new BigInteger(this.getSubArray(header, offset, DATA_LENGTH_LENGTH)).intValue();

		offset += DATA_LENGTH_LENGTH;

		this.transactionID = new BigInteger(this.getSubArray(header,offset, TRANSACTION_ID_LENGTH)).intValue();

		offset += TRANSACTION_ID_LENGTH;

		this.requestCode = new BigInteger(this.getSubArray(header, offset, REQUEST_CODE_LENGTH)).shortValue();

		offset += REQUEST_CODE_LENGTH;

		this.replyCode = new BigInteger(this.getSubArray(header,offset,REPLY_CODE_LENGTH)).shortValue();

	}

	public byte[] getSubArray(byte[] array , int start, int length)
	{
		byte[] subarray = new byte[length];

		for(int i = 0; i<start+length; i++)
		{
			subarray[i] = array[start + i];
		}

		return subarray;
	}

	public String toString()
	{
		String string = "HEADER\n";

		string += "VERSION:	" + this.version;
		string += "DATALENGTH:	" + this.dataLength;
		string += "TRANSACTION ID:	" + this.transactionID;
		string += "REQUEST CODE:	" + this.requestCode;
		string += "REPLY CODE:	"	+ this.replyCode;

		return string;
	}

	public byte[] serialize()
	{
		byte[] serial = new byte[HEADER_LENGTH];

		int offset = 0;

		serial[0] = this.version;

		offset += VERSION_LENGTH;


		//byte[] bytes = this.dataLength.toByteArray();


		for(int i = 0; i < DATA_LENGTH_LENGTH; i++ )
		{
			//serial[ offset + i] = bytes[i];
		}

		//bytes = this.transactionID.toByteArray();

		for(int i = 0; i < TRANSACTION_ID_LENGTH; i++)
		{
			//serial[offset + i] = bytes[i];
		}

		//bytes = this.requestCode.toByteArray();

		for(int i = 0; i < REPLY_CODE_LENGTH; i++)
		{
			//serial[offset + i] = bytes[i];
		}

		return serial;


	}

}
