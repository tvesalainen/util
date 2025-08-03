/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An InputStream for ByteBuffers remaining bytes.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RemainingInputStream extends InputStream
{
    private ByteBuffer bb;

    public RemainingInputStream(ByteBuffer bb)
    {
        this.bb = bb;
    }
    
    @Override
    public int read() throws IOException
    {
        if (bb.hasRemaining())
        {
            return bb.get();
        }
        else
        {
            return -1;
        }
    }
    
}
