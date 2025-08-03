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
package org.vesalainen.nio.channels.vc;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.concurrent.Callable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class Echo implements Callable<Void>
{
    
    private ByteChannel rc;
    private ByteBuffer bb = ByteBuffer.allocate(256);

    public Echo(ByteChannel rc)
    {
        this.rc = rc;
    }

    @Override
    public Void call() throws Exception
    {
        while (true)
        {
            bb.clear();
            rc.read(bb);
            bb.flip();
            rc.write(bb);
        }
    }
    
}
