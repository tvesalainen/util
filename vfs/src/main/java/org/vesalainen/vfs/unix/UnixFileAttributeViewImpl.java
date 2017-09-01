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

import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.*;
import java.util.EnumSet;
import java.util.Set;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.PosixFileAttributeViewImpl;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnixFileAttributeViewImpl extends PosixFileAttributeViewImpl implements UnixFileAttributeView
{

    public UnixFileAttributeViewImpl(FileAttributeAccess access)
    {
        super(UNIX_VIEW, access);
    }

    @Override
    public int device()
    {
        return (int) get(DEVICE);
    }

    @Override
    public int inode()
    {
        return (int) get(INODE);
    }

    @Override
    public boolean setUserId()
    {
        return (boolean) get(SETUID);
    }

    @Override
    public void setUserId(boolean setUserId)
    {
        put(SETUID, setUserId);
    }

    @Override
    public boolean setGroupId()
    {
        return (boolean) get(SETGID);
    }

    @Override
    public void setGroupId(boolean setGroupId)
    {
        put(SETGID, setGroupId);
    }

    @Override
    public boolean stickyBit()
    {
        return (boolean) get(STICKY);
    }

    @Override
    public void stickyBit(boolean stickyBit)
    {
        put(STICKY, stickyBit);
    }

    @Override
    public short mode()
    {
        return getMode();
    }

    @Override
    public void mode(int mode)
    {
        fromMode((short) ((short) mode & 07777));
    }

    @Override
    public String modeString()
    {
        return PosixHelp.toString(mode());
    }

    @Override
    public void mode(String mode)
    {
        mode(PosixHelp.getMode(mode));
    }
    
    public short getMode()
    {
        Set<PosixFilePermission> perms = (Set<PosixFilePermission>) get(PERMISSIONS);
        if (perms == null)
        {
            perms = EnumSet.noneOf(PosixFilePermission.class);
        }
        short mode = 0;
        if ((Boolean)get(IS_REGULAR))
        {
            mode |= 0100000;
        }
        else
        {
            if ((Boolean)get(IS_DIRECTORY))
            {
                mode |= 0040000;
            }
            else
            {
                if ((Boolean)get(IS_SYMBOLIC_LINK))
                {
                    mode |= 0120000;
                }
                else
                {
                    throw new UnsupportedOperationException("unsupported file type");
                }
            }
        }
        for (PosixFilePermission p : perms)
        {
            switch (p)
            {
                case OWNER_READ:
                    mode |= 0400;
                    break;
                case OWNER_WRITE:
                    mode |= 0200;
                    break;
                case OWNER_EXECUTE:
                    mode |= 0100;
                    break;
                case GROUP_READ:
                    mode |= 040;
                    break;
                case GROUP_WRITE:
                    mode |= 020;
                    break;
                case GROUP_EXECUTE:
                    mode |= 010;
                    break;
                case OTHERS_READ:
                    mode |= 04;
                    break;
                case OTHERS_WRITE:
                    mode |= 02;
                    break;
                case OTHERS_EXECUTE:
                    mode |= 01;
                    break;
            }
        }
        if ((Boolean)get(SETUID))
        {
            mode |= 04000;
        }
        if ((Boolean)get(SETGID))
        {
            mode |= 02000;
        }
        if ((Boolean)get(STICKY))
        {
            mode |= 01000;
        }
        return mode;
    }
    private void fromMode(short mode)
    {
        EnumSet perms = EnumSet.noneOf(PosixFilePermission.class);
        put(PERMISSIONS, perms);
        switch (mode & 0170000)
        {
            case 0120000:   // i
                put(IS_SYMBOLIC_LINK, true);
                break;
            case 0100000:   // -
                put(IS_REGULAR, true);
                break;
            case 0040000:   // d
                put(IS_DIRECTORY, true);
                break;
            default:
                break;
        }
        // owner
        if ((mode & 00400)==00400)
        {
            perms.add(OWNER_READ);
        }
        if ((mode & 00200)==00200)
        {
            perms.add(OWNER_WRITE);
        }
        if ((mode & 00100)==00100)
        {
            perms.add(OWNER_EXECUTE);
            if ((mode & 0004000)==0004000)
            {
                put(SETUID, true);
            }
        }
        // group
        if ((mode & 0040)==0040)
        {
            perms.add(GROUP_READ);
        }
        if ((mode & 0020)==0020)
        {
            perms.add(GROUP_WRITE);
        }
        if ((mode & 0010)==0010)
        {
            perms.add(GROUP_EXECUTE);
            if ((mode & 0002000)==0002000)
            {
                put(SETGID, true);
            }
        }
        // others
        if ((mode & 004)==004)
        {
            perms.add(OTHERS_READ);
        }
        if ((mode & 002)==002)
        {
            perms.add(OTHERS_WRITE);
        }
        if ((mode & 001)==001)
        {
            perms.add(OTHERS_EXECUTE);
            if ((mode & 0001000)==0001000)
            {
                put(STICKY, true);
            }
        }
        else
        {
            if ((mode & 0001000)==0001000)
            {
                put(STICKY, true);
            }
        }
    }
}
