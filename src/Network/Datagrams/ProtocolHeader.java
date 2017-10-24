package Network.Datagrams;

/**
 * Created by Astrid on 24-10-2017.
 */
public class ProtocolHeader
{
	public static final int HEADER_LENGTH = 20;
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
}
