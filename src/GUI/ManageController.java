package GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class ManageController
{
	private FXMLLoader mainView = new FXMLLoader();
	private FXMLLoader loginView = new FXMLLoader();

	private MainController mainController;
	private LoginController loginController;

	private Parent rootMain;
	private Parent rootLogin;

	public static ManageController instance;

	public ManageController() throws IOException
	{
		loginView.setLocation(getClass().getResource("LoginWindow.fxml"));
		mainView.setLocation(getClass().getResource("MainWindow.fxml"));

		rootMain = mainView.load();
		rootLogin = loginView.load();

		mainController = mainView.getController();
		loginController = loginView.getController();

		loginController.init(this);
	}

	public static ManageController getInstance()
	{
		if(ManageController.instance == null)
		{
			try
			{
				ManageController.instance = new ManageController();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return ManageController.instance;
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

	public MainController getMainController()
	{
		return mainController;
	}
}
