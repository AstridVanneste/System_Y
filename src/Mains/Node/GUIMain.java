package Mains.Node;

import GUI.HeadController;
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

/**
 * Created by Axel on 19/12/2017.
 */
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
		HeadController controller = new HeadController();
		controller.login(arg);
	}
}
