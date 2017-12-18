package GUI;

import Node.Node;
import java.io.IOException;
import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainController
{
	private ObservableList<TableFile> data = FXCollections.observableArrayList();		// data as an observable list of TableFiles
	private String fileSelected;

	private static MainController instance;

	@FXML
	private javafx.scene.image.ImageView shutdownButton;
	@FXML
	private javafx.scene.control.ScrollPane scrollPane;
	@FXML
	private javafx.scene.control.TableView<TableFile> tableView;
	@FXML
	private TableColumn<TableFile, String> fileName;
	@FXML
	private TableColumn<TableFile, String> size;
	@FXML
	private Label previousLabel;
	@FXML
	private Label nextLabel;

	public MainController()
	{

	}

	public static MainController getInstance()
	{
		if(MainController.instance == null)
		{
			MainController.instance = new MainController();
		}
		return MainController.instance;
	}

	private void setExampleFiles ()
	{
/*
		addFile(new TableFile("File2", "Not supported yet"));
		addFile(new TableFile("File3", "Not supported yet"));
		addFile(new TableFile("File4", "Not supported yet"));
		addFile(new TableFile("File5", "Not supported yet"));
*/

	}

	/**
	 * When calling the correspondent fxml-file, this method will also be called
	 */
	@FXML
	public void initialize()
	{
		this.fileName.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
		this.size.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
		//setExampleFiles();
	}

	public void notifyChanges()
	{
		this.data.removeAll();
		for(String s : Node.getInstance().getAgentHandler().getAllFiles())
		{
			TableFile tableFile = new TableFile(s,"Not supported yet");
			data.add(tableFile);
		}
	}

	/**
	 *  This function is called to create a new fileEntry in the TableView
	 */
	public void addFile(String fileName)
	{

		this.data.add(new TableFile(fileName, "Not supported yet"));
		this.tableView.setItems(this.data);
	}

	public void deleteFile(String fileName)
	{
		this.data.remove(new TableFile(fileName, "Not supported yet"));
		this.tableView.setItems(this.data);
	}

	public void UpdateNeighbours (String prevID, String nextID)
	{
		this.previousLabel.setText(prevID);
		this.nextLabel.setId(nextID);
	}

	/**
	 *  Compares the list of the files in the network vs the files in the tableView
	 *  Adds a file in the tableView if necessary.
	 */
	public void updateFiles(LinkedList<String> list)
	{
		LinkedList<String> files = list;

		for(String file : files)
		{
			TableFile newFile = new TableFile(file, "Not supported yet");

			if (!data.contains(newFile))
			{
				//addFile(newFile);
			}
		}
	}


	/**
	 * When clicked on a fileEntry, a PopUpWindow will open to do something with the file
	 * @throws IOException
	 */
	public void openPopUpWindow () throws IOException
	{
		String file = tableView.getSelectionModel().getSelectedItem().getFileName();
		setFileSelected(file);

		FXMLLoader fxmlLoader = new FXMLLoader();
		Parent root = fxmlLoader.load(getClass().getResource("PopUpWindow.fxml").openStream());
		//PopUpController controller = fxmlLoader.getController();

		Stage secondaryStage = new Stage();
		secondaryStage.initStyle(StageStyle.UTILITY);
		secondaryStage.setMinWidth(160);
		secondaryStage.setMinHeight(120);
		secondaryStage.setScene(new Scene(root, 160, 120));
		secondaryStage.show();

		//controller.setSelectedFile(this.fileSelected);
		//controller.setFiles(this.data);

	}
	public void shutdown()
	{
		shutdownButton.setImage(new Image("@exit_image.jpg"));
		Node.getInstance().stop();
	}

	public void setFileSelected (String fileName)
	{
		this.fileSelected = fileName;
	}

	public String getFileSelected()
	{
		return this.fileSelected;
	}

	public ObservableList<TableFile> getData()
	{
		return this.data;
	}

}
