package Node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInteractionInterface extends Remote
{
	public void setNextNeighbourRemote (short id) throws RemoteException;

	public short getNextNeighbourRemote () throws RemoteException;

	public void setPreviousNeighbourRemote (short id) throws RemoteException;

	public short getPreviousNeighbourRemote () throws RemoteException;

	public boolean isRunning () throws RemoteException;

	public void indicateNeighboursSet () throws RemoteException;
}
