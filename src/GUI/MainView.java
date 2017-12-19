package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainView extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader();
			Parent root = fxmlLoader.load(getClass().getResource("MainWindow.fxml"));

			primaryStage.setMinWidth(600);
			primaryStage.setMinHeight(400);
			primaryStage.setScene(new Scene(root, 600, 400));
			primaryStage.show();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
