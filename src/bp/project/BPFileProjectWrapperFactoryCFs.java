package bp.project;

import java.util.function.BiConsumer;
import java.util.function.Function;

import bp.res.BPResource;

public class BPFileProjectWrapperFactoryCFs implements BPFileProjectWrapperFactory
{
	public void installWrapper(BiConsumer<String, Function<BPResource, BPResource>> cb)
	{
//		cb.accept(".csv", BPFileProjectWrapperFactoryCFs::wrapDSV);
//		cb.accept(".tsv", BPFileProjectWrapperFactoryCFs::wrapDSV);
	}

	protected final static BPResource wrapDSV(BPResource res)
	{
		return null;
	}
}
