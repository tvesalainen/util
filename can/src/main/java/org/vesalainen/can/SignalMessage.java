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

import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalMessage extends AbstractMessage
{

    @Override
    protected boolean update(AbstractCanService service)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void execute(CachedScheduledThreadPool executor)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
