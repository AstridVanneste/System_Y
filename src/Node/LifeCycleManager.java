package Node;

import IO.Network.Constants;
import IO.Network.Datagrams.Datagram;
import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.UDP.Multicast.Subscriber;
import IO.Network.UDP.Unicast.Client;
import NameServer.NameServer;
import NameServer.ResolverInterface;
import NameServer.ShutdownAgentInterface;
import Util.Serializer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Astrid on 07/11/2017.
 */
public class LifeCycleManager implements NodeInteractionInterface, Runnable
{
	private String name;
	private String nsIp;
	private short id;
	private short previousNeighbour;
	private short nextNeighbour;
	private Subscriber subscriber;
	private ResolverInterface resolverStub;
	private ShutdownAgentInterface shutdownStub;
	private int startupTransactionId;
	private boolean newNode;

	public LifeCycleManager()
	{
		this.newNode = true;
		this.resolverStub = null;
		this.shutdownStub = null;
		this.id = -1;
		this.name = "";
		this.previousNeighbour = -1;
		this.nextNeighbour = -1;
	}

	/**
	 * Gracefully removes the node from the system.
	 */
	public void shutdown()
	{
		String IPprevious = "";
		String IPnext = "";

		try
		{
			IPprevious = resolverStub.getIP(previousNeighbour);
			IPnext = resolverStub.getIP(nextNeighbour);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}


		try
		{
			Registry registryPrev = LocateRegistry.getRegistry(IPprevious);
			NodeInteractionInterface previousNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			previousNode.setNextNeighbour(nextNeighbour);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
		}


		try
		{
			Registry registryNext = LocateRegistry.getRegistry(IPnext);
			NodeInteractionInterface nextNode = (NodeInteractionInterface) registryNext.lookup(Node.NODE_INTERACTION_NAME);
			nextNode.setPreviousNeighbour(previousNeighbour);
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			//CALL FAILURE
		}


		try
		{
			shutdownStub.requestShutdown(id);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}
	}
	public void start()
	{
		System.out.println("give me a name");
		Scanner scanner = new Scanner(System.in);
		setName(scanner.nextLine());


		accessRequest();

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Multicast for NS
	 * NS will process this message and has to confirm that the node may access the network
	 */
	public void run()
	{
		while(true)
		{
			multicastListener();
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}


	/**
	 * send a multicast message requezsting to be added to network
	 */
	public void accessRequest () {
		System.out.println("Make accessrequest");

		Client udpClient = new Client();
		udpClient.start();

		byte version = (byte) 0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;
		Random rand = new Random();

		this.startupTransactionId = rand.nextInt()%127;

		int dataLength = name.length() + 8;
		ProtocolHeader header = new ProtocolHeader(version, dataLength, startupTransactionId, requestCode, replyCode);

		byte[] data = new byte[name.length() + 8];
		byte[] nameLengthInByte = Serializer.intToBytes(name.length());
		byte[] nameInByte = name.getBytes();

		byte[] ipInByte = new byte[4];

		try
		{
			ipInByte = Serializer.ipStringToBytes(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		}


		System.arraycopy(nameLengthInByte, 0, data, 0, nameLengthInByte.length);
		System.arraycopy(nameInByte, 0, data, 4, nameInByte.length);
		System.arraycopy(ipInByte, 0, data, nameInByte.length + 4, ipInByte.length);

		Datagram datagram = new Datagram(header, data);
		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize());
		udpClient.stop();

		System.out.println("sent accessrequest");
	}


	/**
	 * Old nodes and new nodes listen on multicast for the acceptance of a new node
	 * Old nodes will set their new neighbour (new node) with RMI
	 * New node will update his number of nodes and wait until his neighbours has called his rmi-methods
	 *
	 */
	public void multicastListener()
	{
		if(subscriber.hasData())
		{
			System.out.println("data received!");

			DatagramPacket packet = subscriber.receivePacket();

			Datagram request = new Datagram(packet.getData());
			System.out.println(request.getHeader().toString());

			byte[] data = request.getData();

			for(byte b: data)
			{
				System.out.print(b + " ");
			}

			short newNodeID = Serializer.bytesToShort(new byte [] {data[0],data[1]});
			short numberOfNodes = Serializer.bytesToShort(new byte[] {data[2],data[3]});

			if (!newNode)
			{
				if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED)
				{
					changeNeighbours(newNodeID);
				}
			}
			if (newNode)
			{
				//check if message contains error message duplicate id from NS
				if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_ID) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					askNewName();
					accessRequest();
				}

				//check if message contains error message duplicate id from NS
				if ((request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_IP) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					System.out.println("ip already exists!");
				}

				//check if successfully added to NS
				if ((request.getHeader().getReplyCode() == (int)ProtocolHeader.REPLY_SUCCESSFULLY_ADDED) &&
						(request.getHeader().getTransactionID() == this.startupTransactionId))
				{
					this.id = newNodeID;
					this.nsIp = packet.getAddress().getHostAddress();
					nameServerBind(nsIp);
					//nameserver sends the amount of nodes in the tree
					if(numberOfNodes == 0)
					{
						this.nextNeighbour = this.id;
						this.previousNeighbour = this.id;
					}
					newNode = false;
					System.out.println("toegevoegd");
				}
			}
		}
	}

	public void nameServerBind(String nsIp){
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}
		Registry reg = null;
		try
		{
			reg = LocateRegistry.getRegistry(nsIp);
			this.resolverStub = (ResolverInterface) reg.lookup(NameServer.RESOLVER_NAME);
			this.shutdownStub = (ShutdownAgentInterface) reg.lookup((NameServer.SHUTDOWN_AGENT_NAME));
		} catch (RemoteException e)
		{
			e.printStackTrace();
		} catch (NotBoundException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 */
	private synchronized void changeNeighbours(short newID)
	{
		// You'r the first node so edit both neighbours of the new node
		if(this.id == this.nextNeighbour && this.id == this.previousNeighbour)
		{
			this.nextNeighbour = newID;
			this.previousNeighbour = newID;
			Registry reg = null;
			try
			{
				NodeInteractionInterface neighbourInterface = null;
				reg = LocateRegistry.getRegistry(resolverStub.getIP(newID));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setNextNeighbour(id);
				neighbourInterface.setPreviousNeighbour(id);
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}catch (NotBoundException e)
			{
				e.printStackTrace();
			}
		}

		// The node will change the previous and next id of the new node in following cases:
		//you are the previous of the new node and need to change the neighbours of the new node and your old next neighbour
		if (
				(		(newID > this.id && newID > this.nextNeighbour && this.nextNeighbour < this.id) ||
						(newID > this.id && newID < this.nextNeighbour && this.nextNeighbour > this.id) ||
						(newID < this.id && newID < this.nextNeighbour && this.nextNeighbour < this.id)

				))
		{
			Registry reg = null;
			try
			{
				NodeInteractionInterface neighbourInterface = null;
				reg = LocateRegistry.getRegistry(this.resolverStub.getIP(newID));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setNextNeighbour(this.nextNeighbour);
				neighbourInterface.setPreviousNeighbour(this.id);

				reg = LocateRegistry.getRegistry(this.resolverStub.getIP(this.nextNeighbour));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setPreviousNeighbour(newID);
				this.nextNeighbour = newID;

			} catch (RemoteException e)
			{
				e.printStackTrace();
			} catch (NotBoundException e)
			{
				e.printStackTrace();
			}

		}
	}

	/**
	 * Subscribe this node on the multicast-address 224.0.0.1
	 */
	public void subscribeOnMulticast ()
	{
		this.subscriber = new Subscriber(Constants.DISCOVERY_MULTICAST_IP,Constants.DISCOVERY_CLIENT_PORT);
		this.subscriber.start();
	}

	/**
	 * Unsubscribe this node on the multicast-address 224.0.0.1
	 */
	public void unsubscribeMulticast ()
	{
		this.subscriber.stop();
	}


	/**
	 * at startup, set the neighbours as your own
	 */
	private void setNeighbours()
	{
		this.previousNeighbour = this.id;
		this.nextNeighbour = this.id;
	}

	private void askNewName ()
	{
		System.out.println("Please enter a new name: ");
		Scanner scanner = new Scanner(System.in);
		this.name = scanner.nextLine();
		this.id = NameServer.getHash(name);
	}

	public String getName()
	{
		return this.name;
	}


	public void setName(String name)
	{
		this.name = name;
	}

	public short getPreviousNeighbour() throws RemoteException
	{
		return this.previousNeighbour;
	}

	public void setPreviousNeighbour(short previousNeighbour) throws RemoteException
	{
		this.previousNeighbour = previousNeighbour;
	}

	public short getNextNeighbour() throws RemoteException
	{
		return this.nextNeighbour;
	}

	public void setNextNeighbour(short nextNeighbour) throws RemoteException
	{
		this.nextNeighbour = nextNeighbour;
	}

	public ResolverInterface getResolverStub()
	{
		return this.resolverStub;
	}

	public ShutdownAgentInterface getShutdownStub()
	{
		return this.shutdownStub;
	}

	public short getId()
	{
		return this.id;
	}

	public void setId(short id)
	{
		this.id = id;
	}
}
