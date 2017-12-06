package Node;

import java.io.Serializable;

public abstract class Agent implements Runnable, Serializable
{
	public abstract boolean isFinished();
}
