package Node;


/**
 * Created by Astrid on 07/11/2017.
 */
public class FailureAgent
{
	public FailureAgent()
	{

	}

	public void failure(short ID)
	{
		Node.getInstance().getResolverStub().getPrevious(ID);
	}
}
