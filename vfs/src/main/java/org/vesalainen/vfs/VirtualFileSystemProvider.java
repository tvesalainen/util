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
package org.vesalainen.vfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystemProvider extends FileSystemProvider
{
    static final String SCHEME = "org.vesalainen.vfs";

    private VirtualFile getFile(Path path)
    {
        VirtualFileSystem vfs = (VirtualFileSystem) path.getFileSystem();
        return vfs.getFileStore(path).get(path);
    }
    private VirtualFile createFile(Path path, FileAttribute<?>... attrs) throws IOException
    {
        VirtualFileSystem vfs = (VirtualFileSystem) path.getFileSystem();
        return vfs.getFileStore(path).create(path, attrs);
    }
    private VirtualFile deleteFile(Path path)
    {
        VirtualFileSystem vfs = (VirtualFileSystem) path.getFileSystem();
        return vfs.getFileStore(path).remove(path);
    }
    @Override
    public String getScheme()
    {
        return SCHEME;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileSystem getFileSystem(URI uri)
    {
        if (SCHEME.equalsIgnoreCase(uri.getScheme()))
        {
            VirtualFileSystem vfs = new VirtualFileSystem(this);
            vfs.addDefaultFileStore(new Root(vfs, "/"), new VirtualFileStore());
            return vfs;
        }
        throw new IllegalArgumentException(uri+" not matching");
    }

    @Override
    public Path getPath(URI uri)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        VirtualFile file = getFile(path);
        Set<OpenOption> opts = new HashSet<>(options);
        if (!opts.contains(READ) && !opts.contains(WRITE) && !opts.contains(APPEND))
        {
            opts.add(READ);
        }
        if (opts.contains(APPEND))
        {
            if (opts.contains(READ) || opts.contains(TRUNCATE_EXISTING))
            {
                throw new IllegalArgumentException("READ or TRUNCATE_EXISTING with APPEND");
            }
            opts.add(WRITE);
        }
        if (options.contains(CREATE_NEW))
        {
            if (file != null)
            {
                throw new FileAlreadyExistsException(path.toString());
            }
            file = createFile(path, attrs);
        }
        if (options.contains(CREATE) && file == null)
        {
            file = createFile(path, attrs);
        }
        if (file == null)
        {
            throw new FileNotFoundException(path.toString());
        }
        return new VFileChannel(path, file, opts);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Path path) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isHidden(Path path) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException
    {
        if (getFile(path) == null)
        {
            throw new NoSuchFileException(path.toString());
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
    {
        VirtualFile file = getFile(path);
        if (file == null)
        {
            throw new FileNotFoundException(path.toString());
        }
        if (BasicFileAttributes.class.equals(type))
        {
            return (A) file.getBasicAttrs();
        }
        throw new UnsupportedOperationException(type+" not supported");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
