package GUI;

import javafx.beans.property.SimpleStringProperty;

public class TableFile implements Comparable<TableFile>{
	private final SimpleStringProperty fileName = new SimpleStringProperty("");
	private final SimpleStringProperty size = new SimpleStringProperty("");

	public TableFile()
	{
		this("", "");
	}

	public TableFile(String fileName, String size)
	{
		setFileName(fileName);
		setSize(size);
	}

	public String getFileName()
	{
		return fileName.get();
	}

	public void setFileName(String fName)
	{
		fileName.set(fName);
	}

	public String getSize()
	{
		return size.get();
	}

	public void setSize(String siz)
	{
		size.set(siz);
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
