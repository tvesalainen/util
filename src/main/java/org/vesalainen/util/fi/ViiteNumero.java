/*
 * Copyright (C) 2010 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.util.fi;

public class ViiteNumero
{
	private char[] _chk = new char[] { '7', '1', '3', '7', '1', '3', '7', '1', '3', '7', '1', '3', '7', '1', '3', '7', '1', '3', '7' };
	private char[] _buf = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };

	public ViiteNumero(String str, boolean check)
	{
		int ii;
		if (check)
		{
			if (str.length() > (_buf.length))
			{
				throw new IllegalArgumentException( str+" is too long" );
			}
		}
		else
		{
			if (str.length() > (_buf.length-1))
			{
				throw new IllegalArgumentException( str+" is too long" );
			}
		}
		for (ii=0;ii<str.length();ii++)
		{
			if (str.charAt(ii) < '0' || str.charAt(ii) > '9')
			{
				throw new IllegalArgumentException( str+" contains non numeric characters" );
			}
		}
		if (check)
		{
			str.getChars(0, str.length(), _buf, (_buf.length)-str.length() );
		}
		else
		{
			str.getChars(0, str.length(), _buf, (_buf.length-1)-str.length() );
		}
		calcCheckDigit(check);
	}

	public ViiteNumero(long n, boolean check)
	{
		String str = String.valueOf(n);
		if (check)
		{
			str.getChars(0, str.length(), _buf, (_buf.length)-str.length() );
		}
		else
		{
			str.getChars(0, str.length(), _buf, (_buf.length-1)-str.length() );
		}
		calcCheckDigit(check);
	}

	private void calcCheckDigit(boolean check)
	{
		int ii;
		int chk = 0;
		for (ii=0;ii<(_buf.length-1);ii++)
		{
			chk += (_chk[ii]-'0')*(_buf[ii]-'0');
		}
		if (check)
		{
			if (_buf[(_buf.length-1)] != (char)('0'+(char)(((10-(chk % 10)) % 10))))
			{
				throw new IllegalArgumentException( this+" has wrong check digit" );
			}
		}
		else
		{
			_buf[(_buf.length-1)] = (char)('0'+(char)(((10-(chk % 10)) % 10)));
		}
	}

	public String toPankkiViivaKoodiString()
	{
		int ii;
		StringBuffer sb = new StringBuffer();
		for (ii=0;ii<_buf.length;ii++)
		{
			sb.append(_buf[ii]);
		}
		return sb.toString();
	}

	public String toString()
	{
		int ii;
		boolean startzero = true;
		StringBuffer sb = new StringBuffer();
		for (ii=0;ii<_buf.length;ii++)
		{
			if (startzero)
			{
				if (_buf[ii] != '0')
				{
					sb.append(_buf[ii]);
					startzero = false;
				}
			}
			else
			{
				if ((ii % 5) == 0)
				{
					sb.append(' ');
				}
				sb.append(_buf[ii]);
			}
		}
		return sb.toString();
	}

	public int fill(int start, char[] buf)
	{
		int ii;
		for (ii=0;ii<_buf.length;ii++)
		{
			buf[start+ii] = _buf[ii];
		}
		return start+_buf.length;
	}

	public static void main(String[] args)
	{
		try
		{
			ViiteNumero vn = new ViiteNumero(args[0], args.length > 1);
			System.out.println(vn);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
}

