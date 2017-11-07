package Node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInteractionInterface extends Remote
{
	public void setNextNeighbour (int id) throws RemoteException;

	public int getNextNeighbour () throws RemoteException;

	public void setPreviousNeighbour (int id) throws RemoteException;

	public int getPreviousNeighbour () throws RemoteException;
}
