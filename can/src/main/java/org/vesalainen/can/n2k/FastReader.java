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
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastReader extends JavaLogging
{
    private String name;
    private byte[] buf;
    private byte packetId;
    private int byteMax;
    private int byteCount;
    private int fastMessageFails;
    private StringBuilder sb = new StringBuilder();

    public FastReader(String name, byte[] buf)
    {
        super(FastReader.class);
        this.name = name;
        this.buf = buf;
    }
    
    public boolean update(long time, int canId, int dataLength, long data)
    {
        int sa = PGN.sourceAddress(canId);
        try
        {
            int header;
            int b = DataUtil.get(data, 0);
            int seq = b & 0x1f;
            byte id = (byte) (b & 0xe0);
            if (id != packetId)
            {
                if (byteMax != byteCount)
                {
                    info("fast message failure %d: %x->%x: seq=%d max=%d cnt=%d \n%s", sa, packetId, id, seq, byteMax, byteCount, sb.toString());
                    fastMessageFails++;
                }
                packetId = id;
                byteMax = buf.length;
                byteCount = 0;
                sb.setLength(0);
            }
            DataUtil.append(data, dataLength, sb);
            sb.append('\n');
            if (seq == 0)
            {   // new message
                byteMax = DataUtil.get(data, 1);
                header = 2;
                fine("new fast %s: %d %x max=%d buf=%d", name, sa, id, byteMax, buf.length);
            }
            else
            {
                header = 1;
            }
            int off = seq == 0 ? 0 : 6 + (seq-1)*7;
            int remaining = min(dataLength-header, byteMax - off);
            byteCount += remaining;
            fine("%d %x: seq=%d max=%d cnt=%d rem=%d", sa, id, seq, byteMax, byteCount, remaining);
            try
            {
                DataUtil.fromLong(data, header, buf, off, remaining);
                fine("%s", HexUtil.toString(buf));
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                log(SEVERE, ex, "seq=%d off=%d max=%d cnt=%d rem=%d", seq, off, byteMax, byteCount, remaining);
            }
            return byteMax == byteCount;
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "update %s", name);
        }
        return false;
    }

    public int getFastMessageFails()
    {
        return fastMessageFails;
    }

    public int getByteMax()
    {
        return byteMax;
    }
    
}
