package bp.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import bp.config.BPConfig;
import bp.data.BPTreeData.BPTreeDataArrayList;
import bp.format.BPFormatYAML;
import bp.util.ObjUtil;
import bp.util.Std;
import bp.util.TextUtil;
import bp.util.YAMLUtil;

public class BPYAMLContainerBase<D extends BPMData> extends BPTextContainerBase implements BPMContainer<D>, BPTreeDataContainer
{
	@SuppressWarnings("unchecked")
	public D readMData(boolean loadsub)
	{
		String text = readAllText();
		Map<String, Object> mobj = YAMLUtil.decode(text);
		BPMData d = ObjUtil.mapToObj2(mobj, false);
		return (D) d;
	}

	public Boolean writeMData(D data, boolean savesub)
	{
		String yaml;
		try
		{
			if (data instanceof BPSLData)
			{
				yaml = YAMLUtil.encode(((BPSLData) data).getSaveData());
			}
			else
			{
				yaml = YAMLUtil.encode(data.getMappedData());
			}
			if (yaml != null)
			{
				return writeAll(TextUtil.fromString(yaml, "utf-8"));
			}
		}
		catch (Exception e)
		{
			Std.err(e);
		}
		return false;
	}

	public BPTreeData readTreeData()
	{
		String text = readAllText();
		Object mobj = YAMLUtil.decode(text);
		BPTreeData rc = new BPTreeData.BPTreeDataObj();
		if (mobj != null)
		{
			if (mobj instanceof List)
			{
				rc = new BPTreeDataArrayList();
				rc.setRoot(mobj);
			}
			else
				rc.setRoot(mobj);
		}
		return rc;
	}

	public CompletionStage<BPTreeData> readTreeDataAsync()
	{
		return CompletableFuture.supplyAsync(this::readTreeData);
	}

	public Boolean writeTreeData(BPTreeData data)
	{
		String yaml;
		try
		{
			yaml = YAMLUtil.encode(data.getRoot());
			if (yaml != null)
			{
				return writeAllText(yaml);
			}
		}
		catch (Exception e)
		{
			Std.err(e);
		}
		return false;
	}

	public CompletionStage<Boolean> writeTreeDataAsync(BPTreeData data)
	{
		return null;
	}

	public static class BPYAMLContainerFactory implements BPDataContainerFactory
	{
		public boolean canHandle(String format)
		{
			return BPFormatYAML.FORMAT_YAML.equals(format);
		}

		public String getName()
		{
			return "YAML";
		}

		@SuppressWarnings("unchecked")
		public <T extends BPDataContainer> T createContainer(BPConfig config)
		{
			BPYAMLContainerBase<BPMData> h = new BPYAMLContainerBase<BPMData>();
			return (T) h;
		}

		public String getFormat()
		{
			return BPFormatYAML.FORMAT_YAML;
		}
	}
}
