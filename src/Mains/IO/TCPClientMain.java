package Mains.IO;

import IO.Network.TCP.Client;

import java.util.Scanner;

public class TCPClientMain
{
	private static final String remoteIP = "192.168.0.247";
	private static final int remotePort = 2002;

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		Client client = new Client(remoteIP, remotePort);
		System.out.println("Press enter to Start TCP client");
		scanner.nextLine();
		client.start();

		System.out.println("Started TCP Client on " + remoteIP + ":" + Integer.toString(remotePort));
		System.out.println("Press enter to send file");
		scanner.nextLine();

	  client.sendFile("src/Mains/IO/64x64.png");

		System.out.println("Sent file");

		System.out.println("Press enter to stop client");
		scanner.nextLine();

		client.stop();
	}
}