package Node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInteractionInterface extends Remote
{
	public void setNextNeighbour (short id) throws RemoteException;

	public short getNextNeighbour () throws RemoteException;

	public void setPreviousNeighbour (short id) throws RemoteException;

	public short getPreviousNeighbour () throws RemoteException;
}
