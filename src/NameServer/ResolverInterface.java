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

	public String lookup(short nodeId) throws RemoteException;

	public String getIP(int nodeId) throws RemoteException;
	
	/**
	 * Removes entry from TreeMap
	 * @param nodeId
	 * @return
	 * @throws RemoteException
	 */
	public void removeFromTree(int nodeId) throws RemoteException;

	/**
	 * For a given filename it returns the IP address from the owner of the file
	 * in the form of a String
	 * @param filename
	 * @return
	 * @throws RemoteException, InvalidParameterException
	 */
	public String getOwnerIP(String filename) throws RemoteException, InvalidParameterException;

	public void addToTree(short ID, String IP) throws RemoteException;   // todo: Remove when RMI testing is complete
}
