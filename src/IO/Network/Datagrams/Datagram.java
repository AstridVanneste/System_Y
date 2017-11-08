package IO.Network.Datagrams;

/**
 * Datagram combines a ProtocolHeader and data into a single serializable entity.
 */
public class Datagram
{
	private ProtocolHeader header;
	private byte[] data;


	/**
	 * Creates a new datagram from a header and the data
	 * @param header
	 * @param data
	 */
	public Datagram (ProtocolHeader header, byte[] data)
	{
		this.header = header;
		this.data = data;
		this.header.setDataLength(this.data.length);
	}

	/**
	 * Creates a new datagram from only a head. The data field will be empty.
	 * @param header
	 */
	public Datagram (ProtocolHeader header)
	{
		this.header = header;
		this.data = new byte [0];
		this.header.setDataLength(this.data.length);
	}

	/**
	 * Creates a new datagram from a serialized datagram.
	 * @param datagram
	 */
	public Datagram(byte[] datagram)
	{
		byte[] header = new byte[ProtocolHeader.HEADER_LENGTH];

		for(int i = 0; i< ProtocolHeader.HEADER_LENGTH; i++)
		{
			header[i] = datagram[i];
		}

		this.header = new ProtocolHeader(header);

		byte[] data = new byte[datagram.length - ProtocolHeader.HEADER_LENGTH];

		for(int i = 0; i<datagram.length-ProtocolHeader.HEADER_LENGTH;i++)
		{
			data[i] = datagram[i+ProtocolHeader.HEADER_LENGTH];
		}

		this.data = data;

		this.header.setDataLength(this.data.length);
	}

	/**
	 * Returns the header of the datagram
	 * @return
	 */
	public ProtocolHeader getHeader()
	{
		return this.header;
	}

	/**
	 * Sets the header to the given value
	 * @param header
	 */
	public void setHeader(ProtocolHeader header)
	{
		this.header = header;
		this.header.setDataLength(this.data.length);
	}

	/**
	 * Returns the data from the datagram
	 * @return
	 */
	public byte[] getData()
	{
		return this.data;
	}

	/**
	 * Sets the data
	 * @param data
	 */
	public void setData(byte[] data)
	{
		this.data = data;
		this.header.setDataLength(this.data.length);
	}

	/**
	 * Returns a serialized version of the header.
	 * @return
	 */
	public byte[] serialize()
	{

		byte[] serial = new byte[data.length + ProtocolHeader.HEADER_LENGTH];

		byte[] header = this.header.serialize();

		int i = 0;

		for(byte b: header)
		{
			serial[i] = b;
			i++;
		}
		for(byte b: this.data)
		{
			serial[i] = b;
			i++;
		}
		int test = (int)(serial[4]) | (serial[5] << 8) | (serial[6] << 16) | (serial[7] << 24);
		return serial;
	}
}
