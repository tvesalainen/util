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

import java.util.*;

public class PankkiViivaKoodi
{
	private char _versio = '1';
	private PankkiTiliNumero _tili = null;
	private int _sum = 0;
	private ViiteNumero _viite = null;
	private Calendar _due = null;

	public PankkiViivaKoodi(
		boolean euro,
		PankkiTiliNumero tili,
		double sum,
		ViiteNumero viite,
		Calendar due
	)
	{
		if (euro)
		{
			_versio = '2';
		}
		_tili = tili;
		_sum = (int)(sum*100);
		_viite = viite;
		_due = due;
	}

	public PankkiViivaKoodi(
		char versio,
		String tili,
		double sum,
		String viite,
		Calendar due
	)
	{
		if (versio != '1' && versio != '2')
		{
			throw new IllegalArgumentException("version "+versio+ " is illegal" );
		}
		_versio = versio;
		_tili = new PankkiTiliNumero(tili);
		_sum = (int)(sum*100);
		_viite = new ViiteNumero(viite, false);
		_due = due;
	}

	public String toString()
	{
		int[] chk = new int[] { 3, 7, 1 };
		int cd = 0;
		int ii = 0;
		int jj = 0;
		String sum = null;
		char[] buf = new char[54];
		for (ii=0;ii<buf.length;ii++)
		{
			buf[ii] = '0';
		}
		ii = 0;
		buf[ii++] = _versio;
		ii = _tili.fill(ii,buf);
		sum = String.valueOf(_sum);
		sum.getChars(0, sum.length(), buf, ii+8-sum.length());
		ii += 8;
		ii = _viite.fill(ii,buf);
		if (_due != null)
		{
			sum = String.valueOf(_due.get(Calendar.YEAR));
			sum.getChars(2, sum.length(), buf, ii);
			ii += 2;
			sum = String.valueOf(_due.get(Calendar.MONTH)+1);
			sum.getChars(0, sum.length(), buf, ii+2-sum.length());
			ii += 2;
			sum = String.valueOf(_due.get(Calendar.DATE));
			sum.getChars(0, sum.length(), buf, ii+2-sum.length());
			ii += 2;
		}
		else
		{
			ii += 6;
		}
		ii += 4;
		for (jj=0;jj<53;jj++)
		{
			cd += (buf[jj]-'0')*chk[jj % 3];
		}
		buf[ii] = (char)('0'+((10 - (cd % 10)) % 10));
		return new String(buf);
	}

	public static void main(String[] args)
	{
		try
		{
			PankkiViivaKoodi pvk = new PankkiViivaKoodi(
				'1',
				"227918-22392",
				123456.78,
				"1",
				Calendar.getInstance()
			);
				
			System.out.println(pvk);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
}
