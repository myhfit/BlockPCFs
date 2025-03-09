package bp.transform;

import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class BPTransformerFactoryBase642BA implements BPTransformerFactory
{
	public String getName()
	{
		return "Base64 to byte[]";
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
		return new CopyOnWriteArrayList<String>(new String[] { TF_TOBYTEARRAY });
	}

	public BPTransformer<?> createTransformer(String func)
	{
		if (TF_TOBYTEARRAY.equals(func))
			return new BPTransformerBase642BA();
		return null;
	}

	public static class BPTransformerBase642BA extends BPTransformerBase<Object>
	{
		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof String)
			{
				return Base64.getDecoder().decode((String) t);
			}
			return null;
		}

		public String getInfo()
		{
			return "Base64 to byte[]";
		}
	}
}