package bp.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class BPDSVData extends BPXYDDataBase
{
	protected int m_txtpos;
	protected boolean m_compatibleonlynewline = false;

	protected void readHeader(String txt, String delimiter)
	{
		if (txt != null)
		{
			char b;
			String headerstr = null;
			StringBuilder sb = new StringBuilder();
			boolean qs = true;
			boolean isr = false;
			int i;
			for (i = 0; i < txt.length(); i++)
			{
				b = txt.charAt(i);
				if (qs)
				{
					if (b == '"')
					{
						qs = false;
						sb.append(b);
					}
					else
					{
						if (b == '\r')
						{
							if (isr)
							{
								sb.append(b);
							}
							else
								isr = true;
						}
						else if (b == '\n')
						{
							if (isr || m_compatibleonlynewline)
							{
								isr = false;
								headerstr = sb.toString();
								break;
							}
							else
							{
								sb.append(b);
							}
						}
						else
						{
							if (isr)
							{
								sb.append('\r');
								isr = false;
							}
							sb.append(b);
						}
					}
				}
				else
				{
					if (b == '"')
						qs = true;
					sb.append(b);
				}
			}
			if (headerstr == null)
				headerstr = sb.toString();
			genCols(headerstr, delimiter);
			m_txtpos = i + 1;
		}
	}

	protected void genCols(String headerstr, String delimiter)
	{
		if (headerstr == null)
			return;
		List<String> strs = parseLine(headerstr, delimiter);
		int colcount = strs.size();
		m_cns = new String[colcount];
		m_ccs = new Class<?>[colcount];
		for (int i = 0; i < colcount; i++)
		{
			m_cns[i] = strs.get(i);
			m_ccs[i] = String.class;
		}
	}

	protected List<String> parseLine(String line, String delimiter)
	{
		char d = delimiter.charAt(0);
		List<String> rc = new ArrayList<String>();
		int li = 0;
		int qi = -1;
		boolean qs = false;
		StringBuilder sb = new StringBuilder();
		int len = line.length();
		for (int i = 0; i < len; i++)
		{
			char c = line.charAt(i);
			if (qs)
			{
				if (c == '"')
				{
					char nc = ((i < line.length() - 1) ? line.charAt(i + 1) : '\0');
					if (nc != '"')
					{
						sb.append(line.substring(qi, i));
						qs = false;
						li = i + 1;
						qi = -1;
					}
					else
					{
						sb.append(line.substring(qi, i));
						sb.append('"');
						li = i + 2;
						qi = i + 2;
						i++;
					}
				}
			}
			else
			{
				if (c == d)
				{
					sb.append(line.substring(li, i));
					rc.add(sb.toString());
					sb.setLength(0);
					li = i + 1;
				}
				else if (c == '"')
				{
					if (li != i)
					{
						sb.append(line.substring(li, i));
					}
					qi = i + 1;
					qs = true;
				}
			}
		}
		if (!qs)
		{
			sb.append(line.substring(li, line.length()));
			rc.add(sb.toString());
		}
		return rc;
	}

	public void setCompatibleOnlyNewLine(boolean flag)
	{
		m_compatibleonlynewline = flag;
	}

	public void read(String txt, String delimiter)
	{
		char b;
		ArrayList<BPXData> datas = new ArrayList<BPXData>();
		String linestr = null;
		StringBuilder sb = new StringBuilder();
		boolean qs = true;
		boolean isr = false;
		int count = 0;
		int page_size = 10000;
		WeakReference<BiConsumer<List<BPXData>, Integer>> insertref = m_insertcallback;
		BiConsumer<List<BPXData>, Integer> insertcb = insertref != null ? insertref.get() : null;
		List<BPXData> page = insertcb != null ? new ArrayList<BPXData>() : null;
		boolean compatibleonlynewline = m_compatibleonlynewline;
		for (int i = m_txtpos; i < txt.length(); i++)
		{
			b = txt.charAt(i);
			if (qs)
			{
				if (b == '"')
				{
					qs = false;
					sb.append(b);
				}
				else
				{
					if (b == '\r')
					{
						if (isr)
						{
							sb.append(b);
						}
						else
							isr = true;
					}
					else if (b == '\n')
					{
						if (isr || compatibleonlynewline)
						{
							linestr = sb.toString();
							BPXData line = getLine(linestr, delimiter);
							if (line != null)
							{
								datas.add(line);
								if (insertcb != null)
								{
									count++;
									page.add(line);
									if (page_size > 0 && count == page_size)
									{
										appendPage(new CopyOnWriteArrayList<BPXData>(page));
										page = new ArrayList<BPXData>();
										count = 0;
									}
								}
							}
							sb.setLength(0);
							isr = false;
						}
						else
						{
							sb.append(b);
						}
					}
					else
					{
						if (isr)
						{
							sb.append('\r');
							isr = false;
						}
						sb.append(b);
					}
				}
			}
			else
			{
				if (b == '"')
					qs = true;
				sb.append(b);
			}
		}
		if (sb.length() > 0)
		{
			linestr = sb.toString();
			BPXData line = getLine(linestr, delimiter);
			if (line != null)
			{
				datas.add(line);
				if (page != null)
				{
					count++;
					page.add(line);
					appendPage(new CopyOnWriteArrayList<BPXData>(page));
					count = 0;
				}
			}
		}
		else if (page != null && page.size() > 0)
		{
			appendPage(new CopyOnWriteArrayList<BPXData>(page));
			count = 0;
		}
		m_datas = new CopyOnWriteArrayList<BPXData>(datas);
	}

	protected BPXData getLine(String linestr, String delimiter)
	{
		if (linestr == null || linestr.length() == 0)
			return null;
		List<String> strs = parseLine(linestr, delimiter);
		Object[] arr = new Object[strs.size()];
		int s = strs.size();
		for (int i = 0; i < s; i++)
		{
			arr[i] = strs.get(i);
		}
		BPXData lineobjs = new BPXData.BPXDataList(arr);
		return lineobjs;
	}
}
