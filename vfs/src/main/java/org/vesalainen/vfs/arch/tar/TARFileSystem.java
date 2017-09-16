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
package org.vesalainen.vfs.arch.tar;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARFileSystem extends ArchiveFileSystem
{
    public TARFileSystem(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        super(provider, path, env, TARHeader::new, 4096, 512);
        String filename = path.getFileName().toString();
        Root root = addFileStore('/'+filename+'/', new VirtualFileStore(this, UNIX_VIEW, USER_VIEW), true);
        if (isReadOnly())
        {
            load(channel, root);
        }
    }
    
}
