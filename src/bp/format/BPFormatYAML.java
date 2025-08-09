package bp.format;

public class BPFormatYAML implements BPFormat
{
	public final static String FORMAT_YAML = "YAML";

	public String getName()
	{
		return FORMAT_YAML;
	}

	public String[] getExts()
	{
		return new String[] { ".yaml", ".yml", "text/yaml", "application/yaml" };
	}

	public boolean checkFeature(BPFormatFeature feature)
	{
		if (feature == BPFormatFeature.TREE || feature == BPFormatFeature.OBJTREE)
			return true;
		return false;
	}
}