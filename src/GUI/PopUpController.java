package GUI;

import Node.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Set;

import static Node.FileType.LOCAL_FILE;
import static Node.FileType.REPLICATED_FILE;

public class PopUpController
{

	private Scene view;
	private Stage currentWindow;
	private ManageController manageController;

	private String selectedFile;

	private static final String PREFIX = "/";

	@FXML
	private javafx.scene.control.Button deleteLocalButton;
	@FXML
	private javafx.scene.control.Button deleteNetworkButton;
	@FXML
	private javafx.scene.control.Button openButton;

	public PopUpController()
	{
	}

	public void init(ManageController manageController)
	{
		this.manageController = manageController;
	}

	public void view (Parent root, String fileName)
	{
		if(view == null)
			view = new Scene(root,160,120);

		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent arg0) {
				ManageController.getInstance().getMainController().setPopUpOpen(false);
				Stage stage = (Stage) openButton.getScene().getWindow();
				stage.close();
			}
		});

		stage.initStyle((StageStyle.UTILITY));
		stage.setScene(view);
		stage.show();

		setSelectedFile(fileName);
		this.currentWindow = (Stage) openButton.getScene().getWindow();
	}



	public void openFile()
	{

		System.out.println("Open file " + this.selectedFile + " ...");
		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.LOCAL_FILE))
		{
			if (Desktop.isDesktopSupported())
			{
				try
				{
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.LOCAL_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.REPLICATED_FILE))
		{
			if (Desktop.isDesktopSupported())
			{
				try
				{
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.REPLICATED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.DOWNLOADED_FILE))
		{
			if (Desktop.isDesktopSupported())
			{
				try
				{
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.DOWNLOADED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.OWNED_FILE))
		{
			if (Desktop.isDesktopSupported())
			{
				try
				{
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.OWNED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else
		{
			Node.getInstance().getAgentHandler().downloadFile(this.selectedFile);
			if (Desktop.isDesktopSupported())
			{
				try
				{
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.DOWNLOADED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		ManageController.getInstance().getMainController().setPopUpOpen(false);
		currentWindow.close();
	}

	public void deleteLocal()
	{
		System.out.println("Delete file local...");
		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.LOCAL_FILE))
		{
			try
			{
				Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.LOCAL_FILE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.REPLICATED_FILE))
		{
			try
			{
				Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.REPLICATED_FILE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		ManageController.getInstance().getMainController().setPopUpOpen(false);
		currentWindow.close();

	}

	public void deleteNetwork()
	{
		Node.getInstance().getAgentHandler().deleteFile(selectedFile);
		ManageController.getInstance().getMainController().setPopUpOpen(false);
		currentWindow.close();
	}

	public void setSelectedFile(String selectedFile)
	{
		this.selectedFile = selectedFile;

		try
		{

			short ownerId = Node.getInstance().getResolverStub().getOwnerID(this.selectedFile);

			if (ownerId != Node.getInstance().getId())
			{
				FileLedger fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId, this.selectedFile);

				//System.out.println("lokale : " + fileLedger.getLocalID());
				//System.out.println("replicated: " + fileLedger.getReplicatedId());

				if (!(fileLedger.getLocalID() == Node.getInstance().getId()) || !(fileLedger.getReplicatedId() == Node.getInstance().getId()))
				{
					deleteLocalButton.setVisible(false);
				}
			}
			else
				deleteLocalButton.setVisible(false);

		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}
}