/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.modbus;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TcpUtil
{
    public static final void readModbusMessage(ReadableByteChannel channel, ByteBuffer in) throws IOException
    {
        while (in.position() < 7)
        {
            int rc = channel.read(in);
            if (rc == -1)
            {
                throw new EOFException();
            }
        }
        short dataLength = in.getShort(4);
        int length = dataLength + 6;
        while (in.position() < length)
        {
            int rc = channel.read(in);
            if (rc == -1)
            {
                throw new EOFException();
            }
        }
    }
}
