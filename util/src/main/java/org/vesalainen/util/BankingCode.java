/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.util;

import java.math.BigInteger;

/**
 * @author Timo Vesalainen
 */
public class BankingCode 
{
    public static String create(String prefix, String code)
    {
        BigInteger bbban = new BigInteger(code);
        StringBuilder sb = new StringBuilder();
        sb.append(code);
        for (char cc : prefix.toCharArray())
        {
            int k = cc - 'A' + 10;
            sb.append(k);
        }
        sb.append("00");
        BigInteger bi = new BigInteger(sb.toString());
        BigInteger remainder = bi.remainder(new BigInteger("97"));
        int cd = 98 - remainder.intValue();
        return String.format("%s%02d%d", prefix, cd, bbban);
    }
    public static void check(String str)
    {
        String code = str.replace(" ", "");
        String bbanString = code.substring(4);
        String prefix = code.substring(0,2);
        String iban = BankingCode.create(prefix, bbanString);
        if (!code.equals(iban))
        {
            throw new IllegalArgumentException(str + " illegal format");
        }
    }
    public static String format(String code)
    {
        check(code);
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<code.length();ii+=4)
        {
            if (sb.length() > 0)
            {
                sb.append(' ');
            }
            sb.append(code.substring(ii, Math.min(ii+4, code.length())));
        }
        return sb.toString();
    }
}
