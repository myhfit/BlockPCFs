package bp.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YAMLUtil
{
	protected final static NumberFormat s_nfi = new DecimalFormat("#");
	protected final static NumberFormat s_nff = new DecimalFormat("0.######");
	protected final static NumberFormat s_nfd = new DecimalFormat("#");
	protected final static DateTimeFormatter s_ndf = DateTimeFormatter.ISO_DATE_TIME;

	@SuppressWarnings("unchecked")
	public final static <T> T decode(String text)
	{
		if (text == null)
			return null;
		Object obj = null;
		String[] lines = (text.split("\n"));
		int len = lines.length;
		Map<String, Object> root = new HashMap<String, Object>();
		root = (Map<String, Object>) innerDecodeObject(lines, 0, len, -1, 0).obj;
		obj = root;
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	private final static RL innerDecodeObject(String[] lines, int line, int len, int level, int testlevel)
	{
		RL r = new RL();
		Object robj = null;
		int setlevel = -1;
		int nextline;
		for (; line < len; line = nextline)
		{
			nextline = line + 1;
			String linestr = lines[line];
			int ll;
			if (testlevel == 0)
				ll = getLevel(linestr);
			else
			{
				ll = testlevel;
				testlevel = 0;
			}
			if (ll == -1)
				continue;
			if (ll > level && (setlevel == -1 || ll == setlevel))
			{
				if (setlevel == -1)
					setlevel = ll;
				Object[] lp = parseLine(linestr, ll);
				if (lp == null)
					continue;
				char c = (Character) lp[1];
				String k = (String) lp[2];
				Object v = lp[3];
				if (robj == null)
				{
					if (c == '-')
						robj = new ArrayList<>();
					else
						robj = new HashMap<>();
				}
				if (v == null)
				{
					RL r2 = innerDecodeObject(lines, line + 1, len, ll, 0);
					v = r2.obj;
					if (c == '-')
					{
						((List<Object>) robj).add(v);
					}
					else
					{
						((Map<String, Object>) robj).put(k, v);
					}
					nextline = r2.line;
					if (r2.line == -1)
					{
						line = nextline;
						break;
					}
					if (r2.testlevel != -1 && r2.testlevel < setlevel)
					{
						line = nextline;
						r.testlevel = r2.testlevel;
						break;
					}
					else
					{
						testlevel = r2.testlevel;
					}
				}
				else
				{
					if (c == '-')
					{
						((List<Object>) robj).add(v);
					}
					else
					{
						((Map<String, Object>) robj).put(k, v);
					}
				}
			}
			else
			{
				r.testlevel = ll;
				break;
			}
		}
		r.line = line >= len ? -1 : line;
		r.obj = robj;
		return r;
	}

	private final static class RL
	{
		public int line;
		public Object obj;
		public int testlevel;
	}

	private final static Object parseValue(String v)
	{
		if (v == null || v.length() == 0)
			return null;
		int l = v.length();
		if (l > 1)
		{
			char c0 = v.charAt(0);
			char c1 = v.charAt(l - 1);
			if (c0 == '\'' && c1 == '\'')
			{
				return escape(v.substring(1, l - 1));
			}
			else if (c0 == '"' && c1 == '"')
			{
				return v.substring(1, l - 1);
			}
		}
		else
		{
			boolean flag = false;
			boolean nnflag = false;
			boolean fflag = false;
			boolean dflag = true;
			for (int i = 0; i < l; i++)
			{
				char c = v.charAt(i);
				if (c == '#')
				{
					v = v.substring(0, i).trim();
					flag = true;
					break;
				}
				else
				{
					if (c >= '0' && c <= '9')
					{

					}
					else
					{
						if (c == '.' || c == 'e' || c == '+')
						{
							fflag = true;
						}
						else if (c != ' ')
						{
							nnflag = true;
						}
						if (!(c == '-' || c == 'T' || c == ':' || c == '+'))
						{
							dflag = false;
						}
					}
				}
			}
			if (!flag)
				v = v.trim();
			if (!nnflag)
			{
				if (fflag)
				{
					try
					{
						return Double.parseDouble(v);
					}
					catch (NumberFormatException e)
					{
					}
				}
				try
				{
					return Long.parseLong(v);
				}
				catch (NumberFormatException e)
				{
				}
			}
			if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false"))
				return Boolean.parseBoolean(v);
			if (v.equals("~"))
				return null;
			if (dflag)
			{
				try
				{
					return Date.from(LocalDateTime.parse(v).atZone(ZoneId.systemDefault()).toInstant());
				}
				catch (DateTimeParseException e)
				{
				}
			}
		}
		return v;
	}

	private final static int getLevel(String line)
	{
		if (line.length() == 0)
			return -1;
		if (line.charAt(line.length() - 1) == '\r')
			line = line.substring(0, line.length() - 1);
		if (line.length() == 0)
			return -1;
		int l = line.length();
		char c = 0;
		for (int i = 0; i < l; i++)
		{
			c = line.charAt(i);
			if (c != ' ')
			{
				return i;
			}
		}
		return -1;
	}

	private final static Object[] parseLine(String line, int level)
	{
		if (line.length() == 0)
			return null;
		if (line.charAt(line.length() - 1) == '\r')
			line = line.substring(0, line.length() - 1);
		if (line.length() == 0)
			return null;
		Object[] rc = new Object[4];
		int l = line.length();
		char c = line.charAt(level);
		rc[0] = level;
		rc[1] = c;
		if (c == '-')
		{
			if (l > level + 1)
			{
				rc[3] = parseValue(line.substring(level + 2));
			}
		}
		else if (c == '?')
		{

		}
		else if (c == '#')
		{
			return null;
		}
		else
		{
			int vi = line.indexOf(":", level);
			rc[2] = line.substring(level, vi);
			if (l > vi + 1)
			{
				rc[3] = parseValue(line.substring(vi + 2));
			}
		}
		return rc;
	}

	private final static int seekStrEnd(String text, int pos, int len, StringBuilder sb)
	{
		int i = pos;
		char lastc = 0;
		int transs = -1;
		for (; i < len; i++)
		{
			char c = text.charAt(i);
			if (c == '\\')
			{
				transs = i;
				if (i < len - 1)
				{
					char nc = text.charAt(i + 1);
					switch (nc)
					{
						case '"':
						{
							sb.append("\"");
							i++;
							break;
						}
						case '\\':
						{
							sb.append("\\");
							i++;
							break;
						}
						case '/':
						{
							sb.append("/");
							i++;
							break;
						}
						case 'b':
						{
							sb.append("\b");
							i++;
							break;
						}
						case 'f':
						{
							sb.append("\f");
							i++;
							break;
						}
						case 'n':
						{
							sb.append("\n");
							i++;
							break;
						}
						case 'r':
						{
							sb.append("\r");
							i++;
							break;
						}
						case 't':
						{
							sb.append("\t");
							i++;
							break;
						}
						case 'u':
						{
							String ncnum = text.substring(i + 2, i + 6);
							char realc = (char) Integer.parseInt(ncnum, 16);
							i += 5;
							sb.append(realc);
							break;
						}
					}
					lastc = nc;
				}
			}
			else if (c == '"' && (lastc != '\\' || transs != i - 1))
			{
				break;
			}
			else
			{
				sb.append(c);
				lastc = text.charAt(i);
			}
		}
		return i;
	}

	public final static String encode(Object obj)
	{
		StringBuilder sb = new StringBuilder();
		writeObject(obj, sb, 0);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public final static void writeObject(Object obj, StringBuilder sb, int level)
	{
		boolean nl = level > 0 || sb.length() > 0;
		if (obj instanceof Map)
		{
			Map<String, ?> mobj = (Map<String, ?>) obj;
			for (String k : mobj.keySet())
			{
				if (nl)
					sb.append("\n");
				else
					nl = true;
				for (int i = 0; i < level; i++)
					sb.append(" ");
				sb.append(k);
				sb.append(":");
				writeValue(mobj.get(k), sb, level);
			}
		}
		else if (obj instanceof List)
		{
			List<?> lobj = (List<?>) obj;
			for (Object li : lobj)
			{
				if (nl)
					sb.append("\n");
				else
					nl = true;
				for (int i = 0; i < level; i++)
					sb.append(" ");
				sb.append("- ");
				writeValue(li, sb, level);
			}
		}
	}

	public final static void writeValue(Object v, StringBuilder sb, int level)
	{
		if (v == null)
		{
			sb.append(" ~");
			return;
		}
		if (v instanceof Map || v instanceof List)
		{
			writeObject(v, sb, level + 2);
		}
		else
		{
			sb.append(" ");
			if (v instanceof String)
			{
				q((String) v, sb);
			}
			else if (v instanceof Number)
			{
				if (v instanceof Integer)
				{
					sb.append(Integer.toString((Integer) v));
				}
				else if (v instanceof Short)
				{
					sb.append(Short.toString((Short) v));
				}
				else if (v instanceof Byte)
				{
					sb.append(Byte.toString((Byte) v));
				}
				else if (v instanceof Long)
				{
					sb.append(Long.toString((Long) v));
				}
				else if (v instanceof Float)
				{
					sb.append(Float.toString((Float) v));
				}
				else if (v instanceof Double)
				{
					sb.append(Double.toString((Double) v));
				}
				else
				{
					sb.append('"');
					sb.append(v.toString());
					sb.append('"');
				}
			}
			else if (v instanceof Date)
			{
				sb.append(s_ndf.format(((Date) v).toInstant()));
			}
			else
			{
				sb.append(v.toString());
			}
		}
	}

	private static void q(String text, StringBuilder sb)
	{
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == '\\' || c == '"')
			{
				sb.append('\\');
				sb.append(c);
			}
			else if (c == '\b')
				sb.append("\\b");
			else if (c == '\f')
				sb.append("\\f");
			else if (c == '\n')
				sb.append("\\n");
			else if (c == '\r')
				sb.append("\\r");
			else if (c == '\t')
				sb.append("\\t");
			else
				sb.append(c);
		}
	}

	public final static String escape(String text)
	{
		if (text == null)
			return null;
		StringBuilder sb = new StringBuilder();
		seekStrEnd(text, 0, text.length(), sb);
		return sb.toString();
	}
}