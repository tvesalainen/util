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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import static java.util.zip.Deflater.*;
import java.util.zip.Inflater;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.OperatingSystem;

/**
 * GZIPChannel implements reading and writing of GZIP files.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://www.ietf.org/rfc/rfc1952.txt">GZIP file format specification version 4.3</a>
 */
public class GZIPChannel implements SeekableByteChannel, ScatteringSupport, GatheringSupport
{
    public final static short MAGIC = (short)0x8b1f;
    private final static int FTEXT = 1;
    private final static int FHCRC = 2;
    private final static int FEXTRA = 4;
    private final static int FNAME = 8;
    private final static int FCOMMENT = 16;
    private SeekableByteChannel channel;
    private int bufSize = 4096;
    private int maxSkipSize;
    private String filename;
    private Set<OpenOption> options;
    private Inflater inflater;
    private Deflater deflater;
    private ByteBuffer compBuf;
    private byte[] uncompBuf;
    private ByteBuffer skipBuffer;
    private final Path path;
    private String comment;
    private CRC32 crc32 = new CRC32();
    private FileTime lastModified;
    private Lock readLock = new ReentrantLock();
    private Lock writeLock = new ReentrantLock();
    private boolean closeChannel;
    private boolean isClosed;
    /**
     * Creates GZIPChannel
     * @param path
     * @param bufSize Size of internal buffers
     * @param maxSkipSize Maximum bytes of forward skip
     * @param options
     * @throws IOException 
     */
    public GZIPChannel(Path path, int bufSize, int maxSkipSize, OpenOption... options) throws IOException
    {
        this(path, FileChannel.open(path, options), bufSize, maxSkipSize, Arrays.stream(options).collect(Collectors.toSet()));
        closeChannel = true;
    }
    /**
     * Creates GZIPChannel
     * @param path
     * @param options
     * @throws IOException 
     * @see java.nio.channels.FileChannel#open(java.nio.file.Path, java.nio.file.OpenOption...) 
     */
    public GZIPChannel(Path path, OpenOption... options) throws IOException
    {
        this(path, FileChannel.open(path, options), 4096, 0, Arrays.stream(options).collect(Collectors.toSet()));
        closeChannel = true;
    }
    /**
     * Creates GZIPChannel
     * @param path
     * @param options
     * @param bufSize Size of internal buffers
     * @param maxSkipSize Maximum bytes of forward skip
     * @param attrs
     * @throws IOException 
     */
    public GZIPChannel(Path path, Set<? extends OpenOption> options, int bufSize, int maxSkipSize, FileAttribute<?>... attrs) throws IOException
    {
        this(path, FileChannel.open(path, options, attrs), bufSize, maxSkipSize, options);
        closeChannel = true;
    }
    /**
     * Creates GZIPChannel
     * @param path
     * @param options
     * @param attrs
     * @throws IOException 
     * @see java.nio.channels.FileChannel#open(java.nio.file.Path, java.util.Set, java.nio.file.attribute.FileAttribute...)  
     */
    public GZIPChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        this(path, FileChannel.open(path, options, attrs), 4096, 0, options);
        closeChannel = true;
    }
    /**
     * Creates GZIPChannel. Channel is closed when GZIPChannel is closed.
     * @param path
     * @param channel
     * @param bufSize Size of internal buffers
     * @param maxSkipSize Maximum bytes of forward skip
     * @param options 
     */
    public GZIPChannel(Path path, SeekableByteChannel channel, int bufSize, int maxSkipSize, Set<? extends OpenOption> options)
    {
        this.path = path;
        this.channel = channel;
        this.bufSize = bufSize;
        if (bufSize < 256)
        {
            throw new IllegalArgumentException("bufSize too small");
        }
        this.maxSkipSize = maxSkipSize;
        if (maxSkipSize < 0)
        {
            throw new IllegalArgumentException("maxSkipSize negative");
        }
        this.options = new HashSet<>(options);
        if (options.contains(READ) && options.contains(WRITE))
        {
            throw new UnsupportedOperationException("both read and write not supported");
        }
        if (!options.contains(WRITE))
        {
            this.options.add(READ);
        }
        compBuf = ByteBuffer.allocate(bufSize);
        uncompBuf = new byte[bufSize];
        if (maxSkipSize > 0)
        {
            skipBuffer = ByteBuffer.allocate(maxSkipSize);
        }
        compBuf.order(LITTLE_ENDIAN);
        compBuf.flip();
    }
    /**
     * Add file entries to this GZIPChannel. Channel will be closed after this
     * call.
     * @param paths
     * @throws IOException 
     */
    public void compress(Stream<Path> paths) throws IOException
    {
        boolean first = true;
        Iterator<Path> it = paths.iterator();
        while (it.hasNext())
        {
            Path p = it.next();
            if (Files.isRegularFile(path))
            {
                setFilename(p.getFileName().toString());
                setLastModified(Files.getLastModifiedTime(p));
                if (!first)
                {
                    nextOutput();
                }
                first = false;
                OutputStream os = Channels.newOutputStream(this);
                Files.copy(p, os);
            }
            else
            {
                throw new IOException(p+" is not a regular file");
            }
        }
        close();
    }
    /**
     * Extract files from this GZIPChannel to given directory.
     * @param targetDir
     * @param options
     * @throws IOException 
     */
    public void extractAll(Path targetDir, CopyOption... options) throws IOException
    {
        if (!Files.isDirectory(path))
        {
            throw new NotDirectoryException(targetDir.toString());
        }
        ensureReading();
        do
        {
            Path p = targetDir.resolve(filename);
            InputStream is = Channels.newInputStream(this);
            Files.copy(is, p, options);
            Files.setLastModifiedTime(p, lastModified);
        } while (nextInput());
    }
    /**
     * return uncompressed position
     * @return
     * @throws IOException 
     */
    @Override
    public long position() throws IOException
    {
        if (inflater == null && deflater == null)
        {
            return 0;
        }
        if (inflater != null)
        {
            return inflater.getBytesWritten();
        }
        else
        {
            return deflater.getBytesRead();
        }
    }
    /**
     * Changes uncompressed position. Only forward direction is allowed with
     * small skips. This method is for alignment purposes mostly.
     * @param newPosition
     * @return
     * @throws IOException 
     */
    @Override
    public GZIPChannel position(long newPosition) throws IOException
    {
        int skip = (int) (newPosition - position());
        if (skip < 0)
        {
            throw new UnsupportedOperationException("backwards position not supported");
        }
        if (skip > skipBuffer.capacity())
        {
            throw new UnsupportedOperationException(skip+" skip not supported maxSkipSize="+maxSkipSize);
        }
        if (skip > 0)
        {
            if (skipBuffer == null)
            {
                throw new UnsupportedOperationException("skip not supported maxSkipSize="+maxSkipSize);
            }
            skipBuffer.clear();
            skipBuffer.limit(skip);
            if (options.contains(READ))
            {
                read(skipBuffer);
            }
            else
            {
                write(skipBuffer);
            }
        }
        return this;
    }
    /**
     * Return position
     * @return
     * @throws IOException 
     */
    @Override
    public long size() throws IOException
    {
        return position();
    }
    /**
     * Throws UnsupportedOperationException
     * @param size
     * @return
     * @throws IOException 
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        if (!dst.hasRemaining())
        {
            return 0;
        }
        ensureReading();
        readLock();
        try
        {
            int count = 0;
            while (dst.hasRemaining())
            {
                int length = Math.min(uncompBuf.length, dst.remaining());
                int rc;
                while ((rc = inflater.inflate(uncompBuf, 0, length)) == 0)
                {
                    if (inflater.needsDictionary())
                    {
                        throw new IOException("inflater needs dictionary");
                    }
                    if (inflater.finished())
                    {
                        if (count > 0)
                        {
                            return count;
                        }
                        readTrailer();
                        return -1;
                    }
                    if (inflater.needsInput())
                    {
                        fillInflater();
                    }
                }
                crc32.update(uncompBuf, 0, rc);
                dst.put(uncompBuf, 0, rc);
                count += rc;
            }
            return count;
        }
        catch (DataFormatException ex)
        {
            throw new IOException(ex);
        }
        finally
        {
            readUnlock();
        }
    }
    private void fillInflater() throws IOException
    {
        if (!compBuf.hasRemaining())
        {
            compBuf.clear();
            channel.read(compBuf);
            compBuf.flip();
        }
        inflater.setInput(compBuf.array(), compBuf.position(), compBuf.remaining());
        compBuf.position(compBuf.limit());
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        ensureWriting();
        int count = src.remaining();
        while (src.hasRemaining())
        {
            if (deflater.needsInput())
            {
                int len = Math.min(src.remaining(), uncompBuf.length);
                src.get(uncompBuf, 0, len);
                deflater.setInput(uncompBuf, 0, len);
                crc32.update(uncompBuf, 0, len);
            }
            compBuf.clear();
            int rc = deflater.deflate(compBuf.array());
            if (rc > 0)
            {
                compBuf.limit(rc);
                ChannelHelper.writeAll(channel, compBuf);
            }
        }
        return count;
    }
    /**
     * Returns true if not closed
     * @return 
     */
    @Override
    public boolean isOpen()
    {
        return !isClosed;
    }
    /**
     * Closes channel. Underlying channel is closed only if it was opened
     * by one of the constructors.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException
    {
        if (!isClosed)
        {
            flush();
            if (closeChannel)
            {
                channel.close();
            }
            isClosed = true;
        }
    }
    /**
     * Finishes
     * @throws IOException 
     */
    public void flush() throws IOException
    {
        if (deflater != null)
        {
            writeTrailer();
        }
        else
        {
            if (inflater != null)
            {
                ByteBuffer bb = ByteBuffer.allocate(4096);
                int rc = read(bb);
                while (rc != -1)
                {
                    bb.clear();
                    rc = read(bb);
                }
            }
        }
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
        filename = null;
        comment = null;
        compBuf.compact();
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
        lastModified = FileTime.from(mtime, TimeUnit.SECONDS);
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
            ByteBuffer duplicate = compBuf.duplicate();
            duplicate.flip();
            CRC32 crc32 = new CRC32();
            crc32.update(duplicate);
            int value = (int) (crc32.getValue() & 0xffff);
            int crc16 = (compBuf.getShort() & 0xffff);
            if (crc16 != value)
            {
                throw new IOException("CRC16 mismatch");
            }
        }
    }

    private void writeHeader() throws IOException
    {
        compBuf.clear();
        compBuf.putShort(MAGIC);
        compBuf.put((byte)8);
        byte flags = FHCRC;
        if (filename != null)
        {
            flags |= FNAME;
        }
        if (comment != null)
        {
            flags |= FCOMMENT;
        }
        if (lastModified == null)
        {
            lastModified = FileTime.from(Instant.now());
        }
        compBuf.put(flags);
        compBuf.putInt((int) lastModified.to(TimeUnit.SECONDS));
        lastModified = null;
        compBuf.put((byte)2);   // compressor used maximum compression, slowest algorithm
        switch (OperatingSystem.getOperatingSystem())
        {
            case Linux:
                compBuf.put((byte)3);   // unix
                break;
            case Windows:
                compBuf.put((byte)11);  // NTFS filesystem (NT)
                break;
            default:
                compBuf.put((byte)255);  // unknown
                break;
        }
        if (filename != null)
        {
            writeString(compBuf, filename);
        }
        if (comment != null)
        {
            writeString(compBuf, comment);
        }
        filename = null;
        comment = null;
        ByteBuffer duplicate = compBuf.duplicate();
        duplicate.flip();
        CRC32 crc = new CRC32();
        crc.update(duplicate);
        compBuf.putShort((short) (crc.getValue() & 0xffff));
        compBuf.flip();
        channel.write(compBuf);
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

    private void readTrailer() throws IOException
    {
        compBuf.position(compBuf.position() - inflater.getRemaining());
        compBuf.compact();
        channel.read(compBuf);
        compBuf.flip();
        System.err.println(HexDump.remainingToHex(compBuf));
        int crc = compBuf.getInt();
        int value = (int) crc32.getValue();
        if (crc != value)
        {
            throw new IOException("CRC32 mismatch");
        }
        int isize = compBuf.getInt();
        if (isize != (inflater.getBytesWritten() & 0xffffffff))
        {
            throw new IOException("Size mismatch");
        }
        channel.position(channel.position()-compBuf.remaining());
    }
    /**
     * After read returns -1 call nextInput to advance to nextInput file.
     * @return true if nextInput file available.
     * @throws IOException 
     */
    public boolean nextInput() throws IOException
    {
        if (!options.contains(READ) && !inflater.finished())
        {
            throw new IllegalStateException("should be called after read returns -1");
        }
        if (!compBuf.hasRemaining())
        {
            compBuf.clear();
            int rc = channel.read(compBuf);
            if (rc == -1)
            {
                return false;
            }
            compBuf.flip();
        }
        readHeader();
        inflater.reset();
        crc32.reset();
        return true;
    }
    public void nextOutput() throws IOException
    {
        writeTrailer();
        writeHeader();
    }
    /**
     * Returns original filename without path or null if name doesn't exist.
     * Usable only in when reading.
     * @return 
     */
    public String getFilename() throws IOException
    {
        ensureReading();
        return filename;
    }
    /**
     * Returns comment or null.
     * Usable only in when reading.
     * @return 
     */
    public String getComment() throws IOException
    {
        ensureReading();
        return comment;
    }
    /**
     * Sets original filename for compressed file.
     * Set before first write.
     * @param filename 
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
    /**
     * Sets comment for compressed file.
     * Set before first write.
     * @param comment 
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    /**
     * Sets last modified time for compressed file.
     * Set before first write.
     * @param lastModified 
     */
    public void setLastModified(FileTime lastModified)
    {
        this.lastModified = lastModified;
    }

    private void writeString(ByteBuffer bb, String text)
    {
        bb.put(text.getBytes(ISO_8859_1));
        bb.put((byte)0);
    }

    private void writeTrailer() throws IOException
    {
        deflater.finish();
        while (!deflater.finished())
        {
            compBuf.clear();
            int rc = deflater.deflate(compBuf.array());
            compBuf.limit(rc);
            ChannelHelper.writeAll(channel, compBuf);
        }
        compBuf.clear();
        compBuf.putInt((int) (crc32.getValue() & 0xffffffffL));
        compBuf.putInt((int) (deflater.getBytesRead() & 0xffffffffL));
        compBuf.flip();
        channel.write(compBuf);
        crc32.reset();
        deflater.reset();
    }

    @Override
    public void readLock()
    {
        readLock.lock();
    }

    @Override
    public void readUnlock()
    {
        readLock.lock();
    }

    @Override
    public void writeLock()
    {
        writeLock.lock();
    }

    @Override
    public void writeUnlock()
    {
        writeLock.unlock();
    }

}
