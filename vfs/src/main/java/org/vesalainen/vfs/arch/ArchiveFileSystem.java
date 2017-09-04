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
package org.vesalainen.vfs.arch;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystem;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class ArchiveFileSystem extends VirtualFileSystem
{
    public static final String OPEN_OPTIONS = "openOptions";
    public static final String FILE_ATTRIBUTES = "fileAttributes";
    
    protected Path path;
    protected Map<String, ?> env;
    protected Supplier<Header> headerSupplier;
    protected SeekableByteChannel channel;
    protected boolean readOnly;
    protected String filename;

    protected ArchiveFileSystem(
            VirtualFileSystemProvider provider, 
            Path path, 
            Map<String, ?> env,
            Supplier<Header> headerSupplier,
            String... views
    ) throws IOException
    {
        super(provider);
        this.path = path;
        this.env = env;
        this.headerSupplier = headerSupplier;
        Set<? extends OpenOption> opts = (Set<? extends OpenOption>) env.get(OPEN_OPTIONS);
        if (opts == null)
        {
            if (Files.exists(path) && Files.size(path) > 0)
            {
                opts = EnumSet.of(READ);
            }
            else
            {
                opts = EnumSet.of(WRITE, CREATE);
            }
        }
        readOnly = !opts.contains(WRITE);
        FileAttribute<?>[] attrs = (FileAttribute<?>[]) env.get(FILE_ATTRIBUTES);
        if (attrs == null)
        {
            attrs = new FileAttribute<?>[0];
        }
        filename = path.getFileName().toString();
        if (path.toString().endsWith(".gz"))
        {
            GZIPChannel gzipChannel = new GZIPChannel(path, opts);
            channel = gzipChannel;
            if (readOnly)
            {
                filename = gzipChannel.getFilename();
                filename = filename == null ? "" : filename;
            }
            else
            {
                gzipChannel.setFilename(filename);
            }
        }
        else
        {
            channel = FileChannel.open(path, opts, attrs);
        }
        addFileStore('/'+filename, new VirtualFileStore(this, views), true);
        if (isReadOnly())
        {
            load();
        }
    }

    @Override
    public final boolean isReadOnly()
    {
        return readOnly;
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        if (!readOnly)
        {
            store(getDefaultRoot());
        }
        channel.close();
    }
    
    public final void load() throws IOException
    {
        Header header = headerSupplier.get();
        header.load(channel);
        while (!header.isEof())
        {
            String fn = header.getFilename();
            if (fn.startsWith(getSeparator()))
            {
                fn = fn.substring(1);
            }
            Path pth = getPath(fn).normalize();
            FileAttribute<?>[] fileAttributes = header.fileAttributes();
            Long size = (long) header.get(SIZE);
            if (size == null)
            {
                throw new IllegalArgumentException("size missing in "+pth);
            }
            Path parent = pth.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }
            String linkname = header.getLinkname();
            Path linkTarget = null;
            if (linkname != null)
            {
                linkTarget = getPath(linkname);
            }
            switch (header.getType())
            {
                case DIRECTORY:
                    Files.createDirectory(pth, fileAttributes);
                    break;
                case SYMBOLIC:
                    Files.createSymbolicLink(pth, linkTarget, fileAttributes);
                    break;
                case HARD:
                    Files.createLink(pth, linkTarget);
                    break;
                case REGULAR:
                    if (size > 0)
                    {
                        try (FileChannel ch = FileChannel.open(pth, EnumSet.of(WRITE, CREATE), fileAttributes))
                        {
                            long pos = 0;
                            while (size > 0)
                            {
                                long count = ch.transferFrom(channel, pos, size);
                                pos += count;
                                size -= count;
                            }
                        }
                    }
                    break;
            }
            header.clear();
            header.load(channel);
        }
    }
    public final void store(Root root) throws IOException
    {
        enumerateInodes();
        Set<String> supportedFileAttributeViews = supportedFileAttributeViews();
        Header header = headerSupplier.get();
        Set<String> topViews = FileAttributeName.topViews(supportedFileAttributeViews);
        Map<String, Object> all = new HashMap<>();
        Files.walk(root).filter((p->p!=root)).forEach((p)->
        {
            try
            {
                Path r = root.relativize(p);
                all.clear();
                for (String view : topViews)
                {
                    Map<String, Object> attrs = Files.readAttributes(r, view+":*");
                    for (Entry<String, Object> entry : attrs.entrySet())
                    {
                        all.put(FileAttributeName.getInstance(entry.getKey()).toString(), entry.getValue());
                    }
                }
                header.clear();
                header.store(channel, r.toString(), all);
                Long size = (Long) all.get(SIZE);
                if (size == null)
                {
                    throw new IllegalArgumentException("size missing in "+r);
                }
                if (size > 0)
                {
                    try (FileChannel ch = FileChannel.open(r, READ))
                    {
                        ch.transferTo(0, size, channel);
                    }
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
        header.storeEof(channel);
    }
    
}
