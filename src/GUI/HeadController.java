package GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Created by Axel on 19/12/2017.
 */
public class HeadController
{
	private FXMLLoader mainView = new FXMLLoader();
	private FXMLLoader loginView = new FXMLLoader();

	private MainController mainController;
	private LoginController loginController;

	private Parent rootMain;
	private Parent rootLogin;

	public HeadController() throws IOException
	{
		loginView.setLocation(getClass().getResource("LoginWindow.fxml"));
		mainView.setLocation(getClass().getResource("MainWindow.fxml"));

		rootMain = mainView.load();
		rootLogin = loginView.load();

		mainController = mainView.getController();
		loginController = loginView.getController();

		loginController.init(this);
	}

	public void login(String[] args)
	{
		loginController.view(rootLogin, args);
	}

	public void toMainWindow()
	{
		mainController.init();
		mainController.view(rootMain);
	}

}
