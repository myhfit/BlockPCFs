package bp.transform;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import bp.util.Std;

public class BPTransformerFactoryImage2BA implements BPTransformerFactory
{
	public String getName()
	{
		return "Image to byte[]";
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
		return new CopyOnWriteArrayList<String>(new String[] { TF_TOBYTEARRAY });
	}

	public BPTransformer<?> createTransformer(String func)
	{
		if (TF_TOBYTEARRAY.equals(func))
			return new BPTransformerImage2BA();
		return null;
	}

	public static class BPTransformerImage2BA extends BPTransformerBase<Object>
	{
		protected String m_outformat;

		protected Object transform(Object t)
		{
			if (t == null)
				return null;
			if (t instanceof Image)
			{
				Image img = (Image) t;
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream())
				{
					ImageIO.write((RenderedImage) img, getOutputFormat(), bos);
					return bos.toByteArray();
				}
				catch (IOException e)
				{
					Std.err(e);
				}

			}
			return null;
		}

		protected String getOutputFormat()
		{
			return m_outformat == null ? "PNG" : m_outformat;
		}

		public String getInfo()
		{
			return "Image to byte[]";
		}
	}
}