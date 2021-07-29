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

import java.nio.ByteBuffer;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractMessage extends JavaLogging
{
    public static final NullMessage NULL_MESSAGE = new NullMessage();
    
    private int currentBytes;

    protected AbstractMessage()
    {
        super(AbstractMessage.class);
    }

    public int getCurrentBytes()
    {
        return currentBytes;
    }

    public void setCurrentBytes(int currentBytes)
    {
        this.currentBytes = currentBytes;
    }
    
    public int getCurrentBits()
    {
        return getCurrentBytes()*8;
    }
    
    public abstract int getMaxBytes();
    
    public int getMaxBits()
    {
        return getMaxBytes()*8;
    }
    /**
     * Updates CanProcessor data. Returns true if needs to execute.
     * @param service
     * @return 
     */
    protected abstract boolean update(AbstractCanService service);
    protected abstract void execute(CachedScheduledThreadPool executor);
    
    public static class NullMessage extends AbstractMessage
    {

        private NullMessage()
        {
        }

        @Override
        public int getMaxBytes()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean update(AbstractCanService service)
        {
            ByteBuffer frame = service.getFrame();
            int canId = frame.getInt(0);
            //warning("Unknown %d:\n%s", canId, HexDump.startToHex(frame));
            return false;
        }

        @Override
        protected void execute(CachedScheduledThreadPool executor)
        {
            throw new UnsupportedOperationException("Not supported.");
        }

    }
}
