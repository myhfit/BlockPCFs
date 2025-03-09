package bp.data;

import bp.config.BPConfig;
import bp.config.BPSetting;
import bp.format.BPFormatCSV;
import bp.format.BPFormatTSV;
import bp.format.BPFormatXYData;

public abstract class BPDSVContainerFactory implements BPDataContainerFactory
{
	public BPSetting getSetting()
	{
		return null;
	}

	public final static class BPCSVContainerFactory extends BPDSVContainerFactory
	{
		public String getName()
		{
			return "CSV";
		}

		@SuppressWarnings("unchecked")
		public <T extends BPDataContainer> T createContainer(BPConfig config)
		{
			BPDSVContainer con = new BPDSVContainer("utf-8", ",");
			return (T) con;
		}

		public boolean canHandle(String format)
		{
			return BPFormatXYData.FORMAT_XYDATA.equals(format);
		}

		public String getFormat()
		{
			return BPFormatCSV.FORMAT_CSV;
		}
	}

	public final static class BPTSVContainerFactory extends BPDSVContainerFactory
	{
		public String getName()
		{
			return "TSV";
		}

		@SuppressWarnings("unchecked")
		public <T extends BPDataContainer> T createContainer(BPConfig config)
		{
			BPDSVContainer con = new BPDSVContainer("utf-8", ",");
			return (T) con;
		}

		public boolean canHandle(String format)
		{
			return BPFormatXYData.FORMAT_XYDATA.equals(format);
		}

		public String getFormat()
		{
			return BPFormatTSV.FORMAT_TSV;
		}
	}
}
