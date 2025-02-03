package bp.transform;

import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class BPTransformerFactoryBA2Base64 implements BPTransformerFactory
{
	public String getName()
	{
		return "byte[] to Base64";
	}

	public boolean checkData(Object source)
	{
		if (source == null)
			return false;
		if (source instanceof byte[])
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
			return new BPTransformerBA2Base64();
		return null;
	}

	public static class BPTransformerBA2Base64 extends BPTransformerBase<Object>
	{
		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof byte[])
			{
				return Base64.getEncoder().encodeToString((byte[]) t);
			}
			return null;
		}

		public String getInfo()
		{
			return "byte[] to Base64";
		}
	}
}
