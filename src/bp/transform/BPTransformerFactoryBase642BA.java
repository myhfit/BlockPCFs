package bp.transform;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import bp.config.BPConfig;
import bp.config.BPSetting;
import bp.config.BPSettingBase;
import bp.config.BPSettingItem;

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
		protected int m_encoding = 0;

		protected final static String[] S_ENCODING = { "RFC4648", "RFC4648_URLSAFE", "RFC2045" };

		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof String)
			{
				Decoder d;
				switch (m_encoding)
				{
					case 1:
						d = Base64.getUrlDecoder();
						break;
					case 2:
						d = Base64.getMimeDecoder();
						break;
					default:
						d = Base64.getDecoder();
				}
				return d.decode((String) t);
			}
			return null;
		}

		public String getInfo()
		{
			return "Base64 to byte[]";
		}

		public BPSetting getSetting()
		{
			BPSettingBase rc = new BPSettingBase().addItem(BPSettingItem.create("encoding", "Encoding", BPSettingItem.ITEM_TYPE_SELECT, S_ENCODING));
			rc.set("encoding", S_ENCODING[m_encoding]);
			return rc;
		}

		public void setSetting(BPConfig config)
		{
			int r = 0;
			if (config != null)
			{
				String encoding = config.get("encoding");
				for (int i = 0; i < S_ENCODING.length; i++)
				{
					if (S_ENCODING[i].equals(encoding))
					{
						r = i;
						break;
					}
				}
			}
			m_encoding = r;
		}
	}
}