package Mains.IO;

import IO.File;
import IO.Network.TCP.Server;

import java.io.IOException;
import java.util.Scanner;

public class TCPServerMain
{
	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);
		Server server = new Server(2002);

		try
		{
			server.start();

			System.out.println("press enter to receive file");
			scanner.nextLine();

			File file = new File("result.txt");

			for(String remoteHost: server.getActiveConnections())
			{
				System.out.println("RECEIVING FILE ON " + remoteHost);
				long received = server.receiveFile(remoteHost, "result.gif", new File("/Users/Astrid/Dropbox/A_Universiteit/Semester_5/Gedistribueerde_systemen/Practicum/System_Y/src/IO/Network/TCP/bitjes_be_crazy.gif").size());
				System.out.println("Received " + received + " bytes");
				System.out.println("Should have been " + new File("/Users/Astrid/Dropbox/A_Universiteit/Semester_5/Gedistribueerde_systemen/Practicum/System_Y/src/IO/Network/TCP/bitjes_be_crazy.gif").size() + " bytes");
			}

			System.out.println("press enter to stop server");
			scanner.nextLine();

			server.stop();

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}