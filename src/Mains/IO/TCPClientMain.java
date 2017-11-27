package Mains.IO;

import IO.Network.Datagrams.ProtocolHeader;
import IO.Network.TCP.Client;

import java.util.Scanner;

public class TCPClientMain
{
	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		Client client = new Client("localhost", 2002);
		client.start();

		System.out.println("Press enter to send file");
		scanner.nextLine();

		ProtocolHeader header = new ProtocolHeader(ProtocolHeader.CURRENT_VERSION,0,1, ProtocolHeader.REQUEST_FILE, ProtocolHeader.REPLY_FILE);
		client.sendFile("/Users/Astrid/Dropbox/A_Universiteit/Semester_5/Gedistribueerde_systemen/Practicum/System_Y/src/IO/Network/TCP/bitjes_be_crazy.gif", header);

		System.out.println("Press enter to stop client");
		scanner.nextLine();

		client.stop();
	}
}