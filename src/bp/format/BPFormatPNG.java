package bp.format;

public class BPFormatPNG implements BPFormat
{
	public final static String FORMAT_PNG = "PNG";

	public String getName()
	{
		return FORMAT_PNG;
	}

	public String[] getExts()
	{
		return new String[] { ".png", "image/png" };
	}
}
