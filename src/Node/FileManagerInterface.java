package Node;

public interface FileManagerInterface
{
	public void checkFiles ();

	public void pushFile (String filenamme, int fileSize, FileType type);

	public void pullFile (short dstID, String filename);
}
