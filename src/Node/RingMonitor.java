package Node;

/**
 * The ring monitor is a system that will be running on only one of the nodes. It will check if a file agent is in the system.
 * It will do this by starting a timeout procedure every time the file agent passes.
 * If the timeout passes the ring monitor introduces a new file agent in the system.
 */
public class RingMonitor implements Runnable
{
	private static final long TIMEOUT = 10000; //time in ms
	private long startTime;
	private Thread thread;
	private boolean running;

	private static RingMonitor instance;

	private RingMonitor()
	{
		this.startTime = System.nanoTime();
		this.thread = new Thread();
		this.thread.setName("Ring Monitor Thread, Node: " + Node.getInstance().getId());
		this.running = false;
	}

	public static RingMonitor getInstance()
	{
		if(RingMonitor.instance == null)
		{
			RingMonitor.instance = new RingMonitor();
		}
		return RingMonitor.instance;
	}

	/**
	 * Starts the ringmonitor.
	 */
	public void start()
	{
		this.thread.start();
		this.running = true;
	}

	/**
	 * Stops the ring monitor
	 */
	public void stop()
	{
		this.running = false;
		try
		{
			this.thread.join();
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	@Override
	/**
	 * The thread will check if the timeout has passed. If this is the case a new file agent will be introduced in the system.
	 */
	public void run()
	{
		while(this.running)
		{
			if (((System.nanoTime() - this.startTime) / 1000000) > TIMEOUT)
			{
				Node.getInstance().getAgentHandler().runAgent(new FileAgent());
				this.startTime = System.nanoTime();
				System.out.printf("Started new file agent because of timeout");
			}
		}
	}

	/**
	 * Checks if the ring monitor is running.
	 * @return
	 */
	public synchronized boolean isRunning()
	{
		return this.running;
	}

	/**
	 * Called when a file agent passes on the node. The timeout procedure will be started again from scratch.
	 */
	public void fileAgentPassed()
	{
		this.startTime = System.nanoTime();
	}
}
