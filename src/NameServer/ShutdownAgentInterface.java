package NameServer;

/**
 * Created by Axel on 20/10/2017.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ShutdownAgentInterface extends Remote
{

	// The node calls this method to do a request to the NS
	public void requestDeadNode (short id) throws RemoteException;
		// call pingNode
	public void requestShutdown(short id) throws RemoteException;


}
