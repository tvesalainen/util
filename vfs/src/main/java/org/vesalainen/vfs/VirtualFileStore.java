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
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.vesalainen.vfs.VirtualFile.Type;
import static org.vesalainen.vfs.VirtualFile.Type.*;

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

    protected VirtualFileStore(VirtualFileSystem fileSystem, String... views)
    {
        this.fileSystem = fileSystem;
        supportedFileAttributeViews.add("basic");
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
    VirtualFile create(Path path, Type type, ByteBuffer content, FileAttribute<?>... attrs) throws IOException
    {
        VirtualFile file = new VirtualFile(this, type, content, supportedFileAttributeViews, attrs);
        files.put(path, file);
        return file;
    }
    VirtualFile remove(Path path) throws DirectoryNotEmptyException
    {
        VirtualFile file = files.get(path);
        if (file.getType() == DIRECTORY)
        {
            Path ck = files.higherKey(path);
            if (ck != null)
            {
                if (ck.startsWith(path))
                {
                    throw new DirectoryNotEmptyException(path.toString());
                }
            }
        }
        return files.remove(path);
    }
    void link(Path link, Path existing)
    {
        files.put(link, files.get(existing));
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
        return files.values().stream().mapToLong((f)->f.size).sum();
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
    
}
