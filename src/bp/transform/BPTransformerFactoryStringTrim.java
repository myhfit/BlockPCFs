package bp.transform;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class BPTransformerFactoryStringTrim implements BPTransformerFactory
{
	public String getName()
	{
		return "String trim";
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
		return new CopyOnWriteArrayList<String>(new String[] { TF_TOSTRING });
	}

	public BPTransformer<?> createTransformer(String func)
	{
		if (TF_TOSTRING.equals(func))
			return new BPTransformerStringTrim();
		return null;
	}

	public static class BPTransformerStringTrim extends BPTransformerBase<Object>
	{
		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof String)
			{
				return ((String)t).trim();
			}
			return null;
		}

		public String getInfo()
		{
			return "String trim";
		}
	}
}