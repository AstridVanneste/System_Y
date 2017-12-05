package Mains.Node;

import Node.Node;

import java.util.Scanner;

import static Util.General.printLineSep;

/**
 * Created by Axel on 25/10/2017.
 */
public class Main
{
    public static void main(String[] args)
    {
	    if ((args.length < 1) ||  (args.length > 2))
	    {
		    System.err.println("[ERROR]\tInvalid number of command line arguments: " + Integer.toString(args.length) + ", should be 1");
		    return;
	    }
	    else
	    {
		    Scanner scanner = new Scanner(System.in);
		    printLineSep();

		    System.out.println("INSTRUCTIONS:\n1.) DO TESTING\n2.) STOP NODE (Can be done via quit, which causes an ungraceful end, or via shutdown, which is a graceful stop)");
		    System.out.println("Press any key to continue...");
		    scanner.nextLine();

		    printLineSep();

		    Node.getInstance().setName(args[0]);
		    System.out.println("Set name '" + args[0] + "'");
			if(args.length == 2)
			{
				System.out.println("Set root directory " + args[1]);
				Node.getInstance().getFileManager().setRootDirectory(args[1]);
			}
			Node.getInstance().start();
		    System.out.println("Started Node...");

			
		    boolean quit = false;

		    while (!quit)
		    {
			    System.out.println("[Q]uit | Show [N]eighbours | Shu[T]down | St[A]rt | [F]ailure | Print [I]D | List Files [o]n this Node | List All [F]iles | [D]ownload File");
			    System.out.print(">");
			    String next = scanner.nextLine();

			    if (next.equals("Q") || next.equals("q"))
			    {
				    System.out.println("Quitting...");
				    quit = true;
				    continue;
			    }
			    else if (next.equals("N") || next.equals("n"))
			    {
				    System.out.println("Checking Next and Previous Nodes...");
				    System.out.println("Next: " + Integer.toString(Node.getInstance().getNextNeighbour()));
				    System.out.println("Previous: " + Integer.toString(Node.getInstance().getPreviousNeighbour()));
			    }
			    else if (next.equals("T") || next.equals("t"))
			    {
				    System.out.println("Stopping Node...");
				    Node.getInstance().stop();
			    }
			    else if (next.equals("A") || next.equals("a"))
			    {
				    System.out.println("Starting Node...");
				    Node.getInstance().start();
			    }
			    else if (next.equals("F") || next.equals("f"))
			    {
				    System.out.println("Please enter the ID of the node you wish to indicate for failure: ");
				    short failID = scanner.nextShort();
				    System.out.println("Starting failure for node " + Short.toString(failID));
				    Node.getInstance().getFailureAgent().failure(failID);
			    }
			    else if (next.equals("I") || next.equals("i"))
			    {
				    System.out.println("ID: " + Short.toString(Node.getInstance().getId()));
			    }
			    else if (next.equals("O") || next.equals("o"))
				{
					System.out.println(Node.getInstance().getFileManager().toString());
				}
			    else
			    {
				    System.err.println("[ERROR]\tInvalid input: '" + next + "'");
				    continue;
			    }

			    printLineSep();
		    }

		    System.exit(-1);
	    }
    }
}