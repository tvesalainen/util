/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.text;

import java.nio.charset.Charset;
import java.util.Formatter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Unicodes
{
    public static String escape(CharSequence txt)
    {
        StringBuilder sb = new StringBuilder();
        escape(txt, sb);
        return sb.toString();
    }
    public static void escape(CharSequence txt, StringBuilder out)
    {
        Formatter f = new Formatter(out);
        int length = txt.length();
        for (int ii=0;ii<length;ii++)
        {
            char cc = txt.charAt(ii);
            if (cc > 127)
            {
                f.format("\\u%04X", (int)cc);
            }
            else
            {
                f.format("%c", cc);
            }
        }
    }
    public static String toSuperScript(CharSequence txt)
    {
        StringBuilder sb = new StringBuilder();
        toSuperScript(txt, sb);
        return sb.toString();
    }
    public static void toSuperScript(CharSequence txt, StringBuilder out)
    {
        int length = txt.length();
        for (int ii=0;ii<length;ii++)
        {
            switch (txt.charAt(ii))
            {
                case '0':
                    out.append('\u2070');
                    break;
                case '1':
                    out.append('\u00B9');
                    break;
                case '2':
                    out.append('\u00B2');
                    break;
                case '3':
                    out.append('\u00B3');
                    break;
                case '4':
                    out.append('\u2074');
                    break;
                case '5':
                    out.append('\u2075');
                    break;
                case '6':
                    out.append('\u2076');
                    break;
                case '7':
                    out.append('\u2077');
                    break;
                case '8':
                    out.append('\u2078');
                    break;
                case '9':
                    out.append('\u2079');
                    break;
                case '+':
                    out.append('\u207A');
                    break;
                case '-':
                    out.append('\u207B');
                    break;
                case '=':
                    out.append('\u207C');
                    break;
                case '(':
                    out.append('\u207D');
                    break;
                case ')':
                    out.append('\u207E');
                    break;
                case 'n':
                    out.append('\u207F');
                    break;
                case 'i':
                    out.append('\u2071');
                    break;
                default:
                    throw new UnsupportedOperationException(txt.charAt(ii)+" not supported");
            }
        }
    }
    public static String toSubScript(CharSequence txt)
    {
        StringBuilder sb = new StringBuilder();
        toSubScript(txt, sb);
        return sb.toString();
    }
    public static void toSubScript(CharSequence txt, StringBuilder out)
    {
        int length = txt.length();
        for (int ii=0;ii<length;ii++)
        {
            switch (txt.charAt(ii))
            {
                case '0':
                    out.append('\u2080');
                    break;
                case '1':
                    out.append('\u2081');
                    break;
                case '2':
                    out.append('\u2082');
                    break;
                case '3':
                    out.append('\u2083');
                    break;
                case '4':
                    out.append('\u2084');
                    break;
                case '5':
                    out.append('\u2085');
                    break;
                case '6':
                    out.append('\u2086');
                    break;
                case '7':
                    out.append('\u2087');
                    break;
                case '8':
                    out.append('\u2088');
                    break;
                case '9':
                    out.append('\u2089');
                    break;
                case '+':
                    out.append('\u208A');
                    break;
                case '-':
                    out.append('\u208B');
                    break;
                case '=':
                    out.append('\u208C');
                    break;
                case '(':
                    out.append('\u208D');
                    break;
                case ')':
                    out.append('\u208E');
                    break;
                case 'a':
                    out.append('\u2090');
                    break;
                case 'e':
                    out.append('\u2091');
                    break;
                case 'o':
                    out.append('\u2092');
                    break;
                case 'x':
                    out.append('\u2093');
                    break;
                case 'h':
                    out.append('\u2095');
                    break;
                case 'k':
                    out.append('\u2096');
                    break;
                case 'l':
                    out.append('\u2097');
                    break;
                case 'm':
                    out.append('\u2098');
                    break;
                case 'n':
                    out.append('\u2099');
                    break;
                case 'p':
                    out.append('\u209A');
                    break;
                case 's':
                    out.append('\u209B');
                    break;
                case 't':
                    out.append('\u209C');
                    break;
                default:
                    throw new UnsupportedOperationException(txt.charAt(ii)+" not supported");
            }
        }
    }
}
