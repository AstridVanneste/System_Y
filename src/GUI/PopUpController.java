//package GUI;
//
//import Node.*;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//
//import java.awt.*;
//import java.io.File;
//import java.io.IOException;
//import java.rmi.RemoteException;
//import java.util.Set;
//
//import static Node.FileType.*;
//
//public class PopUpController
//{
//
//	private ObservableList<TableFile> files;
//	private String selectedFile;
//
//	private static final String PREFIX = "/";
//
//	@FXML
//	private javafx.scene.control.Button deleteLocalButton;
//	@FXML
//	private javafx.scene.control.Button deleteNetworkButton;
//	@FXML
//	private javafx.scene.control.Button openButton;
//
//	public PopUpController()
//	{
//	}
//
//	/**
//	 *
//	 */
//	public void openFile()
//	{
//
//		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, LOCAL_FILE))
//		{
//			if (Desktop.isDesktopSupported()) {
//				try {
//					File myFile = new File(Node.getInstance().getFileManager().getFolder(LOCAL_FILE) + PREFIX + this.selectedFile);
//					Desktop.getDesktop().open(myFile);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//			//return true;
//		}
//		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, REPLICATED_FILE))
//		{
//			if (Desktop.isDesktopSupported()) {
//				try {
//					File myFile = new File(Node.getInstance().getFileManager().getFolder(REPLICATED_FILE) + PREFIX + this.selectedFile);
//					Desktop.getDesktop().open(myFile);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//			//return true;
//		}
//		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, DOWNLOADED_FILE))
//		{
//			if (Desktop.isDesktopSupported()) {
//				try {
//					File myFile = new File(Node.getInstance().getFileManager().getFolder(DOWNLOADED_FILE) + PREFIX + this.selectedFile);
//					Desktop.getDesktop().open(myFile);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//			//return true;
//		}
//		else if(Node.getInstance().getFileManager().hasFile(this.selectedFile, OWNED_FILE))
//		{
//			if (Desktop.isDesktopSupported()) {
//				try {
//					File myFile = new File(Node.getInstance().getFileManager().getFolder(OWNED_FILE) + PREFIX + this.selectedFile);
//					Desktop.getDesktop().open(myFile);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//			//return true;
//		}
//		else
//		{
//			Node.getInstance().getFileManager().requestFile(this.selectedFile);
//			if (Desktop.isDesktopSupported()) {
//				try {
//					File myFile = new File(Node.getInstance().getFileManager().getFolder(DOWNLOADED_FILE) + PREFIX + this.selectedFile);
//					Desktop.getDesktop().open(myFile);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//			//return true;
//		}
//	}
//
//	public void deleteLocal()
//	{
//
//		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, LOCAL_FILE))
//		{
//			try
//			{
//				Node.getInstance().getFileManager().deleteFile(this.selectedFile,LOCAL_FILE);
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//		else
//		{
//			FileLedger fileLedger;
//			short ownerId = 0;
//			try
//			{
//				ownerId = Node.getInstance().getResolverStub().getOwnerID(this.selectedFile);
//			} catch (RemoteException e)
//			{
//				e.printStackTrace();
//			}
//			fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,this.selectedFile);
//			short localId = fileLedger.getLocalID();
//			if(localId != 0)
//			{
//				Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,LOCAL_FILE);
//			}
//		}
//	}
//
//	public void deleteNetwork()
//	{
//
//		if(Node.getInstance().getFileManager().hasFile(this.selectedFile, OWNED_FILE))
//		{
//			FileLedger fileLedger = Node.getInstance().getFileManager().getFileLedgers().get(this.selectedFile);
//			short localId = fileLedger.getLocalID();
//			short replicatedId = fileLedger.getReplicatedId();
//			Set<Short> downloaders = fileLedger.getCopies();
//			if(localId != 0)
//			{
//				Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,LOCAL_FILE);
//			}
//			if(localId != 0)
//			{
//				Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,REPLICATED_FILE);
//			}
//			for(short downId : downloaders){
//				Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,DOWNLOADED_FILE);
//			}
//			try
//			{
//				Node.getInstance().getFileManager().deleteFile(this.selectedFile,OWNED_FILE);
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//			Node.getInstance().getFileManager().deleteFileLedger(fileLedger.getFileName());
//		}
//		else
//		{
//			FileLedger fileLedger;
//			try
//			{
//				short ownerId = Node.getInstance().getResolverStub().getOwnerID(this.selectedFile);
//				fileLedger = Node.getInstance().getFileManager().getFileLedgerRemote(ownerId,this.selectedFile);
//				short localId = fileLedger.getLocalID();
//				short replicatedId = fileLedger.getReplicatedId();
//				Set<Short> downloaders = fileLedger.getCopies();
//				if(localId != 0)
//				{
//					if(localId != Node.getInstance().getId())
//					{
//						Node.getInstance().getFileManager().deleteFileRemote(localId,this.selectedFile,LOCAL_FILE);
//					}
//					else
//					{
//						Node.getInstance().getFileManager().deleteFile(this.selectedFile,LOCAL_FILE);
//					}
//				}
//				if(replicatedId != 0)
//				{
//					if(replicatedId != Node.getInstance().getId())
//					{
//						Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,REPLICATED_FILE);
//					}
//					else
//					{
//						Node.getInstance().getFileManager().deleteFile(this.selectedFile,REPLICATED_FILE);
//					}
//				}
//				for(short downId : downloaders){
//					if(downId != Node.getInstance().getId())
//					{
//						Node.getInstance().getFileManager().deleteFileRemote(downId,this.selectedFile,DOWNLOADED_FILE);
//					}
//					else
//					{
//						Node.getInstance().getFileManager().deleteFile(this.selectedFile,DOWNLOADED_FILE);
//					}
//				}
//				if(ownerId != 0)
//				{
//					Node.getInstance().getFileManager().deleteFileRemote(replicatedId,this.selectedFile,OWNED_FILE);
//					Node.getInstance().getFileManager().deleteFileLedgerRemote(fileLedger.getFileName());
//				}
//			} catch (RemoteException e)
//			{
//				e.printStackTrace();
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public void setFiles(ObservableList<TableFile> files)
//	{
//		this.files = files;
//	}
//
//
//	public void setSelectedFile(String selectedFile)
//	{
//		this.selectedFile = selectedFile;
//	}
//
//
//
//}