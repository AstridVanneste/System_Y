package GUI;

import Node.*;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Set;

import static Node.FileType.LOCAL_FILE;
import static Node.FileType.REPLICATED_FILE;

public class PopUpController
{
	private Stage currentWindow;
	private MainController mainController;

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

	public void init(MainController mainController)
	{
		this.mainController = mainController;
	}

	/**
	 *
	 */
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
		//currentWindow.close();
		mainController.setPopUpOpen(false);
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
		//currentWindow.close();
		mainController.setPopUpOpen(false);
	}

	public void deleteNetwork()
	{
		Node.getInstance().getAgentHandler().deleteFile(selectedFile);
		mainController.setPopUpOpen(false);
	}

	public void setSelectedFile(String selectedFile)
	{
		this.selectedFile = selectedFile;
		if(!Node.getInstance().getFileManager().hasFile(selectedFile,FileType.LOCAL_FILE))
		{
			deleteLocalButton.setVisible(false);
		}
	}
}