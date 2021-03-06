package Node;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

public class DownloadPair implements Serializable
{
	private Semaphore lock;
	private short requestID;

	public DownloadPair (int numPermits, short id)
	{
		this.lock = new Semaphore(numPermits, true);
		this.requestID = id;
	}

	public short getID ()
	{
		return this.requestID;
	}

	public synchronized void setID (short newID)
	{
		this.requestID = newID;
	}

	public int availablePermits ()
	{
		return this.lock.availablePermits();
	}

	public synchronized void acquire (int permits) throws InterruptedException
	{
		this.lock.acquire(permits);
	}

	public synchronized void release (int permits)
	{
		this.lock.release(permits);
	}
}
