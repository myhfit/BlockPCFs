package bp.format;

public class BPFormatM3U implements BPFormat
{
	public final static String FORMAT_M3U = "M3U";

	public String getName()
	{
		return FORMAT_M3U;
	}

	public String[] getExts()
	{
		return new String[] { ".m3u" };
	}
}