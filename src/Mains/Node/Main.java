package Mains.Node;

import Node.Node;
import Node.DownloadManager;

import java.net.URL;
import java.security.Policy;
import java.util.LinkedList;
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
		    System.err.println("[ERROR]\tInvalid number of command line arguments: " + Integer.toString(args.length) + ", should be at least 1 and at most 2");
		    System.err.println("USAGE: java -jar Node.jar NODENAME [SYSTEM Y ROOT DIRECTORY]");
		    System.exit(-1);
	    }
	    else
	    {
		    String serverPolicyPath = "/Policies/Server.policy";
			URL serverPolicyURL = Mains.NameServer.Main.class.getResource(serverPolicyPath);

			if (serverPolicyURL == null)
			{
				System.err.println("getResource returned NULL");
			}

			System.setProperty("java.security.policy",serverPolicyURL.toString());
			Policy.getPolicy().refresh();

		    Scanner scanner = new Scanner(System.in);
		    printLineSep();

		    System.out.println("INSTRUCTIONS:\n1.) DO TESTING\n2.) STOP NODE (Can be done via quit, which causes an ungraceful end, or via shutdown, which is a graceful stop)");
		    System.out.println("Press return to continue...");
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
		    System.out.println("Starting Node...");

		    boolean quit = false;

		    while (!quit)
		    {
			    System.out.println("[Q]uit | Show [N]eighbours | Shu[T]down | St[A]rt | Fail[u]re | Print [I]D | List Files [o]n this Node | List All [F]iles | Manually [D]ownload File | Do[w]nload file through FileAgent | D[e]lete file in network");
			    System.out.print(">");
			    String next = scanner.nextLine();

			    switch (next)
			    {
				    case "Q":
				    case "q":
					    System.out.println("Quitting...");
					    quit = true;
					    continue;
				    case "N":
				    case "n":
					    System.out.println("Checking Next and Previous Nodes...");
					    System.out.println("Next: " + Integer.toString(Node.getInstance().getNextNeighbour()));
					    System.out.println("Previous: " + Integer.toString(Node.getInstance().getPreviousNeighbour()));
					    break;
				    case "T":
				    case "t":
					    System.out.println("Stopping Node...");
					    Node.getInstance().stop();
					    break;
				    case "A":
				    case "a":
					    System.out.println("Starting Node...");
					    Node.getInstance().start();
					    break;
				    case "U":
				    case "u":
					    System.out.println("Please enter the ID of the node you wish to indicate for failure: ");
					    short failID = scanner.nextShort();
					    System.out.println("Starting failure for node " + Short.toString(failID));
					    Node.getInstance().getFailureAgent().failure(failID);
					    break;
				    case "I":
				    case "i":
					    System.out.println("ID: " + Short.toString(Node.getInstance().getId()));
					    break;
				    case "O":
				    case "o":
					    System.out.println(Node.getInstance().getFileManager().toString());
					    break;
				    case "D":
				    case "d":
				    {
					    System.out.println("Type the filename of the file you want to download...");
					    String filename = scanner.nextLine();
					    Node.getInstance().getFileManager().requestFile(filename);
					    break;
				    }
				    case "W":
				    case "w":
				    {
					    System.out.println("Type the filename of the file you want to download...");
					    String filename = scanner.nextLine();
					    Node.getInstance().getAgentHandler().downloadFile(filename);
					    break;
				    }
				    case "F":
				    case "f":
				    	System.out.println("Listing all files in the system...");
				    	final LinkedList<String> files = Node.getInstance().getAgentHandler().getAllFiles();    // ConcurrentModificationException on a list that doesn't need to be modified => Mark final
				    	for (String file : files)
					    {
					    	System.out.println("File: " + file);
					    }
					    break;
				    case "E":
				    case "e":
				    	{
				    		System.out.println("Pleas enter the name of the file you wish to delete");
				    		String filename = scanner.nextLine();
					        Node.getInstance().getAgentHandler().deleteFile(filename);
				        }
				    	break;
				    default:
					    System.err.println("[ERROR]\tInvalid input: '" + next + "'");
					    continue;
			    }

			    printLineSep();
		    }

		    System.exit(-1);
	    }
    }
}