package Mains.Node;

import GUI.ManageController;
import GUI.LoginController;
import GUI.MainController;
import Node.Node;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.Policy;
import java.util.Scanner;

import static Util.General.printLineSep;

public class GUIMain extends Application
{
	private static String[] arg;

	public static void main(String[] args)
	{
		arg = args;
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		String serverPolicyPath = "/Policies/Server.policy";
		URL serverPolicyURL = Mains.NameServer.Main.class.getResource(serverPolicyPath);

		if (serverPolicyURL == null)
		{
			System.err.println("getResource returned NULL");
		}

		System.setProperty("java.security.policy",serverPolicyURL.toString());
		Policy.getPolicy().refresh();

		ManageController.getInstance().login(arg);
	}
}
