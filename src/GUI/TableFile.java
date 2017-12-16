package GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableFile implements Comparable<TableFile>{
	private final StringProperty fileName;
	private final StringProperty size;

	public TableFile()
	{
		this(null,null);
	}

	public TableFile(String fN, String s)
	{
		this.fileName = new SimpleStringProperty(fN);
		this.size = new SimpleStringProperty(s);
	}

	public String getFileName()
	{
		return this.fileName.get();
	}

	public StringProperty fileNameProperty()
	{
		return this.fileName;
	}

	public void setFileName(String fName)
	{
		this.fileName.set(fName);
	}

	public String getSize()
	{
		return this.size.get();
	}

	public StringProperty sizeProperty()
	{
		return this.size;
	}

	public void setSize(String siz)
	{
		this.size.set(siz);
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
