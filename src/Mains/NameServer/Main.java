package Mains.NameServer;


import IO.File;
import NameServer.NameServer;

import java.net.URL;
import java.security.Policy;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
    	boolean quit = false;

    	String serverPolicyPath = "/Policies/Server.policy";
	    URL serverPolicyURL = Main.class.getResource(serverPolicyPath);

	    if (serverPolicyURL == null)
	    {
		    System.err.println("getResource returned NULL");
	    }

	    System.setProperty("java.security.policy",serverPolicyURL.toString());
	    Policy.getPolicy().refresh();

    	Util.General.printLineSep();
		System.out.println("Starting NameServer...");
		NameServer.getInstance().init();
		System.out.println("NameServer started");
		Util.General.printLineSep();

		Util.General.printLineSep();
		System.out.println("Possible commands: \nShow Tree[M]ap | [Q]uit");

		while(!quit)
		{
			System.out.print(">");
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();

			if(input.equals("Q") || input.equals("q"))
			{
				quit = true;
				System.out.println("Quitting...");
				NameServer.getInstance().stop();

			}
			else if(input.equals("M") || input.equals("m"))
			{
				System.out.println(NameServer.getInstance().toString());
			}
		}

	}
}
