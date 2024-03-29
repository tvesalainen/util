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
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;
import static java.util.logging.Level.*;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.nio.ReadBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SingleMessage extends AbstractMessage
{

    protected LongSupplier millisSupplier = System::currentTimeMillis;

    public SingleMessage(Executor executor, MessageClass messageClass, int canId, int len, String comment)
    {
        super(executor, messageClass, canId);
        this.buf = new byte[len];
        this.name = comment;
    }

    @Override
    public int getMaxBytes()
    {
        return 8;
    }

    @Override
    public void setCurrentBytes(int currentBytes)
    {
        super.setCurrentBytes(currentBytes);
    }

    @Override
    protected ObjectName getObjectName() throws MalformedObjectNameException
    {
        return new ObjectName("org.vesalainen.can:type=std,canId="+canId);
    }
    
    @Override
    protected boolean update(long time, int canId, ReadBuffer data)
    {
        try
        {
            setCurrentBytes(data.remaining());
            data.get(buf);
            if (action != null || jmxAction != null)
            {
                millisSupplier = ()->time;
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "execute %s", name);
        }
        return false;
    }

    @Override
    protected long getMillis()
    {
        return millisSupplier.getAsLong();
    }

}
