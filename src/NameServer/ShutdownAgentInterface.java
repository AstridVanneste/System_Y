package NameServer;

/**
 * Created by Axel on 20/10/2017.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ShutdownAgentInterface extends Remote
{

	public void requestShutdown(short id) throws RemoteException;


}
