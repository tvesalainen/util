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
package org.vesalainen.pm.rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.vesalainen.nio.FilterByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileRecord
{
    CPIO cpio;
    String filename;
    ByteBuffer content;

    FileRecord(CPIO cpio, String filename, ByteBuffer content)
    {
        this.cpio = cpio;
        this.filename = filename;
        this.content = content;
    }

    FileRecord(FilterByteBuffer bb) throws IOException
    {
        cpio = new CPIO(bb);
        filename = bb.getString();
        bb.alignInput(4);
        content = ByteBuffer.allocate(cpio.filesize);
        bb.get(content);
        content.flip();
        bb.alignInput(4);
    }

    void save(FilterByteBuffer bb) throws IOException
    {
        cpio.save(bb);
        bb.putString(filename);
        bb.alignOutput(4);
        bb.put(content.duplicate());
        bb.alignOutput(4);
    }
}
