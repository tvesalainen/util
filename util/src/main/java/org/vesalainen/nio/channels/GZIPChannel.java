/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.channels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import static java.util.zip.Deflater.*;
import java.util.zip.Inflater;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GZIPChannel implements ByteChannel
{
    private static final int BUF_SIZE = 4096;
    public final static int MAGIC = 0x1f8b;
    private final static int FTEXT = 1;
    private final static int FHCRC = 2;
    private final static int FEXTRA = 4;
    private final static int FNAME = 8;
    private final static int FCOMMENT = 16;
    private FileChannel channel;
    private boolean readOnly;
    private String filename;
    private Set<OpenOption> options;
    private Inflater inflater;
    private Deflater deflater;
    private ByteBuffer compBuf = ByteBuffer.allocate(BUF_SIZE);
    private byte[] uncompBuf = new byte[BUF_SIZE];
    private final Path path;
    private String comment;

    public GZIPChannel(Path path, OpenOption... options) throws IOException
    {
        this(path, FileChannel.open(path, options), Arrays.stream(options).collect(Collectors.toSet()));
    }

    public GZIPChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        this(path, FileChannel.open(path, options, attrs), options);
    }

    public GZIPChannel(Path path, FileChannel channel, Set<? extends OpenOption> options)
    {
        this.path = path;
        this.channel = channel;
        this.options = new HashSet<>(options);
        if (options.contains(READ) && options.contains(WRITE))
        {
            throw new UnsupportedOperationException("both read and write not supported");
        }
        if (!options.contains(WRITE))
        {
            this.options.add(READ);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        ensureReading();
        try
        {
            int length = Math.min(uncompBuf.length, dst.remaining());
            int rc;
            while ((rc = inflater.inflate(uncompBuf, 0, length)) == 0)
            {
                if (inflater.finished() || inflater.needsDictionary())
                {
                    return -1;
                }
                if (inflater.needsInput())
                {
                    fill();
                }
            }
            dst.put(uncompBuf, 0, rc);
            return rc;
        }
        catch (DataFormatException ex)
        {
            throw new IOException(ex);
        }
    }
    private void fill() throws IOException
    {
        if (!compBuf.hasRemaining())
        {
            compBuf.clear();
            channel.read(compBuf);
            compBuf.flip();
        }
        inflater.setInput(compBuf.array(), compBuf.position(), compBuf.limit());
        compBuf.position(compBuf.limit());
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        ensureWriting();
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
    }

    private void ensureReading() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (!options.contains(READ))
        {
            throw new NonReadableChannelException();
        }
        if (inflater == null)
        {
            readHeader();
            inflater = new Inflater(true);
        }
    }

    private void ensureWriting() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        if (!options.contains(WRITE))
        {
            throw new NonWritableChannelException();
        }
        if (deflater == null)
        {
            writeHeader();
            deflater = new Deflater(DEFAULT_COMPRESSION, true);
        }
    }

    private void readHeader() throws IOException
    {
        compBuf.order(BIG_ENDIAN);
        channel.read(compBuf);
        compBuf.flip();
        short magic = compBuf.getShort();
        if (magic != MAGIC)
        {
            throw new IllegalArgumentException(path+" not gzip file");
        }
        byte cm = compBuf.get();
        if (cm != 8)
        {
            throw new UnsupportedOperationException(cm+" compression method not supported");
        }
        byte flag = compBuf.get();
        int mtime = compBuf.getInt();
        byte xfl = compBuf.get();
        byte os = compBuf.get();
        if ((flag & FEXTRA) != 0)
        {
            short xlen = compBuf.getShort();
            compBuf.position(compBuf.position() + xlen);
        }
        if ((flag & FNAME) != 0)
        {
            filename = readString(compBuf);
        }
        if ((flag & FCOMMENT) != 0)
        {
            comment = readString(compBuf);
        }
        if ((flag & FHCRC) != 0)
        {
            short crc = compBuf.getShort();
        }
    }

    private void writeHeader()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String readString(ByteBuffer bb)
    {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        int cc = bb.get() & 0xff;
        while (cc != 0)
        {
            bais.write(cc);
            cc = bb.get() & 0xff;
        }
        return new String(bais.toByteArray(), ISO_8859_1);
    }

}
