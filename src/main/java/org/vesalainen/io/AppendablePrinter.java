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
package org.vesalainen.io;

import java.io.IOException;
import java.util.Locale;

/**
 * AppendablePrinter enhances Appendable interface to cover PrintStream and
 * PrintReader methods.
 * 
 * <p>This class throws IOException wrapped in IllegalArgumentException.
 * @author tkv
 */
public class AppendablePrinter implements Appendable
{
    protected Appendable out;
    protected String eol;
    /**
     * Creates new AppendablePrinter. End-of-line is \\n
     * @param out 
     */
    public AppendablePrinter(Appendable out)
    {
        this(out, "\n");
    }
    /**
     * Creates new AppendablePrinter.
     * @param out
     * @param endOfLine 
     */
    public AppendablePrinter(Appendable out, String endOfLine)
    {
        this.out = out;
        this.eol = endOfLine;
    }
    @Override
    public Appendable append(CharSequence csq)
    {
        try
        {
            return out.append(csq);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end)
    {
        try
        {
            return out.append(csq, start, end);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Appendable append(char c)
    {
        try
        {
            return out.append(c);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void format(String format, Object... args)
    {
        try
        {
            out.append(String.format(format, args));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void format(Locale l, String format, Object... args)
    {
        try
        {
            out.append(String.format(l, format, args));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(boolean b)
    {
        try
        {
            out.append(Boolean.toString(b));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(char c)
    {
        try
        {
            out.append(c);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(int i)
    {
        try
        {
            out.append(Integer.toString(i));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(long l)
    {
        try
        {
            out.append(Long.toString(l));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(float f)
    {
        try
        {
            out.append(Float.toString(f));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(double d)
    {
        try
        {
            out.append(Double.toString(d));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(char[] s)
    {
            try
            {
         for (char cc : s)
        {
               out.append(cc);
        }
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
    }

    public void print(String s)
    {
        try
        {
            out.append(s);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void print(Object obj)
    {
        try
        {
            out.append(obj.toString());
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void printf(String format, Object... args)
    {
        try
        {
            out.append(String.format(format, args));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void printf(Locale l, String format, Object... args)
    {
        try
        {
            out.append(String.format(l, format, args));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println()
    {
        try
        {
            out.append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(boolean b)
    {
        try
        {
            out.append(Boolean.toString(b)).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(char c)
    {
        try
        {
            out.append(c).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(int i)
    {
        try
        {
            out.append(Integer.toString(i)).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(long l)
    {
        try
        {
            out.append(Long.toString(l)).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(float f)
    {
        try
        {
            out.append(Float.toString(f)).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(double d)
    {
        try
        {
            out.append(Double.toString(d)).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(char[] s)
    {
        try
        {
        for (char cc : s)
        {
            out.append(cc);
        }
        out.append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(String s)
    {
        try
        {
            out.append(s).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void println(Object obj)
    {
        try
        {
            out.append(obj.toString()).append(eol);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

}
