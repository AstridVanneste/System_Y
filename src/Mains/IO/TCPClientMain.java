package Mains.IO;

import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.TCP.Client;

import java.util.Scanner;

public class TCPClientMain
{
	private static final String localIP = "192.168.0.247";
	private static final int localPort = 2002;

	private static final String remoteIP = "192.168.0.247";
	private static final int remotePort = 2001;

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		Client client = new Client(localIP, localPort);
		System.out.println("Press enter to Start TCP client");
		scanner.nextLine();
		client.start();

		System.out.println("Started TCP Client on " + localIP + ":" + Integer.toString(localPort));
		System.out.println("Press enter to send file");
		scanner.nextLine();

	  client.sendFile("src/Mains/IO/64x64.png", header);

		System.out.println("Sent file");

		System.out.println("Press enter to stop client");
		scanner.nextLine();

		client.stop();
	}
}