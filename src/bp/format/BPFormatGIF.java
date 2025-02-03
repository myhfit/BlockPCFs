package bp.format;

public class BPFormatGIF implements BPFormat
{
	public final static String FORMAT_GIF = "GIF";

	public String getName()
	{
		return FORMAT_GIF;
	}

	public String[] getExts()
	{
		return new String[] { ".gif", "image/gif" };
	}
}