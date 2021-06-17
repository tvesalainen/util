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
package org.vesalainen.can;

import java.nio.ByteOrder;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalMessage extends AbstractMessage
{
    private byte[] buf;
    private List<Runnable> signals = new ArrayList<>();

    public SignalMessage(int len)
    {
        buf = new byte[len];
    }
    
    public void addSignal(int bitOffset, int size, ByteOrder bo, double factor, double offset, DoubleConsumer act)
    {
        final LongSupplier ls1;
        final LongSupplier ls2;
        final DoubleSupplier ds3;
        final DoubleSupplier ds4;
        if (bitOffset % 8 == 0 && size % 8 == 0)
        {
            ls1 = ()->getLong8(bitOffset, size, buf);
        }
        else
        {
            ls1 = ()->getLong1(bitOffset, size, buf);
        }
        if (bo == LITTLE_ENDIAN)
        {
            ls2 = ()->changeEndian(ls1.getAsLong());
        }
        else
        {
            ls2 = ls1;
        }
        if (factor != 1.0)
        {
            ds3 = ()->factor*ls2.getAsLong();
        }
        else
        {
            ds3 = ()->ls2.getAsLong();
        }
        if (offset != 0.0)
        {
            ds4 = ()->ds3.getAsDouble()+offset;
        }
        else
        {
            ds4 = ds3;
        }
        signals.add(()->act.accept(ds4.getAsDouble()));
    }
    @Override
    protected boolean update(AbstractCanService service)
    {
        service.readData(buf, 0);
        return true;
    }

    @Override
    protected void execute(CachedScheduledThreadPool executor)
    {
        for (Runnable func : signals)
        {
            func.run();
        }
    }
    
    public static long getLong8(int offset, int length, byte... buf)
    {
        int o = offset / 8;
        int l = length / 8;
        long res = 0;
        for (int ii=0;ii<l;ii++)
        {
            res = (res<<8) + (buf[ii+o] & 0xff);
        }
        return res;
    }
    public static long getLong1(int offset, int length, byte... buf)
    {
        long res = 0;
        for (int ii=0;ii<length;ii++)
        {
            int jj = ii+offset;
            res = (res<<1) + (buf[jj/8]>>(7-jj%8) & 0x1);
        }
        return res;
    }
    /**
     * Change Big Endian to Little Endian and vice versa.
     * @param l
     * @return 
     */
    public static long changeEndian(long l)
    {
        long res = 0;
        while (l != 0)
        {
            res = (res<<8) + (l & 0xff);
            l>>=8;
        }
        return res;
    }
}
