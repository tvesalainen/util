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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class FileSystemFactory
{
    private static final Map<String,Class<? extends VirtualFileSystem>> map = new HashMap<>();
    
    protected VirtualFileSystemProvider provider;
    protected Path path;
    protected Map<String, ?> env;

    public FileSystemFactory(VirtualFileSystemProvider provider, Path path, Map<String, ?> env)
    {
        this.provider = provider;
        this.path = path;
        this.env = env;
    }

    public abstract FileSystem create() throws IOException;
    
    public static final void register(String extension, Class<? extends VirtualFileSystem> fileSystem)
    {
        map.put(extension, fileSystem);
    }
    
    public static final FileSystem getInstance(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        try
        {
            String pathString = path.toString();
            String extension = null;
            for (String ext : map.keySet())
            {
                if (pathString.endsWith(ext))
                {
                    extension = ext;
                    break;
                }
            }
            if (extension == null)
            {
                throw new UnsupportedOperationException(path+" not supported");
            }
            Class<? extends VirtualFileSystem> cls = map.get(extension);
            if (cls == null)
            {
                throw new UnsupportedOperationException(extension+" not supported");
            }
            Constructor<? extends VirtualFileSystem> constructor = cls.getConstructor(VirtualFileSystemProvider.class, Path.class, Map.class);
            return constructor.newInstance(provider, path, env);
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new IOException(ex);
        }
    }
}
