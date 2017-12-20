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
		ManageController controller = new ManageController();
		controller.login(arg);
	}
}
