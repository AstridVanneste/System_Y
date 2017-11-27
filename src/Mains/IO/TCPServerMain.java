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

			File file = new File("result.txt");

			System.out.println("Press enter to loop through active connections looking for files");
			scanner.nextLine();

			for(String remoteHost: server.getActiveConnections())
			{
				System.out.println("Got TCP connection on " + remoteHost);
				long received = server.receiveFile(remoteHost, "Vaultboy.png", new File("src/Mains/IO/64x64.png").size());
				System.out.println("Received " + received + "B");
				System.out.println("Should have been " + new File("src/Mains/IO/64x64.png").size() + "B");
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