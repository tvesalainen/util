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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.vesalainen.io.AppendablePrinter;

/**
 * PrintBuffer is a class for applications using only us-ascii to use Printer interface
 * methods to put text directly to ByteBuffer.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrintBuffer extends AppendablePrinter
{
    private final ByteBuffer bb;
    
    public PrintBuffer(ByteBuffer bb)
    {
        this(bb, "\n");
    }

    public PrintBuffer(ByteBuffer bb, String endOfLine)
    {
        this.bb = bb;
        init(new AppendableBB(), endOfLine);
    }

    private class AppendableBB implements Appendable
    {

        @Override
        public Appendable append(CharSequence csq) throws IOException
        {
            return append(csq, 0, csq.length());
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException
        {
            for (int ii=start;ii<end;ii++)
            {
                append(csq.charAt(ii));
            }
            return this;
        }

        @Override
        public Appendable append(char c) throws IOException
        {
            bb.put((byte) c);
            return this;
        }
        
    }
}
