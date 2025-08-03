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
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.vesalainen.io.AppendablePrinter;

/**
 * PrintBuffer is a class use Printer interface
 * methods to put text directly to ByteBuffer.
 * After constructing text use flush() to move it to ByteBuffer.
 * <p>If charset is one-char to one-byte (like US-ASCII) flush is not needed 
 * because text is written to ByteBuffer directly. Calling flush() doesn't do
 * anything.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrintBuffer extends AppendablePrinter
{
    private final ByteBuffer byteBuffer;
    private CharBuffer charBuffer;
    private final CharsetEncoder encoder;
    /**
     * Creates PrintBuffer using UTF-8 and NL as eol.
     * @param bb 
     */
    public PrintBuffer(ByteBuffer bb)
    {
        this(UTF_8, bb);
    }
    /**
     * Creates PrintBuffer using given charset and NL as eol.
     * @param charset
     * @param bb 
     */
    public PrintBuffer(Charset charset, ByteBuffer bb)
    {
        this(charset, bb, "\n");
    }
    /**
     * Creates PrintBuffer using given charset and given endOfLine.
     * @param charset
     * @param bb
     * @param endOfLine 
     */
    public PrintBuffer(Charset charset, ByteBuffer bb, String endOfLine)
    {
        this.encoder = charset.newEncoder();
        this.byteBuffer = bb;
        if (encoder.maxBytesPerChar() <= 1.0)
        {
            charBuffer = null;
            init(new AppendableBB(), endOfLine);
        }
        else
        {
            charBuffer = CharBuffer.allocate(bb.remaining());
            init(charBuffer, endOfLine);
        }
    }
    public void clear()
    {
        byteBuffer.clear();
        if (charBuffer != null)
        {
            charBuffer.clear();
        }
    }
    /**
     * Ensures text is written to ByteBuffer. 
     */
    public void flush()
    {
        if (charBuffer != null)
        {
            charBuffer.flip();
            encoder.encode(charBuffer, byteBuffer, true);
            charBuffer = null;
        }
    }

    public ByteBuffer getByteBuffer()
    {
        return byteBuffer;
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
            byteBuffer.put((byte) c);
            return this;
        }
        
    }
}
