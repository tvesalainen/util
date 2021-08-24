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

import static java.lang.Integer.min;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import static java.util.logging.Level.WARNING;
import org.vesalainen.can.dbc.MessageClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastMessage extends PgnMessage
{
    private final static int MAX_FAST_SIZE = 223*8; // bits
    private byte packetId;
    private int packetSeq;
    private int byteCount;
    
    public FastMessage(Executor executor, MessageClass messageClass, int canId, int len, String comment)
    {
        super(executor, messageClass, canId, len, comment);
    }

    @Override
    public int getMaxBytes()
    {
        return 223;
    }

    @Override
    protected boolean update(AbstractCanService service)
    {
        try
        {
            ByteBuffer frame = service.getFrame();
            byte b = frame.get(0);
            byte id = (byte) (b & 0xe0);
            int seq = b & 0x1f;
            if (seq == 0)
            {   // new message
                packetId = id;
                packetSeq = 0;
                byteCount = frame.get(1) & 0xff;
                setCurrentBytes(byteCount);
                frame.position(2);
                info("new fast %s: %d cnt=%d buf=%d", name, id, byteCount, buf.length);
            }
            else
            {
                if (id != packetId || seq != packetSeq + 1)
                {   // out of order
                    if (packetId != -1)
                    {
                        info("reject fast %d %d", id, seq);
                        packetId = -1;
                        packetSeq = -1;
                    }
                    return false;
                }
                frame.position(1);
            }
            int off = seq == 0 ? 0 : 6 + (seq-1)*7;
            int remaining = min(frame.remaining(), byteCount);
            byteCount -= remaining;
            info("seq=%d cnt=%d rem=%d", seq, byteCount, remaining);
            frame.get(buf, off, remaining);
            packetSeq = seq;
            return byteCount == 0;
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "update %s", name);
        }
        return false;
    }
    
}
