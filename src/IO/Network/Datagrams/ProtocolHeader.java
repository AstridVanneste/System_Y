package IO.Network.Datagrams;


import Util.Serializer;

import java.util.Arrays;

/**
 * ProtocolHeader is the standard header for all messages send over the network in System_Y
 */
public class ProtocolHeader
{
	public static final byte CURRENT_VERSION = 1;
	public static final int HEADER_LENGTH = 12;

	private static final int VERSION_LENGTH = 1;
	private static final int DATA_LENGTH_LENGTH = 3;
	private static final int TRANSACTION_ID_LENGTH = 4;
	private static final int REQUEST_CODE_LENGTH = 2;
	private static final int REPLY_CODE_LENGTH = 2;


	private static final int DATA_LENGTH_MASK = 0x00FFFFFF;
	private static final int REQUEST_CODE_MASK = 0x0000FFFF;
	private static final int REPLY_CODE_MASK = 0x0000FFFF;

	//REQUEST CODES
	public static final int REQUEST_DISCOVERY_CODE = 0x00000001;
	public static final int REQUEST_FILE = 0x00004001;


	//REPLY CODES
	public static final int NO_REPLY = 0x00000000;
	public static final int REPLY_SUCCESSFULLY_ADDED = 0x00000001;
	public static final int REPLY_DUPLICATE_ID = 0x00000002;
	public static final int REPLY_DUPLICATE_IP = 0x00000003;
	public static final int REPLY_FILE = 0x00004001;
	public static final int REPLY_FILE_END = 0x00004002;

	private byte version;
	private int dataLength;
	private int transactionID;
	private int requestCode;
	private int replyCode;


	/**
	 * Empty constructor: fields get no values!!
	 * Only use this if you set the fields immediately afterwards
	 */
	public ProtocolHeader()
	{

	}

	/**
	 * Copy constructor
	 * @param header
	 */
	public ProtocolHeader(ProtocolHeader header)
	{
		this.version = header.getVersion();
		this.dataLength = header.getDataLength();
		this.transactionID = header.getTransactionID();
		this.requestCode = header.getRequestCode();
		this.replyCode = header.getReplyCode();
	}

	/**
	 * Constructor where you provide all fields immediately.
	 * @param version
	 * @param dataLength
	 * @param transactionID
	 * @param requestCode
	 * @param replyCode
	 */
	public ProtocolHeader(byte version, int dataLength,int transactionID, int requestCode, int replyCode)
	{
		this.version = version;
		this.dataLength = dataLength & DATA_LENGTH_MASK;
		this.transactionID = transactionID;
		this.requestCode = requestCode & REQUEST_CODE_MASK;
		this.replyCode = replyCode & REPLY_CODE_MASK;
	}

	/**
	 * Constructor to make a header from an array of bytes. This array will be split to
	 * correctly set all the fields.
	 * @param header
	 */
	public ProtocolHeader(byte[] header)
	{
		int offset = 0;

		this.version = header[0];

		offset += VERSION_LENGTH;

		byte[] bytes = new byte[4];
		int i = 0;

		for(byte b: Arrays.copyOfRange(header, offset, offset + VERSION_LENGTH))
		{
			bytes[i] = b;
			i++;
		}

		this.dataLength = Serializer.bytesToInt(bytes);

		offset += DATA_LENGTH_LENGTH;

		this.transactionID =  Serializer.bytesToInt(Arrays.copyOfRange(header, offset, offset + TRANSACTION_ID_LENGTH));

		offset += TRANSACTION_ID_LENGTH;

		this.requestCode = Serializer.bytesToShort(Arrays.copyOfRange(header, offset, offset + REQUEST_CODE_LENGTH));

		offset += REQUEST_CODE_LENGTH;

		this.replyCode = Serializer.bytesToShort(Arrays.copyOfRange(header, offset, offset + REPLY_CODE_LENGTH));
	}

	/**
	 * returns version of the header
	 * @return version
	 */
	public byte getVersion()
	{
		return this.version;
	}


	/**
	 * sets version field to a given value
	 * @param version
	 */
	public ProtocolHeader setVersion(byte version)
	{
		this.version = version;
		return this;
	}

	/**
	 * returns data length of the header
	 * @return dataLength
	 */
	public int getDataLength()
	{
		return this.dataLength;
	}


	/**
	 * sets dataLength field to a given value
	 * @param dataLength()
	 */
	public ProtocolHeader setDataLength(int dataLength)
	{
		//System.out.println("Got length " + Integer.toHexString(dataLength));
		this.dataLength = dataLength & DATA_LENGTH_MASK;
		//System.out.println("After masking " + Integer.toHexString(this.dataLength));
		return this;
	}

	/**
	 * returns transaction ID of the header
	 * @return transactionID
	 */
	public int getTransactionID()
	{
		return this.transactionID;
	}


	/**
	 * sets transactionID field to a given value
	 * @param transactionID
	 */
	public ProtocolHeader setTransactionID(int transactionID)
	{
		this.transactionID = transactionID;
		return this;
	}

	/**
	 * returns request code of the header
	 * @return requestCode
	 */
	public int getRequestCode()
	{
		return this.requestCode;
	}


	/**
	 * sets requestCode field to a given value
	 * @param requestCode
	 */
	public ProtocolHeader setRequestCode(int requestCode)
	{
		this.requestCode = requestCode & REQUEST_CODE_MASK;
		return this;
	}

	/**
	 * returns reply code of the header
	 * @return reply code
	 */
	public int getReplyCode()
	{
		return this.replyCode;
	}

	/**
	 * sets replyCode field to a given value
	 * @param replyCode
	 */
	public ProtocolHeader setReplyCode(int replyCode)
	{
		this.replyCode = replyCode & REPLY_CODE_MASK;
		return this;
	}


	/**
	 * Returns a String with all the fields in an easy to read format
	 * @return
	 */
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

	/**]
	 * return a serial version of all the fields
	 * @return
	 */
	public byte[] serialize()
	{
		byte[] serial = new byte[HEADER_LENGTH];

		int offset = 0;

		serial[0] = this.version;

		offset += VERSION_LENGTH;


		byte[] bytes = Util.Serializer.intToBytes(this.dataLength);


		for(int i = 0; i < DATA_LENGTH_LENGTH; i++ )
		{
			serial[offset + i] = bytes[i];
		}

		offset += DATA_LENGTH_LENGTH;


		bytes = Util.Serializer.intToBytes(this.transactionID);

		for(int i = 0; i < TRANSACTION_ID_LENGTH; i++)
		{
			serial[offset + i] = bytes[i];
		}

		offset += TRANSACTION_ID_LENGTH;

		bytes = Util.Serializer.intToBytes(this.requestCode);

		for(int i = 0; i < REQUEST_CODE_LENGTH; i++)
		{
			serial[offset + i] = bytes[i+2];
		}

		offset += REQUEST_CODE_LENGTH;

		bytes = Util.Serializer.intToBytes(this.replyCode);

		for(int i = 0; i < REPLY_CODE_LENGTH; i++)
		{
			serial[offset + i ] = bytes[i+2];
		}

		return serial;
	}

}
