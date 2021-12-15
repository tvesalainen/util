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
import org.vesalainen.util.AbstractFunctionQueue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrameQueue extends AbstractFunctionQueue implements Frame, Runnable, FrameQueueMXBean
{
    private final int canId;
    private final Frame forwarder;
    private final String name;
    
    public FrameQueue(int size, int canId, Frame forwarder, String name)
    {
        super(size);
        this.canId = canId;
        this.forwarder = forwarder;
        this.name = name;
    }
    
    @Override
    public void frame(long millis, int canId, int dataLength, long data)
    {
        try
        {
            putLong(millis);
            putInt(canId);
            put((byte) dataLength);
            putLong(data);
            hasMoreData();
        }
        catch (InterruptedException ex)
        {
            log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    public void run()
    {
        ObjectName objectName = null;
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try
        {
            objectName = new ObjectName("org.vesalainen.can:type=queue,src="+getSource()+",name="+name);
            server.registerMBean(this, objectName);
            while (true)
            {
                try
                {
                    long millis = getLong();
                    int canId = getInt();
                    int dataLength = getByte();
                    long data = getLong();
                    hasMoreRoom();
                    forwarder.frame(millis, canId, dataLength, data);
                }
                catch (InterruptedException ex)
                {
                    log(Level.SEVERE, null, ex);
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
            log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            }
            catch (InstanceNotFoundException | MBeanRegistrationException ex)
            {
                log(Level.SEVERE, null, ex);
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
    
}
