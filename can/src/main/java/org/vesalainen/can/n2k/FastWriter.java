/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.n2k;

import static java.lang.Integer.min;
import java.util.function.LongConsumer;
import org.vesalainen.can.DataUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastWriter
{
    private int packetId=1;
    
    public void write(int bytes, byte[] buf, LongConsumer act)
    {
        if (bytes < 6 || bytes > 223)
        {
            throw new IllegalArgumentException("illegal fast message");
        }
        long res = 0;
        int id = (packetId++<<5)&0xff;
        res = DataUtil.set(res, 0, id);
        res = DataUtil.set(res, 1, bytes);
        for (int ii=0;ii<6;ii++)
        {
            res = DataUtil.set(res, 2+ii, buf[ii]);
        }
        act.accept(res);
        int len = (bytes-6);
        int seq = 1;
        int idx = 6;
        while (len > 0)
        {
            res = 0;
            res = DataUtil.set(res, 0, id|seq);
            for (int ii=0;ii<7;ii++)
            {
                if (len > 0)
                {
                    res = DataUtil.set(res, 1+ii, buf[idx]);
                    idx++;
                    len--;
                }
                else
                {
                    res = DataUtil.set(res, 1+ii, 0xff);
                }
            }
            seq++;
            act.accept(res);
        }
    }
}
