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

import java.util.concurrent.Executor;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NullMessage extends SingleMessage
{
    
    public NullMessage(Executor executor, int canId)
    {
        super(executor, null, canId, 8, "unknown frame");
    }

    @Override
    protected void detach()
    {
    }

    @Override
    protected void attach()
    {
    }

    @Override
    public int getMaxBytes()
    {
        return 0;
    }

    @Override
    protected ObjectName getObjectName() throws MalformedObjectNameException
    {
        return new ObjectName("org.vesalainen.can:type=Unknown,canId=" + canId);
    }

    @Override
    protected boolean update(AbstractCanService service)
    {
        super.update(service);
        if (emitter != null)
        {
            emitter.sendNotification2(()->NOTIF_HEX_TYPE, ()->HexUtil.toString(buf), this::getMillis);
        }
        return false;
    }

    @Override
    protected void execute()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
