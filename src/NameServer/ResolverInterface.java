package NameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Astrid on 22-10-2017.
 */
public interface ResolverInterface extends Remote
{
	public String lookup(int nodeId) throws RemoteException;

	public String lookup(String filename) throws RemoteException;
}
