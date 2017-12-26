package GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class ManageController
{
	private FXMLLoader mainView = new FXMLLoader();
	private FXMLLoader loginView = new FXMLLoader();
	private FXMLLoader loadView = new FXMLLoader();
	private FXMLLoader popUpView = new FXMLLoader();

	private MainController mainController;
	private LoginController loginController;
	private LoadController loadController;
	private PopUpController popUpController;

	private Parent rootMain;
	private Parent rootLogin;
	private Parent rootLoad;
	private Parent rootPopUp;

	private Boolean closeLoad;

	public static ManageController instance;

	public ManageController() throws IOException
	{
		// FXML-files are placed into the loaders
		loginView.setLocation(getClass().getResource("LoginWindow.fxml"));
		mainView.setLocation(getClass().getResource("MainWindow.fxml"));
		loadView.setLocation(getClass().getResource("LoadingWindow.fxml"));
		popUpView.setLocation(getClass().getResource("PopUpWindow.fxml"));

		// Execute the fxml-'objects'. Controllers are made and @FXML initialize's are executed
		rootMain = mainView.load();
		rootLogin = loginView.load();
		rootLoad = loadView.load();
		rootPopUp = popUpView.load();

		// Get the controller-objects to perform actions on it
		mainController = mainView.getController();
		loginController = loginView.getController();
		loadController = loadView.getController();
		popUpController = popUpView.getController();

		loginController.init(this);
		loadController.init(this);
		mainController.init();
		popUpController.init(this);

		closeLoad = false;
	}

	public static ManageController getInstance()
	{
		if(ManageController.instance == null)
		{
			try
			{
				ManageController.instance = new ManageController();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return ManageController.instance;
	}

	// First action performed after launching the GUI, view the LoginScreen
	public void login(String[] args)
	{
		loginController.view(rootLogin, args);
	}

	// Switch to MainWindow
	public void toMainWindow()
	{
		mainController.view(rootMain);
	}

	// Show loadingwindow. Called after logged in
	public void toLoadWindow ()
	{
		loadController.view(rootLoad);
	}

	// Close loadingwindow. Called by the model when a second node has joined the network
	public void closeLoadWindow()
	{
		loadController.close();
		toMainWindow();
	}

	public void toPopUp (String file)
	{
		popUpController.view(rootPopUp, file );
	}


	public MainController getMainController()
	{
		return mainController;
	}

	public synchronized Boolean getCloseLoad()
	{
		return closeLoad;
	}

	public synchronized void setCloseLoad(Boolean closeLoad)
	{
		this.closeLoad = closeLoad;
	}
}
