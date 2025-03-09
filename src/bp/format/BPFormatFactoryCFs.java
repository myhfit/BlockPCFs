package bp.format;

import java.util.function.Consumer;

public class BPFormatFactoryCFs implements BPFormatFactory
{
	public void register(Consumer<BPFormat> regfunc)
	{
		regfunc.accept(new BPFormatJPEG());
		regfunc.accept(new BPFormatPNG());
		regfunc.accept(new BPFormatBMP());
		regfunc.accept(new BPFormatGIF());
		regfunc.accept(new BPFormatM3U());
		regfunc.accept(new BPFormatM3U8());
		regfunc.accept(new BPFormatXML());
	}
}
