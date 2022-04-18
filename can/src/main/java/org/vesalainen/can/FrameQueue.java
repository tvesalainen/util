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

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nio.ReadBuffer;
import org.vesalainen.util.AbstractFunctionQueue;

/**
 * @deprecated doesn't work???
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrameQueue extends AbstractFunctionQueue implements Frame, Runnable, FrameQueueMXBean
{
    private final int canId;
    private final Frame forwarder;
    private final String name;
    private byte seq;
    
    public FrameQueue(int size, int canId, Frame forwarder, String name)
    {
        super(size);
        this.canId = canId;
        this.forwarder = forwarder;
        this.name = name;
    }
    
    @Override
    public void frame(long millis, int canId, ReadBuffer data)
    {
        try
        {
            put(seq++);
            putLong(millis);
            putInt(canId);
            int len = data.remaining();
            put((byte)len);
            for (int ii=0;ii<len;ii++)
            {
                put(data.get());
            }
            hasMoreData();
        }
        catch (InterruptedException ex)
        {
            log(Level.SEVERE, ex, "");
        }        
    }

    @Override
    public void run()
    {
        byte seq2 = 0;
        ObjectName objectName = null;
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Buffer buffer = new Buffer();
        try
        {
            objectName = new ObjectName("org.vesalainen.can:type=queue,src="+getSource()+",name="+name);
            server.registerMBean(this, objectName);
            while (true)
            {
                try
                {
                    byte b = getByte();
                    if (seq2 != b)
                    {
                        int queueLength = getQueueLength();
                        int maxQueueLength = getMaxQueueLength();
                        throw new IllegalArgumentException("out of seq");
                    }
                    seq2++;
                    long millis = getLong();
                    int canId = getInt();
                    buffer.setRemaining(getByte()&0xff);
                    forwarder.frame(millis, canId, buffer);
                    buffer.finish();
                    hasMoreRoom();
                }
                catch (InterruptedException ex)
                {
                    log(Level.SEVERE, ex, "");
                    return;
                }        
                catch (Exception ex)
                {
                    log(SEVERE, ex, "%s", ex.getMessage());
                }
            }
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            log(Level.SEVERE, ex, "");
        }
        finally
        {
            try
            {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            }
            catch (InstanceNotFoundException | MBeanRegistrationException ex)
            {
                log(Level.SEVERE, ex, "");
            }
        }
    }

    @Override
    public int getCanId()
    {
        return canId;
    }

    @Override
    public int getSource()
    {
        return PGN.sourceAddress(canId);
    }
    
    private class Buffer implements ReadBuffer
    {
        private int remaining;

        public void setRemaining(int remaining)
        {
            this.remaining = remaining;
        }
        
        public void finish()
        {
            while (remaining != 0)
            {
                get();
            }
        }
        @Override
        public int remaining()
        {
            return remaining;
        }

        @Override
        public byte get()
        {
            try
            {
                byte b = getByte();
                remaining--;
                return b;
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
}
