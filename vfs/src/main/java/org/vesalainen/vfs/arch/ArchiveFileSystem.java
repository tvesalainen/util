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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.vesalainen.nio.channels.FilterSeekableByteChannel;
import org.vesalainen.util.ArrayHelp;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystem;
import org.vesalainen.vfs.VirtualFileSystemProvider;
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
        Set<? extends OpenOption> opts = (Set<? extends OpenOption>) env.get(OPEN_OPTIONS);
        if (opts == null)
        {
            if (Files.exists(path))
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
        channel = FileChannel.open(path, opts, attrs);
        if (path.toString().endsWith(".gz"))
        {
            channel = new FilterSeekableByteChannel(channel, GZIPInputStream::new, GZIPOutputStream::new);
        }
        addFileStore("/", new VirtualFileStore(this, views), true);
        load();
    }

    @Override
    public boolean isReadOnly()
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
            store();
        }
        channel.close();
    }
    
    public final void load() throws IOException
    {
        Header header = headerSupplier.get();
        header.load(channel);
        while (!header.isEof())
        {
            Path pth = getPath(header.filename());
            FileAttribute<?>[] fileAttributes = header.fileAttributes();
            Long size = (long) header.get(SIZE);
            if (size == null)
            {
                throw new IllegalArgumentException("size missing in "+pth);
            }
            try (FileChannel ch = FileChannel.open(pth, EnumSet.of(WRITE, CREATE), fileAttributes))
            {
                ch.transferFrom(channel, 0, size);
            }
            header.clear();
            header.load(channel);
        }
    }
    public final void store() throws IOException
    {
        Set<String> supportedFileAttributeViews = supportedFileAttributeViews();
        Header header = headerSupplier.get();
        Root root = getDefaultRoot();
        Files.walk(root).forEach((p)->
        {
            try
            {
                Map<String, Object> all = new HashMap<>();
                for (String view : supportedFileAttributeViews)
                {
                    Map<String, Object> attrs = Files.readAttributes(p, view+":*");
                    for (Entry<String, Object> entry : attrs.entrySet())
                    {
                        all.put(view+':'+entry.getKey(), entry.getValue());
                    }
                }
                header.clear();
                header.store(channel, all);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }
    
}
