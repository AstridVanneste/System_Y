package Node;

import java.util.Scanner;

public class ShutdownTest1
{
	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		Node.getInstance();

		//Node.getInstance().nameServerBind("10.0.0.2");
		//Node.getInstance().setId((short) 1);
		//Node.getInstance().setNextNeighbour((short) 200);
		//Node.getInstance().setPreviousNeighbour((short) 200);

		System.out.println("Press enter to shutdown the node");
		scanner.nextLine();

		Node.getInstance().getLifeCycleManager().shutdown();

	}
}
