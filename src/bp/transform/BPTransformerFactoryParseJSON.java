package bp.transform;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import bp.util.JSONUtil;

public class BPTransformerFactoryParseJSON implements BPTransformerFactory
{
	public String getName()
	{
		return "Parse JSON";
	}

	public boolean checkData(Object source)
	{
		if (source == null)
			return false;
		if (source instanceof String)
			return true;
		return false;
	}

	public Collection<String> getFunctionTypes()
	{
		return new CopyOnWriteArrayList<String>(new String[] { TF_TOMAP, TF_TOLIST, TF_TOOBJ });
	}

	public BPTransformer<?> createTransformer(String func)
	{
		return new BPTransformerParseJSON();
	}

	public static class BPTransformerParseJSON extends BPTransformerBase<String>
	{
		protected Object transform(String t)
		{
			if (t == null)
				return null;
			return JSONUtil.decode(t);
		}

		public String getInfo()
		{
			return "Parse JSON";
		}
	}
}