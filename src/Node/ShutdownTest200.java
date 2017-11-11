package Node;

import java.util.Scanner;

public class ShutdownTest200
{
	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		//Node.getInstance().nameServerBind("10.0.0.2");
		//Node.getInstance().setId((short) 200);
		//Node.getInstance().setNextNeighbour((short) 1);
		//Node.getInstance().setPreviousNeighbour((short) 1);

		System.out.println("Press enter to print neighbours");
		scanner.nextLine();

		//System.out.println("NEXT: " + Node.getInstance().getNextNeighbour() + " PREVIOUS: " + Node.getInstance().getPreviousNeighbour());
	}
}
