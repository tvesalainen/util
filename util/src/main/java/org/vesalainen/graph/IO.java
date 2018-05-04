/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.graph;

import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IO
{
    public static final int readFully(Reader reader, byte[] buffer) throws IOException
    {
        return readFully(reader, buffer, 0, buffer.length);
    }
    public static final int readFully(Reader reader, byte[] buffer, int offset, int length) throws IOException
    {
        int count = 0;
        while (length > 0)
        {
            int rc = reader.read(buffer, offset, length);
            if (rc == -1)
            {
                if (count > 0)
                {
                    return count;
                }
                else
                {
                    return -1;
                }
            }
            length -= rc;
            offset += rc;
            count += rc;
        }
        return count;
    }
    @FunctionalInterface
    public interface Reader
    {
        int read(byte[] buffer, int offset, int length) throws IOException;
    }
}
