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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import static org.vesalainen.vfs.VirtualFileSystemProvider.URI;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class VirtualFileSystems
{
    /**
     * Returns reference to default virtual file system
     * @return 
     */
    public static final FileSystem getDefault()
    {
        return FileSystems.getFileSystem(URI);  
    }
    /**
     * Constructs a new FileSystem to access the contents of a file as a file system.
     * @param path
     * @param env
     * @return
     * @throws IOException 
     */
    public static final FileSystem newFileSystem(Path path, Map<String,?> env) throws IOException
    {
        return getDefault().provider().newFileSystem(path, env);
    }
}
