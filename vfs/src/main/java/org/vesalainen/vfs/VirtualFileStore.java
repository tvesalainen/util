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

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;
import org.vesalainen.vfs.VirtualFile.Type;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileStore extends FileStore
{
    protected VirtualFileSystem fileSystem;
    protected ConcurrentNavigableMap<Path,VirtualFile> files = new ConcurrentSkipListMap<>();
    protected Map<String,Object> storeAttributes = new HashMap<>();
    protected Set<String> supportedFileAttributeViews = new HashSet<>();
    protected int blockSize = 4096;

    public VirtualFileStore(VirtualFileSystem fileSystem, String... views)
    {
        this.fileSystem = fileSystem;
        supportedFileAttributeViews.addAll(FileAttributeName.impliedSet(views));
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    public VirtualFileSystem getFileSystem()
    {
        return fileSystem;
    }
    public VirtualFileSystemProvider provider()
    {
        return (VirtualFileSystemProvider) fileSystem.provider();
    }
    public Set<String> supportedFileAttributeViews()
    {
        return supportedFileAttributeViews;
    }

    VirtualFile get(Path path)
    {
        return files.get(path);
    }
    VirtualFile create(Path path, Type type, FileAttribute<?>... attrs) throws IOException
    {
        VirtualFile file = new VirtualFile(this, type, supportedFileAttributeViews, attrs);
        files.put(path, file);
        return file;
    }
    VirtualFile remove(Path path) throws DirectoryNotEmptyException
    {
        if (isNonEmptyDirectory(path))
        {
            throw new DirectoryNotEmptyException(path.toString());
        }
        return files.remove(path);
    }
    boolean isNonEmptyDirectory(Path path)
    {
        VirtualFile file = files.get(path);
        if (file.isDirectory())
        {
            Path ck = files.higherKey(path);
            if (ck != null)
            {
                if (ck.startsWith(path))
                {
                    return true;
                }
            }
        }
        return false;
    }
    void link(Path link, Path existing)
    {
        files.put(link, files.get(existing));
    }
    void add(Path path, VirtualFile file)
    {
        files.put(path, file);
    }
    DirectoryStream<Path> directoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
    {
        return new DirectoryStreamImpl(dir, filter);
    }
    @Override
    public String name()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String type()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReadOnly()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getTotalSpace() throws IOException
    {
        return files.values().stream().mapToLong((f)->f.getSize()).sum();
    }

    @Override
    public long getUsableSpace() throws IOException
    {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException
    {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsFileAttributeView(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getAttribute(String attribute) throws IOException
    {
        return storeAttributes.get(attribute);
    }

    void enumerateInodes(int dev)
    {
        MapSet<VirtualFile,Path> map = new HashMapSet<>();
        files.forEach((p,f)->map.add(f, p));
        AtomicInteger inode = new AtomicInteger(0);
        map.forEach((f,p)->
        {
            f.setAttribute(DEVICE, dev);
            f.setAttribute(INODE, inode.getAndIncrement());
            f.setAttribute(NLINK, p.size());
        });
    }

    public class DirectoryStreamImpl implements DirectoryStream<Path>
    {
        private Path dir;
        private final Filter<? super Path> filter;

        public DirectoryStreamImpl(Path dir, DirectoryStream.Filter<? super Path> filter)
        {
            this.dir = dir;
            this.filter = filter;
        }
        
        @Override
        public Iterator<Path> iterator()
        {
            if (dir.isAbsolute() && dir.getNameCount() == 0)
            {
                return new PathIter(dir, filter, files.keySet().iterator());
            }
            else
            {
                return new PathIter(dir, filter, files.keySet().tailSet(dir).iterator());
            }
        }

        @Override
        public void close() throws IOException
        {
        }
        
    }
    private class PathIter implements Iterator<Path>
    {
        private Path dir;
        private final DirectoryStream.Filter<? super Path> filter;
        private Iterator<Path> iterator;
        private Path next;

        public PathIter(Path dir, DirectoryStream.Filter<? super Path> filter, Iterator<Path> iterator)
        {
            this.dir = dir;
            this.filter = filter;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext()
        {
            try
            {
                while (iterator.hasNext())
                {
                    next = iterator.next();
                    if (next.startsWith(dir))
                    {
                        if (!dir.equals(next) && dir.equals(next.getParent()) && filter.accept(next))
                        {
                            return true;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                return false;
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Path next()
        {
            return next;
        }
        
    }
}
