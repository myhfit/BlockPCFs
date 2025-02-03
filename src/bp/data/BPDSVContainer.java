package bp.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import bp.res.BPResourceIO;
import bp.util.IOUtil;
import bp.util.Std;
import bp.util.TextUtil;

public class BPDSVContainer extends BPTextContainerBase implements BPXYDContainer, BPTextContainer
{
	protected String m_delimiter;

	protected ExecutorService m_exec;

	public BPDSVContainer(String encoding, String delimiter)
	{
		m_encoding = encoding;
		m_delimiter = delimiter;
	}

	public void open()
	{
		m_exec = Executors.newSingleThreadExecutor(new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		super.open();
	}

	public void close()
	{
		if (m_exec != null && !m_exec.isShutdown())
		{
			m_exec.shutdown();
			m_exec = null;
		}
		super.close();
	}

	public String getDelimiter()
	{
		return m_delimiter;
	}

	public BPXYData readXYData()
	{
		return readXYDData();
	}

	public boolean structureEditable()
	{
		return true;
	}

	public BPXYDData readXYDData()
	{
		BPXYDData rc = null;
		try
		{
			rc = readXYDDataAsync(null).toCompletableFuture().get();
		}
		catch (InterruptedException e)
		{
			Std.err(e);
		}
		catch (ExecutionException e)
		{
			Std.err(e);
			throw new RuntimeException(e.getCause());
		}
		return rc;
	}

	public CompletionStage<BPXYData> readXYDataAsync()
	{
		return readXYDDataAsync(null).handle((data, ex) ->
		{
			if (ex != null)
				throw new RuntimeException(ex);
			return data;
		});
	}

	public CompletionStage<BPXYDData> readXYDDataAsync(Consumer<BPXYDData> preparecallback)
	{
		Supplier<BPXYDData> seg = new DSVReadSeg((BPResourceIO) m_res, m_encoding, m_delimiter, preparecallback);
		return CompletableFuture.supplyAsync(seg, m_exec);
	}

	public Boolean writeXYData(BPXYData data)
	{
		Boolean rc = null;
		try
		{
			rc = writeXYDataAsync(data).toCompletableFuture().get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			Std.err(e);
		}
		return rc;
	}

	public CompletionStage<Boolean> writeXYDataAsync(BPXYData data)
	{
		Supplier<Boolean> seg = new DSVWriteSeg((BPResourceIO) m_res, data, m_delimiter, m_encoding, null);
		return CompletableFuture.supplyAsync(seg, m_exec);
	}

	public Boolean writeXYDData(BPXYDData data)
	{
		Boolean rc = null;
		try
		{
			rc = writeXYDDataAsync(data, new WeakReference<Runnable>(() -> data.complete())).toCompletableFuture().get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			Std.err(e);
		}
		return rc;
	}

	public CompletionStage<Boolean> writeXYDDataAsync(BPXYDData data, WeakReference<Runnable> setupcallback)
	{
		Supplier<Boolean> seg = new DSVWriteSeg((BPResourceIO) m_res, data, m_delimiter, m_encoding, setupcallback);
		return CompletableFuture.supplyAsync(seg, m_exec);
	}

	protected static class DSVWriteSeg implements Supplier<Boolean>
	{
		protected volatile BPXYData m_data;
		protected volatile String m_delimiter;
		protected volatile String m_encoding;
		protected volatile BPResourceIO m_res;
		protected final SimpleDateFormat s_ndf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		protected volatile WeakReference<Runnable> m_setupcallback;

		public DSVWriteSeg(BPResourceIO res, BPXYData data, String delimiter, String encoding, WeakReference<Runnable> setupcallback)
		{
			m_res = res;
			m_data = data;
			m_delimiter = delimiter;
			m_encoding = encoding;
			m_setupcallback = setupcallback;
		}

		public Boolean get()
		{
			BPXYData data = m_data;
			String[] cns = data.getColumnNames();
			BPResourceIO res = m_res;
			String delimiter = m_delimiter;
			String encoding = m_encoding;
			boolean isxyd = data instanceof BPXYDData;
			boolean needwaitcomplete = false;
			AtomicBoolean isc = new AtomicBoolean();
			ConcurrentLinkedQueue<BPXData> tempdatas = new ConcurrentLinkedQueue<>();
			Object lo = new Object();
			WeakReference<Runnable> setupcallbackref = m_setupcallback;
			WeakReference<BiConsumer<List<BPXData>, Integer>> iref = new WeakReference<BiConsumer<List<BPXData>, Integer>>((pagedata, pos) ->
			{
				if (pagedata != null)
				{
					tempdatas.addAll(pagedata);
				}
				synchronized (lo)
				{
					lo.notify();
				}
			});
			WeakReference<Runnable> cref = new WeakReference<Runnable>(() ->
			{
				isc.set(true);
				synchronized (lo)
				{
					lo.notify();
				}
			});
			if (isxyd)
			{
				((BPXYDData) data).setDataListener(iref, null, cref);
				if (setupcallbackref != null)
				{
					Runnable setupcallback = setupcallbackref.get();
					if (setupcallback != null)
					{
						setupcallback.run();
						needwaitcomplete = true;
					}
				}
			}
			final boolean nwc = needwaitcomplete;
			return res.useOutputStream((out) ->
			{
				try
				{
					int c = cns.length;
					writeHeader(cns, out, delimiter, encoding);
					writeDatas(data.getDatas(), c, out, delimiter, encoding);
					if (nwc)
					{
						// wait complete
						do
						{
							List<BPXData> page = new ArrayList<BPXData>();
							while (tempdatas.size() > 0)
							{
								page.add(tempdatas.poll());
							}
							if (page.size() > 0)
								writeDatas(page, c, out, delimiter, encoding);
							if (!isc.get())
							{
								synchronized (lo)
								{
									try
									{
										lo.wait(1000);
									}
									catch (InterruptedException e)
									{
										Std.err(e);
										break;
									}
								}
							}
						} while (!isc.get());
						((BPXYDData) data).clearDataListeners();
					}
					return true;
				}
				catch (IOException e)
				{
					Std.err(e);
					throw new RuntimeException(e);
				}
			});
		}

		protected void writeHeader(String[] cns, OutputStream out, String delimiter, String encoding) throws IOException
		{
			String[] headers = cns;
			StringBuilder sb = new StringBuilder();
			boolean t = false;
			for (String header : headers)
			{
				if (t)
				{
					sb.append(delimiter);
				}
				else
				{
					t = true;
				}
				sb.append(header);
			}
			out.write(sb.toString().getBytes(encoding));
		}

		protected void writeDatas(List<BPXData> datas, int colcount, OutputStream out, String delimiter, String encoding) throws IOException
		{
			StringBuilder sb;
			if (datas.size() > 0)
			{
				for (BPXData data : datas)
				{
					out.write(new byte[] { 13, 10 });
					sb = new StringBuilder();
					boolean t = false;
					for (int i = 0; i < colcount; i++)
					{
						Object item = data.getColValue(i);
						if (t)
						{
							sb.append(delimiter);
						}
						else
						{
							t = true;
						}
						sb.append(fix(item));
					}
					out.write(sb.toString().getBytes(encoding));
				}
			}
		}

		protected String fix(Object data)
		{
			if (data == null)
				return "";
			if (data instanceof Number)
			{
				return data.toString();
			}
			else if (data instanceof Date)
			{
				return s_ndf.format(data);
			}
			else
			{
				return escape(data.toString());
			}
		}

		protected String escape(String text)
		{
			if (text.indexOf('\"') > -1)
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < text.length(); i++)
				{
					char c = text.charAt(i);
					if (c == '\"')
					{
						sb.append('"');
					}
					sb.append(c);
				}
				return sb.toString();
			}
			else
			{
				return "\"" + text + "\"";
			}
		}
	}

	protected static class DSVReadSeg implements Supplier<BPXYDData>
	{
		protected Consumer<BPXYDData> m_callback;
		protected String m_encoding;
		protected String m_delimiter;
		protected BPResourceIO m_res;

		public DSVReadSeg(BPResourceIO res, String encoding, String delimiter, Consumer<BPXYDData> preparecallback)
		{
			m_res = res;
			m_encoding = encoding;
			m_delimiter = delimiter;
			m_callback = preparecallback;
		}

		public BPXYDData get()
		{
			BPResourceIO res = m_res;
			return res.useInputStream(this::read);
		}

		protected BPXYDData read(InputStream in)
		{
			String encoding = m_encoding;
			String txt = TextUtil.toString(IOUtil.read(in), encoding);
			BPDSVData data = new BPDSVData();
			data.setCompatibleOnlyNewLine(true);
			data.readHeader(txt, m_delimiter);
			if (m_callback != null)
				m_callback.accept(data);
			data.read(txt, m_delimiter);
			return data;
		}
	}
}
