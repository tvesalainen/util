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
package org.vesalainen.vfs.unix;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.concurrent.TimeUnit;
import org.vesalainen.vfs.arch.Header;
import static org.vesalainen.vfs.arch.Header.Type.*;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class UnixFileHeader extends Header
{
    protected UnixFileAttributeView view;
    protected UnixFileAttributes unix;
    
    protected int inode;
    protected int mode;
    protected long uid;
    protected long gid;
    protected int nlink = 1;
    protected long mtime;
    protected long atime;
    protected long ctime;
    protected long size;
    protected int devmajor;
    protected int devminor;
    protected int rdevmajor;
    protected int rdevminor;
    protected String filename;
    protected String linkname;
    protected String uname;
    protected String gname;

    public UnixFileHeader()
    {
        view = new UnixFileAttributeViewImpl(this);
        unix = view.readAttributes();
    }
    
    @Override
    public void clear()
    {
        super.clear();
        type = null;
        inode = 0;
        mode = 0;
        uid = 0;
        gid = 0;
        nlink = 1;
        mtime = 0;
        atime = 0;
        ctime = 0;
        size = 0;
        devmajor = 0;
        devminor = 0;
        rdevmajor = 0;
        rdevminor = 0;
        filename = null;
        linkname = null;
        uname = null;
        gname = null;
    }

    @Override
    public String getFilename()
    {
        return filename;
    }

    @Override
    public String getLinkname()
    {
        return linkname;
    }

    protected void fromAttributes() throws IOException
    {
        if (unix.isDirectory())
        {
            type = DIRECTORY;
        }
        if (unix.isRegularFile())
        {
            type = REGULAR;
        }
        if (unix.isSymbolicLink())
        {
            type = SYMBOLIC;
        }
        inode = unix.inode();
        mode = unix.mode();
        nlink = unix.nlink();
        mtime = unix.lastModifiedTime().to(TimeUnit.SECONDS);
        atime = unix.lastAccessTime().to(TimeUnit.SECONDS);
        ctime = unix.creationTime().to(TimeUnit.SECONDS);
        size = (int) unix.size();
        devminor = unix.device();
        UserPrincipal owner = unix.owner();
        if (owner != null)
        {
            uname = owner.getName();
        }
        GroupPrincipal group = unix.group();
        if (group != null)
        {
            gname = group.getName();
        }
    }
    protected void toAttributes() throws IOException
    {
        switch (type)
        {
            case REGULAR:
            case HARD:
                put(IS_REGULAR, true);
                break;
            case DIRECTORY:
                put(IS_DIRECTORY, true);
                break;
            case SYMBOLIC:
                put(IS_SYMBOLIC_LINK, true);
                break;
        }
        put(INODE, inode);
        view.mode(mode);
        put(NLINK, nlink);
        if (mtime != 0)
        {
            put(LAST_MODIFIED_TIME, FileTime.from(mtime, TimeUnit.SECONDS));
        }
        if (atime != 0)
        {
            put(LAST_ACCESS_TIME, FileTime.from(atime, TimeUnit.SECONDS));
        }
        if (ctime != 0)
        {
            put(CREATION_TIME, FileTime.from(ctime, TimeUnit.SECONDS));
        }
        put(SIZE, size);
        put(DEVICE, devminor);
        if (uname != null)
        {
            view.setOwner(new UnixUser(uname, uid));
        }
        if (gname != null)
        {
            view.setGroup(new UnixGroup(gname, gid));
        }
    }
}
