package bp.format;

public class BPFormatBMP implements BPFormat
{
	public final static String FORMAT_BMP = "BMP";

	public String getName()
	{
		return FORMAT_BMP;
	}

	public String[] getExts()
	{
		return new String[] { ".bmp", "image/bmp" };
	}
}