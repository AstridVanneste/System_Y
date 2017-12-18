package GUI;

import Node.Node;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainController
{
	private ObservableList<TableFile> data;		// data as an observable list of TableFiles
	private String fileSelected;

	private static MainController instance;

	@FXML
	private javafx.scene.image.ImageView shutdownButton;
	@FXML
	private javafx.scene.control.ScrollPane scrollPane;
	@FXML
	private javafx.scene.control.TableView<TableFile> tableView;
	@FXML
	private TableColumn<TableFile, String> fileNameColumn;
	@FXML
	private TableColumn<TableFile, String> sizeColumn;
	@FXML
	private Label previousLabel;
	@FXML
	private Label nextLabel;

	public MainController()
	{
		this.data = FXCollections.observableArrayList();
	}

	public static MainController getInstance()
	{
		if(MainController.instance == null)
		{
			MainController.instance = new MainController();
		}
		return MainController.instance;
	}

	/**
	 * When calling the correspondent fxml-file, this method will also be called
	 */
	@FXML
	public void initialize()
	{
		this.fileNameColumn.setCellValueFactory(new PropertyValueFactory("fileName"));
		this.sizeColumn.setCellValueFactory(new PropertyValueFactory("size"));

		//this.tableView.getColumns().setAll(this.fileNameColumn,this.sizeColumn);
		setExampleFiles();
	}

	public void notifyChanges()
	{
		this.data.clear();
		//this.tableView.getItems().removeAll();

		for(String s : Test.getAllFiles())//Node.getInstance().getAgentHandler().getAllFiles()
		{
			System.out.println("New file: " + s);
			this.data.add(new TableFile(s,"Not supported yet"));
		}
		this.tableView.setItems(this.data);
	}

	private void addFile(String name)
	{
		this.data.add(new TableFile(name, "Not supported yet"));
		this.tableView.setItems(this.data);
	}

	public void UpdateNeighbours (String prevID, String nextID)
	{
		this.previousLabel.setText(prevID);
		this.nextLabel.setId(nextID);
	}

	/**
	 * When clicked on a fileEntry, a PopUpWindow will open to do something with the file
	 * @throws IOException
	 */
	public void openPopUpWindow () throws IOException
	{
		String file = tableView.getSelectionModel().getSelectedItem().getFileName();
		this.fileSelected = file;

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
		//controller.setFiles(this.tableView.getItems());

	}
	public void shutdown()
	{
		shutdownButton.setImage(new Image("@exit_image.jpg"));
		Node.getInstance().stop();
	}

	public ObservableList<TableFile> getData()
	{
		return this.data;
	}

	private void setExampleFiles ()
	{

		addFile("File2");
		addFile("File3");


	}
}
