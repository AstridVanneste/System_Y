package GUI;

import Node.Node;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class MainController
{
	private String fileSelected;
	private Scene view;
	private List<String> allFiles;
	private Boolean popUpOpen;

	private PopUpController controller;

	@FXML
	private javafx.scene.image.ImageView shutdownButton;
	@FXML
	private javafx.scene.control.ScrollPane scrollPane;
	@FXML
	private javafx.scene.control.TableView<TableFile> tableView;
	@FXML
	private TableColumn<TableFile, String> fileNameColumn;
	//@FXML
	//private TableColumn<TableFile, String> sizeColumn;
	@FXML
	private TextField pathField;

	public MainController()
	{
	}

	/**
	 * When calling the correspondent fxml-file, this method will also be called
	 */
	@FXML
	public void initialize()
	{
		this.fileNameColumn.setCellValueFactory(new PropertyValueFactory("fileName"));
	}

	public void init ()
	{
		popUpOpen = false;
	}

	/*public void start()
	{
		Thread t = new Thread(this);
		t.start();
	}

	public void run()
	{
		while(isRunnig)
		{
			if(String.valueOf(Node.getInstance().getNextNeighbour()) != nextLabel.getText())
			{
				nextLabel.setText(String.valueOf(Node.getInstance().getNextNeighbour()));
			}
			if(String.valueOf(Node.getInstance().getPreviousNeighbour()) != previousLabel.getText())
			{
				previousLabel.setText(String.valueOf(Node.getInstance().getPreviousNeighbour()));
			}
		}
	}*/

	public void view(Parent root){
		if(view == null)
			view = new Scene(root,600,400);
		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent arg0) {
				System.exit(-1);
			}
		});
		stage.setScene(view);
		stage.show();
	}

	public void updateFiles(LinkedList<String> filesList)
	{
		ObservableList<TableFile> obsList = tableView.getItems();

		int i = 0;
		boolean identical = true;
		if(filesList.size() == obsList.size())
		{
			for(String s : filesList)
			{
				if(!s.equals(obsList.get(i).getFileName()))
				{
					System.out.println("not identical");
					identical = false;
				}
				i++;
			}

			if(!identical)
			{
				obsList.clear();
				System.out.println("cleared");
				for(String s: filesList)
				{
					obsList.add(new TableFile(s));
				}
				tableView.setItems(obsList);
				identical = true;
			}
		}
		if(filesList.size() != 0 && filesList.size() != obsList.size())
		{
			obsList.clear();
			for(String s: filesList)
			{
				obsList.add(new TableFile(s));
				System.out.println(s);
			}
			tableView.setItems(obsList);
		}
	}

	/**
	 * When clicked on a fileEntry, a PopUpWindow will open to do something with the file
	 * @throws IOException
	 */
	public void openPopUpWindow () throws IOException
	{
		if (!popUpOpen)
		{
			popUpOpen = true;

			TableFile file = tableView.getSelectionModel().getSelectedItem();

			if (file != null)
			{
				String fileName = file.getFileName();

				this.fileSelected = fileName;
				ManageController.getInstance().toPopUp(fileName);
			}
		}
	}

	public void shutdown()
	{
		//shutdownButton.setImage(new Image("@exit_image.jpg"));
		Node.getInstance().stop();
		System.exit(0);
	}

	public void addFile()
	{
		String filePath = pathField.getText();
		if (!filePath.equals(""))
		{

		}
	}

	public void setPopUpOpen (Boolean value)
	{
		this.popUpOpen = value;
	}

}
