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
package org.vesalainen.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;

/**
 * DynamicByteBuffer helps create dynamic ByteBuffer. ByteBuffer is actually
 * MappedByteBuffer and it is backed by temporary file.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DynamicByteBuffer
{
    /**
     * Creates dynamically growing ByteBuffer upto maxSize. ByteBuffer is 
     * created by mapping a temporary file, MapMode is READ_WRITE.
     * @param maxSize
     * @return
     * @throws IOException 
     * @see java.nio.MappedByteBuffer
     */
    public static MappedByteBuffer create(int maxSize) throws IOException
    {
        return create(MapMode.READ_WRITE, maxSize);
    }
    /**
     * Creates dynamically growing ByteBuffer upto maxSize. ByteBuffer is 
     * created by mapping a temporary file
     * @param mapMode
     * @param maxSize
     * @return
     * @throws IOException 
     */
    public static MappedByteBuffer create(MapMode mapMode, int maxSize) throws IOException
    {
        Path tmp = Files.createTempFile("dynBB", "tmp");
        try (FileChannel fc = FileChannel.open(tmp, READ, WRITE, CREATE, DELETE_ON_CLOSE))
        {
            return fc.map(MapMode.READ_WRITE, 0, maxSize);
        }
    }
    /**
     * Creates dynamically growing ByteBuffer upto maxSize for named file.
     * @param path
     * @param maxSize
     * @return
     * @throws IOException 
     */
    public static ByteBuffer create(Path path, int maxSize) throws IOException
    {
        try (FileChannel fc = FileChannel.open(path, READ, WRITE, CREATE))
        {
            return fc.map(FileChannel.MapMode.READ_WRITE, 0, maxSize);
        }
    }
}
