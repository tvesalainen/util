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
package org.vesalainen.vfs.attributes;

import java.nio.ByteBuffer;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DigestFileAttributeViewImpl extends FileAttributeViewImpl implements DigestFileAttributeView
{

    public DigestFileAttributeViewImpl(FileAttributeAccess access)
    {
        super(DIGEST_VIEW, access);
    }

    @Override
    public ByteBuffer content()
    {
        return (ByteBuffer) get(CONTENT);
    }

    @Override
    public byte[] crc32()
    {
        return (byte[]) get(CRC32);
    }

    @Override
    public byte[] md5()
    {
        return (byte[]) get(MD5);
    }

    @Override
    public byte[] sha1()
    {
        return (byte[]) get(SHA1);
    }

    @Override
    public byte[] sha256()
    {
        return (byte[]) get(SHA256);
    }
    
}
