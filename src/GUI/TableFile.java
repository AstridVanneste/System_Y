package GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;

public class TableFile implements Comparable<TableFile>{
	private StringProperty fileName;
	private StringProperty size;

	public TableFile(String name, String size)
	{
		this.fileName = new SimpleStringProperty(name);
		this.size = new SimpleStringProperty(size);
	}

	public void setFileName(String value)
	{
		fileNameProperty().set(value);
	}

	public String getFileName()
	{
		return fileNameProperty().get();
	}

	public StringProperty fileNameProperty()
	{
		if (fileName == null)
			fileName = new SimpleStringProperty(this, "fileName");
		return fileName;
	}


	public void setSize(String value)
	{
		sizeProperty().set(value);
	}

	public String getSize()
	{
		return sizeProperty().get();
	}

	public StringProperty sizeProperty()
	{
		if (size == null)
			size = new SimpleStringProperty(this, "size");
		return size;
	}

	@Override
	public int compareTo(TableFile file)
	{
		// less than 0: the argument is greater than this object
		if (file != null)
		{
			return this.fileName.get().compareTo(file.fileName.get());
		}

		return 0;

	}
}
