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
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.vesalainen.nio.channels.FilterSeekableByteChannel;
import org.vesalainen.vfs.VirtualFileSystemProvider;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Archive
{
    public static final String OPEN_OPTIONS = "openOptions";
    public static final String FILE_ATTRIBUTES = "fileAttributes";
    
    protected VirtualFileSystemProvider provider;
    protected Path path;
    protected Map<String, ?> env;
    protected SeekableByteChannel channel;

    public Archive(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        this.provider = provider;
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
    }
    public void load()
    {
    }
    
}
