package Network.Datagrams;

/**
 * Created by Astrid on 24-10-2017.
 */
public class Datagram
{
	private ProtocolHeader header;
	private byte[] data;


	public Datagram (ProtocolHeader header, byte[] data)
	{
		this.header = header;
		this.data = data;
	}

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
	}

	public ProtocolHeader getHeader()
	{
		return this.header;
	}

	public void setHeader(ProtocolHeader header)
	{
		this.header = header;
	}

	public byte[] getData()
	{
		return this.data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}
}
