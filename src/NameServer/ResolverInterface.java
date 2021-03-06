package NameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;

/**
 * Created by Astrid on 22-10-2017.
 */
public interface ResolverInterface extends Remote
{
	/**
	 * Translates the ID of a node to its IP address in form of a String
	 * @param nodeId
	 * @return
	 * @throws RemoteException
	 */
	public String getIP(short nodeId) throws RemoteException;

	/**
	 * For a given filename it returns the IP address from the owner of the file
	 * in the form of a String
	 * @param filename
	 * @return
	 * @throws RemoteException, InvalidParameterException
	 */
	public short getOwnerID(String filename) throws RemoteException, InvalidParameterException;

	/**
	 *returns previous neighbour of node with given ID
	 * @param ID
	 */
	public short getPrevious(short ID) throws RemoteException;

	/**
	 * returns next neighbour of node with given ID
	 * @param ID
	 * @return
	 */
	public short getNext(short ID) throws RemoteException;

	/**
	 * returns the hash of a given String
	 * @param name
	 * @return
	 * @throws RemoteException
	 */
	public short getHash(String name) throws RemoteException;

}
