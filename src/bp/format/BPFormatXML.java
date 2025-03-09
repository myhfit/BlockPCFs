package bp.format;

public class BPFormatXML implements BPFormat
{
	public final static String FORMAT_XML = "XML";

	public String getName()
	{
		return FORMAT_XML;
	}

	public String[] getExts()
	{
		return new String[] { ".xml", "text/xml" };
	}
}
