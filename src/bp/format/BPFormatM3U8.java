package bp.format;

public class BPFormatM3U8 implements BPFormat
{
	public final static String FORMAT_M3U8 = "M3U8";

	public String getName()
	{
		return FORMAT_M3U8;
	}

	public String[] getExts()
	{
		return new String[] { ".m3u8" };
	}
}