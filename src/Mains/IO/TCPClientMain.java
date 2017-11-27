package Mains.IO;

import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.TCP.Client;

import java.util.Scanner;

public class TCPClientMain
{
	private static final String localIP = "localhost";
	private static final int localPort = 2002;

	private static final String remoteIP = "192.168.0.244";
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

		ProtocolHeader header = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION,0,1, ProtocolHeader.REQUEST_FILE, ProtocolHeader.REPLY_FILE);
		client.sendFile("/Users/Astrid/Dropbox/A_Universiteit/Semester_5/Gedistribueerde_systemen/Practicum/System_Y/src/Mains/IO/bitjes_be_crazy.gif", header);
		//client.sendFile("src/Mains/IO/64x64.png", header);

		System.out.println("Sent file");

		System.out.println("Press enter to stop client");
		scanner.nextLine();

		client.stop();

	}
}