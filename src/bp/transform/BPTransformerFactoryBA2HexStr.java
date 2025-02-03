package bp.transform;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class BPTransformerFactoryBA2HexStr implements BPTransformerFactory
{
	public String getName()
	{
		return "byte[] to HexStr";
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
			return new BPTransformerBA2HexStr();
		return null;
	}

	public static class BPTransformerBA2HexStr extends BPTransformerBase<Object>
	{
		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof byte[])
			{
				byte[] bs = (byte[]) t;
				int l = bs.length;
				char[] chs = new char[l * 2];
				int b;
				int b1;
				for (int i = 0, j = 0; i < l; i++)
				{
					b = bs[i] & 0xff;
					b1 = ((b & 0xf0) >> 4);
					if (b1 > 9)
						chs[j++] = (char) (b1 + '7');
					else
						chs[j++] = (char) (b1 + '0');
					b1 = (b & 0xf);
					if (b1 > 9)
						chs[j++] = (char) (b1 + '7');
					else
						chs[j++] = (char) (b1 + '0');
				}
				return new String(chs);
			}
			return null;
		}

		public String getInfo()
		{
			return "byte[] to HexStr";
		}
	}
}
