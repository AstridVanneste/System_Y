package GUI;

import Node.Node;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LoginController
{
	private Scene view;
	private Boolean validEntered =true;

	@FXML
	private TextField nodeName;
	@FXML
	private TextField rootDir;

	private ManageController manageController;

	public void init(ManageController controller)
	{
		this.manageController = controller;
	}

	public void login()
	{
		validEntered = true;
		String name = nodeName.getText();
		if(name.equals(""))
			validEntered = false;

		String directory = rootDir.getText();
		// check if valid rootdir

		if(validEntered) {
			Stage currentWindow = (Stage) nodeName.getScene().getWindow();
			currentWindow.close();

			System.out.println("logged in");
			System.out.println("name " + name);
			Node.getInstance().setName(name);
			Node.getInstance().getFileManager().setRootDirectory(directory);

			ManageController.getInstance().toLoadWindow();

			Node.getInstance().start();

		}
	}

	public void view (Parent root, String[] args)
	{
		if(view == null)
			view = new Scene(root,300,150);
		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent arg0) {
				System.exit(0);
			}
		});

		stage.setScene(view);
		stage.show();

		if (args.length != 0)
		{
			this.nodeName.setText(args[0]);
			this.rootDir.setText(args[1]);
		}
	}

}
