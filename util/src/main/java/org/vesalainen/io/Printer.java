/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.io;

import java.util.Locale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Printer extends Appendable
{

    void format(String format, Object... args);

    void format(Locale l, String format, Object... args);

    void print(boolean b);

    void print(char c);

    void print(int i);

    void print(long l);

    void print(float f);

    void print(double d);

    void print(char[] s);

    void print(String s);

    void print(Object obj);

    void printf(String format, Object... args);

    void printf(Locale l, String format, Object... args);

    void println();

    void println(boolean b);

    void println(char c);

    void println(int i);

    void println(long l);

    void println(float f);

    void println(double d);

    void println(char[] s);

    void println(String s);

    void println(Object obj);
    
}
