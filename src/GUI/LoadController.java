package GUI;

import Node.Node;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class LoadController implements Runnable
{
	private Scene view;
	private ManageController manageController;
	private Stage stage;

	@FXML
	public Text textField;

	public void init(ManageController controller)
	{
		this.manageController = controller;
	}



	public void view (Parent root)
	{
		if(view == null)
			view = new Scene(root,600,90);

		stage = new Stage();

		stage.setResizable(false);
		stage.setScene(view);
		//stage.initStyle(StageStyle.UNDECORATED);
		stage.show();

		Thread t = new Thread(this);
		t.start();

	}

	public void close ()
	{
		stage.close();
		manageController.toMainWindow();
	}

	@Override
	public void run()
	{
		System.out.println("thread started");
		boolean isClosed = true;
		while (isClosed)
		{
			if (ManageController.getInstance().getCloseLoad())
			{
				javafx.application.Platform.runLater(() -> close());

				isClosed = false;
			}

		}
	}
}
