package Mains.IO;

import IO.File;
import IO.Network.TCP.Server;

import java.io.IOException;
import java.util.Scanner;

public class TCPServerMain
{
	private static final int localPort = 2200;

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);
		Server server = new Server(localPort);

		try
		{
			System.out.println("Press enter to start Server");
			scanner.nextLine();
			server.start();


			System.out.println("Press enter to loop through active connections looking for files");
			scanner.nextLine();

			for(String remoteHost: server.getActiveConnections())
			{
				server.receiveFile(remoteHost, "result.gif");
			}

			System.out.println("Press enter to stop server");
			scanner.nextLine();

			server.stop();
		}
		catch(IOException ioe)
		{
			System.out.println("IOException when working with TCP Server");
			ioe.printStackTrace();
		}
	}
}