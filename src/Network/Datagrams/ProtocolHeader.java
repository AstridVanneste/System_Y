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


	public static final int DATA_LENGTH_MASK = 0x0111;

	private byte version;
	private int dataLength;
	private int transactionID;
	private short requestCode;
	private short replyCode;

	public ProtocolHeader()
	{

	}

	public ProtocolHeader(byte version, int dataLength,int transactionID, short requestCode, short replyCode)
	{
		this.version = version;
		this.dataLength = dataLength & DATA_LENGTH_MASK;
		this.transactionID = transactionID;
		this.requestCode = requestCode;
		this.replyCode = replyCode;
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
		this.requestCode = requestCode;
	}

	public long getReplyCode()
	{
		return this.replyCode;
	}

	public void setReplyCode(short replyCode)
	{
		this.replyCode = replyCode;
	}

	public void setHeader(byte[] header)
	{
		int offset = 0;

		for(int i = 0;i<offset + VERSION_LENGTH; i++)
		{
			this.version = header[offset+i];
		}

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
}
