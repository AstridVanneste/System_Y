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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.swing.text.TableView;

public class MainController
{
	private String fileSelected;
	private Scene view;
	private List<String> allFiles;
	private Boolean popUpOpen;

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
	private Label previousLabel;
	@FXML
	private Label nextLabel;
	@FXML
	private Label nodeNameLabel;
	@FXML
	private Label nodeIDLabel;

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

		System.out.println(Node.getInstance().getName());
		this.nodeNameLabel.setText(Node.getInstance().getName());
		System.out.println(String.valueOf(Node.getInstance().getId()));
	}

	public void updateFiles(LinkedList<String> filesList)
	{
		boolean obsListChanged = false;
		ObservableList<TableFile> obsList = tableView.getItems();
		for (String file : filesList)
		{
			if (!obsList.contains(file))
			{
				obsList.add(new TableFile(file));
				obsListChanged = true;
			}
		}

		Iterator<TableFile> allIt = obsList.iterator();

		while (allIt.hasNext())
		{
			final String file  = allIt.next().getFileName();

			if (!filesList.contains(file))
			{
				obsList.remove(file);
				obsListChanged = true;
			}
		}

		if(obsListChanged)
		{
			tableView.setItems(obsList);
		}

	}

	public void updateNeighbours ()
	{
		this.nextLabel.setText(String.valueOf(Node.getInstance().getNextNeighbour()));
		this.previousLabel.setText(String.valueOf(Node.getInstance().getPreviousNeighbour()));
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
			String file = tableView.getSelectionModel().getSelectedItem().getFileName();
			this.fileSelected = file;

			FXMLLoader fxmlLoader = new FXMLLoader();
			Parent root = fxmlLoader.load(getClass().getResource("PopUpWindow.fxml").openStream());
			PopUpController controller = fxmlLoader.getController();
			controller.init(this);

			Stage secondaryStage = new Stage();
			secondaryStage.initStyle(StageStyle.UTILITY);
			secondaryStage.setMinWidth(160);
			secondaryStage.setMinHeight(120);
			secondaryStage.setScene(new Scene(root, 160, 120));
			secondaryStage.show();

			controller.setSelectedFile(this.fileSelected);
		}
	}

	public void shutdown()
	{
		//shutdownButton.setImage(new Image("@exit_image.jpg"));
		Node.getInstance().stop();
		System.exit(0);
	}

	public void setPopUpOpen (Boolean value)
	{
		this.popUpOpen = value;
	}

}
