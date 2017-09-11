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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Inode
{
    private long devminor;
    private long inode;

    public Inode(long devminor, long inode)
    {
        this.devminor = devminor;
        this.inode = inode;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 73 * hash + (int) (this.devminor ^ (this.devminor >>> 32));
        hash = 73 * hash + (int) (this.inode ^ (this.inode >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Inode other = (Inode) obj;
        if (this.devminor != other.devminor)
        {
            return false;
        }
        if (this.inode != other.inode)
        {
            return false;
        }
        return true;
    }
    
}
