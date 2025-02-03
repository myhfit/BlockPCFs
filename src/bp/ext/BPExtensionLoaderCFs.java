package bp.ext;

public class BPExtensionLoaderCFs implements BPExtensionLoader
{
	public String getName()
	{
		return "CommonFormats";
	}

	public boolean isUI()
	{
		return false;
	}

	public String getUIType()
	{
		return null;
	}

	public String[] getParentExts()
	{
		return null;
	}

	public String[] getDependencies()
	{
		return null;
	}
}
