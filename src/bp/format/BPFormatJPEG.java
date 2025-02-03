package bp.format;

public class BPFormatJPEG implements BPFormat
{
	public final static String FORMAT_JPEG = "JPEG";

	public String getName()
	{
		return FORMAT_JPEG;
	}

	public String[] getExts()
	{
		return new String[] { ".jpg", ".jpeg", ".jpe", "image/jpeg" };
	}
}