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

	public void init()
	{
		//currentWindow = (Stage) openButton.getScene().getWindow();
		//File file = new File(Node.getInstance()..getFullPath(filename, type));
		//return file.exists();
		//System.out.println(Node.getInstance().getFileManager().hasFile(selectedFile,FileType.LOCAL_FILE));
		//if(!Node.getInstance().getFileManager().hasFile(selectedFile,FileType.LOCAL_FILE))
		//{
			//deleteLocalButton.setVisible(false);
		//}
		//else{
			//deleteLocalButton.setVisible(true);
		//}
	}

	/**
	 *
	 */
	public void openFile()
	{

		System.out.println("Open file " + this.selectedFile + " ...");
		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.LOCAL_FILE))
		{
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.LOCAL_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.REPLICATED_FILE))
		{
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.REPLICATED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.DOWNLOADED_FILE))
		{
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.DOWNLOADED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.OWNED_FILE))
		{
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.OWNED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		else
		{
			Node.getInstance().getAgentHandler().downloadFile(this.selectedFile);
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File(Node.getInstance().getFileManager().getFolder(FileType.DOWNLOADED_FILE) + PREFIX + this.selectedFile);
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			//return true;
		}
		//currentWindow.close();
	}

	public void deleteLocal()
	{
		System.out.println("Delete file local...");
		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.LOCAL_FILE))
		{
			try
			{
				Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.LOCAL_FILE);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		//currentWindow.close();
	}

	public void deleteNetwork()
	{
		System.out.println("Delete file on network...");
		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, FileType.OWNED_FILE))
		{
			FileLedger fileLedger = Node.getInstance().getFileManager().getFileLedgers().get(this.selectedFile);
			short localId = fileLedger.getLocalID();
			short replicatedId = fileLedger.getReplicatedId();
			Set<Short> downloaders = fileLedger.getCopies();
			if(localId != Node.DEFAULT_ID)
			{
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,FileType.LOCAL_FILE);
					Node.getInstance().getAgentHandler().deleteFile(selectedFile);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			if(replicatedId != Node.DEFAULT_ID)
			{
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.REPLICATED_FILE);
					Node.getInstance().getAgentHandler().deleteFile(selectedFile);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			for(short downId : downloaders){
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,FileType.DOWNLOADED_FILE);
					Node.getInstance().getAgentHandler().deleteFile(selectedFile);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.OWNED_FILE);
				Node.getInstance().getAgentHandler().deleteFile(selectedFile);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			Node.getInstance().getFileManager().deleteFileLedger(fileLedger.getFileName());
		}
		else
		{
			FileLedger fileLedger;
			try
			{
				short ownerId = Node.getInstance().getResolverStub().getOwnerID(this.selectedFile);
				fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,this.selectedFile);
				short localId = fileLedger.getLocalID();
				short replicatedId = fileLedger.getReplicatedId();
				Set<Short> downloaders = fileLedger.getCopies();
				if(localId != Node.DEFAULT_ID)
				{
					if(localId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,FileType.LOCAL_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.LOCAL_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
				}
				if(replicatedId != Node.DEFAULT_ID)
				{
					if(replicatedId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.REPLICATED_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.REPLICATED_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
				}
				for(short downId : downloaders){
					if(downId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,FileType.DOWNLOADED_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.DOWNLOADED_FILE);
						Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					}
				}
				if(ownerId != 0)
				{
					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.OWNED_FILE);
					Node.getInstance().getAgentHandler().deleteFile(selectedFile);
					Node.getInstance().getFileManager().deleteFileLedgerRemote(fileLedger.getFileName());
				}
			} catch (RemoteException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		//currentWindow.close();
	}

	public void setSelectedFile(String selectedFile)
	{
		this.selectedFile = selectedFile;
	}



}