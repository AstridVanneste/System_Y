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
public class LifeCycleManager implements Runnable
{
	private boolean running;
	private int bootstrapTransactionID;
	private Subscriber subscriber;
	private ShutdownAgentInterface shutdownStub;

	public LifeCycleManager()
	{
		this.running = false;
		this.bootstrapTransactionID = -1;
		this.subscriber = new Subscriber(Constants.DISCOVERY_MULTICAST_IP,Constants.DISCOVERY_CLIENT_PORT);
		this.shutdownStub = null;
	}

	public void start()
	{
		this.subscriber.start();

		this.running = true;

		Thread thread = new Thread(this);
		thread.start();

		this.bootstrapTransactionID = (new Random()).nextInt() & 0x7FFFFFFF;
		this.sendAccessRequest();
	}

	/**
	 * Old nodes and new nodes listen on multicast for the acceptance of a new node
	 * Old nodes will set their new neighbour (new node) with RMI
	 * New node will update his number of nodes and wait until his neighbours has called his rmi-methods
	 */
	@Override
	public void run()
	{
		while(this.running)
		{
			synchronized (this.subscriber)
			{
				//System.out.println("Loop");
				if (this.subscriber.hasData())
				{
					System.out.println("Got answer form nameserver");
					// Subscriber got some data
					// Start parsing bytes
					DatagramPacket packet = this.subscriber.receivePacket();

					Datagram request = new Datagram(packet.getData());

					if (request.getHeader().getTransactionID() == this.bootstrapTransactionID)
					{
						System.out.println("Transaction ID was ours");
						// If the transaction is not ours, we don't care
						byte[] data = request.getData();

						short newNodeID = Serializer.bytesToShort(new byte[]{data[0], data[1]});
						short numberOfNodes = Serializer.bytesToShort(new byte[]{data[2], data[3]});

						if (Node.getInstance().getId() != Node.DEFAULT_ID)
						{
							System.out.println("We're not a new node");
							// We're not a new node, check and if needed update neighbours
							if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED)
							{
								this.updateNeighbours(newNodeID);
							}
						} else if (Node.getInstance().getId() == Node.DEFAULT_ID)
						{
							System.out.println("We are a new node");
							// We are a new node, let's start setting neighbours

							//check if message contains error message duplicate id from NS
							if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_ID)
							{
								System.err.println("[ERROR]\tNameserver detected ID as being a duplicate, please restart the Node and set a unique name.");
								this.running = true; // Exit the 'inifinite' while loop
								continue;
							}

							//check if message contains error message duplicate id from NS
							if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_DUPLICATE_IP)
							{
								System.err.println("[ERROR]\tNameserver detected IP as being a duplicate, please fix your DHCP config or change your static IP.");
								this.running = true;
								continue;
							}

							//check if successfully added to NS
							if (request.getHeader().getReplyCode() == ProtocolHeader.REPLY_SUCCESSFULLY_ADDED)
							{
								Node.getInstance().setId(newNodeID);
								String nsIp = packet.getAddress().getHostAddress();

								// Set up connection(s) to NameServer
								this.bindNameserverStubs(nsIp);

								//nameserver sends the amount of nodes in the tree
								if (numberOfNodes == 0)
								{
									Node.getInstance().setNextNeighbour(Node.getInstance().getId());
									Node.getInstance().setPreviousNeighbour(Node.getInstance().getId());
								}

								this.bootstrapTransactionID = -1;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * send a multicast message requesting to be added to network
	 */
	private void sendAccessRequest ()
	{
		Client udpClient = new Client();
		udpClient.start();

		byte version = (byte) 0;
		short replyCode = (short) 0;
		short requestCode = ProtocolHeader.REQUEST_DISCOVERY_CODE;

		int dataLength = Node.getInstance().getName().length() + 8;
		ProtocolHeader header = new ProtocolHeader(version, dataLength, this.bootstrapTransactionID, requestCode, replyCode);

		byte[] data = new byte[Node.getInstance().getName().length() + 8];
		byte[] nameLengthInByte = Serializer.intToBytes(Node.getInstance().getName().length());
		byte[] nameInByte = Node.getInstance().getName().getBytes();

		short[] ipInByte = new short[4];

		try
		{
			ipInByte = Serializer.ipStringToBytes(InetAddress.getLocalHost().getHostAddress());
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		System.arraycopy(nameLengthInByte, 0, data, 0, nameLengthInByte.length);
		System.arraycopy(nameInByte, 0, data, 4, nameInByte.length);

		for (int i = nameInByte.length + 4; i < nameInByte.length + 8; i++)
		{
			data[i] = (byte) (ipInByte[i - (nameInByte.length + 4)] & 0x00FF);
		}

		Datagram datagram = new Datagram(header, data);
		udpClient.send(Constants.DISCOVERY_MULTICAST_IP, Constants.DISCOVERY_NAMESERVER_PORT, datagram.serialize());
		udpClient.stop();
	}

	private void bindNameserverStubs (String nsIp)
	{
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		Registry reg = null;

		try
		{
			reg = LocateRegistry.getRegistry(nsIp);
			Node.getInstance().setResolverStub((ResolverInterface) reg.lookup(NameServer.RESOLVER_NAME));
			this.shutdownStub = (ShutdownAgentInterface) reg.lookup((NameServer.SHUTDOWN_AGENT_NAME));
		}
		catch (RemoteException | NotBoundException e)
		{
			e.printStackTrace(); // Failure?
			// todo: Implement some sort of failure mechanism
		}
	}

	/**
	 * Check which neighbour the new incoming neighbour becomes
	 * @param newID The ID of the node that just joined.
	 */
	private void updateNeighbours (short newID)
	{
		/* You're the first node so edit both neighbours of the new node
		 * OWN ID == NEXT ID && OWN ID == PREVIOUS ID
		 */
		if(Node.getInstance().getId() == Node.getInstance().getNextNeighbour() && Node.getInstance().getId() == Node.getInstance().getPreviousNeighbour())
		{
			// This means there's now 2 nodes in the network
			// We set our own neighbours to the other node
			// And use RMI to set the other node's neighbours to us
			Node.getInstance().setPreviousNeighbour(newID);
			Node.getInstance().setNextNeighbour(newID);
			Registry reg = null;

			try
			{
				NodeInteractionInterface neighbourInterface = null;
				reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(newID));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setNextNeighbourRemote(Node.getInstance().getId());
				neighbourInterface.setPreviousNeighbourRemote(Node.getInstance().getId());
			}
			catch (RemoteException | NotBoundException e)
			{
				e.printStackTrace(); //todo: failure
				Node.getInstance().getFailureAgent().failure(newID, newID); // New node has already failed us, what a fucking loser
			}
		}

		// The node will change the previous and next id of the new node in following cases:
		//you are the previous of the new node and need to change the neighbours of the new node and your old next neighbour
		if (
				(		(newID > Node.getInstance().getId() && newID > Node.getInstance().getNextNeighbour() && Node.getInstance().getNextNeighbour() < Node.getInstance().getId()) ||
						(newID > Node.getInstance().getId() && newID < Node.getInstance().getNextNeighbour() && Node.getInstance().getNextNeighbour() > Node.getInstance().getId()) ||
						(newID < Node.getInstance().getId() && newID < Node.getInstance().getNextNeighbour() && Node.getInstance().getNextNeighbour() < Node.getInstance().getId())
				)
			)
		{
			Registry reg = null;
			try
			{
				NodeInteractionInterface neighbourInterface = null;
				reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(newID));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setNextNeighbourRemote(Node.getInstance().getNextNeighbour());
				neighbourInterface.setPreviousNeighbourRemote(Node.getInstance().getId());
			}
			catch (RemoteException | NotBoundException e)
			{
				e.printStackTrace();
				Node.getInstance().getFailureAgent().failure(newID, newID);
			}

			try
			{
				NodeInteractionInterface neighbourInterface = null;
				reg = LocateRegistry.getRegistry(Node.getInstance().getResolverStub().getIP(Node.getInstance().getNextNeighbour()));
				neighbourInterface = (NodeInteractionInterface) reg.lookup(Node.NODE_INTERACTION_NAME);
				neighbourInterface.setPreviousNeighbourRemote(newID);
				Node.getInstance().setNextNeighbour(newID);
			}
			catch (RemoteException | NotBoundException e)
			{
				e.printStackTrace();
				Node.getInstance().getFailureAgent().failure(Node.getInstance().getNextNeighbour(), Node.getInstance().getNextNeighbour());
			}
		}
	}

	public boolean isRunning ()
	{
		return this.running;
	}

	public void stop ()
	{
		this.running = false;
		this.subscriber.stop();
		// shut ourselves down
		this.shutdown();
	}

	/**
	 * Gracefully removes the node from the system.
	 */
	public void shutdown()
	{
		String IPprevious = "";
		String IPnext = "";

		// Get IP's of next and prev. node
		try
		{
			IPprevious = Node.getInstance().getResolverStub().getIP(Node.getInstance().getPreviousNeighbour());
			IPnext = Node.getInstance().getResolverStub().getIP(Node.getInstance().getNextNeighbour());
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}

		// Set Previous node's next neighbour
		try
		{
			Registry registryPrev = LocateRegistry.getRegistry(IPprevious);
			NodeInteractionInterface previousNode = (NodeInteractionInterface) registryPrev.lookup(Node.NODE_INTERACTION_NAME);
			previousNode.setNextNeighbourRemote(Node.getInstance().getNextNeighbour());
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			Node.getInstance().getFailureAgent().failure(Node.getInstance().getPreviousNeighbour(), Node.getInstance().getPreviousNeighbour());
		}

		// Set Next node's previous neighbour
		try
		{
			Registry registryNext = LocateRegistry.getRegistry(IPnext);
			NodeInteractionInterface nextNode = (NodeInteractionInterface) registryNext.lookup(Node.NODE_INTERACTION_NAME);
			nextNode.setPreviousNeighbourRemote(Node.getInstance().getPreviousNeighbour());
		}
		catch(RemoteException | NotBoundException re)
		{
			re.printStackTrace();
			Node.getInstance().getFailureAgent().failure(Node.getInstance().getNextNeighbour(), Node.getInstance().getNextNeighbour());
		}

		// Tell the NameServer we're done
		try
		{
			shutdownStub.requestShutdown(Node.getInstance().getId());
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			Node.getInstance().getFailureAgent().failure(Node.getInstance().getId(), Node.getInstance().getId());
		}
	}
}
