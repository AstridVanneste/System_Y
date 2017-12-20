package GUI;

import Node.*;
import javafx.fxml.FXML;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Set;

import static Node.FileType.LOCAL_FILE;
import static Node.FileType.REPLICATED_FILE;

public class PopUpController
{
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
			Node.getInstance().getFileManager().requestFile(this.selectedFile);
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
		else
		{
			FileLedger fileLedger;
			short ownerId = 0;
			try
			{
				ownerId = Node.getInstance().getResolverStub().getOwnerID(this.selectedFile);
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
			try
			{
				fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,this.selectedFile);
				short localId = fileLedger.getLocalID();
				if(localId != 0)
				{

					Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,LOCAL_FILE);
				}
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}

		}
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
			if(localId != 0)
			{
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,FileType.LOCAL_FILE);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			if(localId != 0)
			{
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.REPLICATED_FILE);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			for(short downId : downloaders){
				try
				{
					Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,FileType.DOWNLOADED_FILE);
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.OWNED_FILE);
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
				if(localId != 0)
				{
					if(localId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,FileType.LOCAL_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.LOCAL_FILE);
					}
				}
				if(replicatedId != 0)
				{
					if(replicatedId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.REPLICATED_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.REPLICATED_FILE);
					}
				}
				for(short downId : downloaders){
					if(downId != Node.getInstance().getId())
					{
						Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,FileType.DOWNLOADED_FILE);
					}
					else
					{
						Node.getInstance().getFileManager().deleteFile(this.selectedFile,FileType.DOWNLOADED_FILE);
					}
				}
				if(ownerId != 0)
				{
					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,FileType.OWNED_FILE);
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
	}

	public void setSelectedFile(String selectedFile)
	{
		this.selectedFile = selectedFile;
	}



}