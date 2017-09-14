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
package org.vesalainen.vfs.pm.rpm;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UpdateCounter implements Consumer<ByteBuffer>
{
    private Consumer<ByteBuffer> tracer;
    private int count;

    public UpdateCounter(Consumer<ByteBuffer> tracer)
    {
        this.tracer = tracer;
    }

    @Override
    public void accept(ByteBuffer t)
    {
        count += t.remaining();
        System.err.println(t+" count="+count);
        tracer.accept(t);
    }

    public int getCount()
    {
        return count;
    }
    
}
