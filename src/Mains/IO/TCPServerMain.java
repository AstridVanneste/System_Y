package Mains.IO;

import IO.File;
import IO.Network.TCP.Server;

import java.io.IOException;
import java.util.Scanner;

public class TCPServerMain
{
	private static final int localPort = 2002;

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);
		Server server = new Server(localPort);
		System.out.println("Created server on port " + Integer.toString(localPort));

		try
		{
			System.out.println("Press enter to start Server");
			scanner.nextLine();
			server.start();


			System.out.println("Press enter to loop through active connections looking for files");
			scanner.nextLine();

			for(String remoteHost: server.getActiveConnections())
			{
				System.out.println("Got TCP connection on " + remoteHost);
				server.receiveFile(remoteHost, "Result.gif");
				System.out.println("File received");
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