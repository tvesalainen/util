/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.channels.sel;

import java.nio.channels.SelectionKey;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SelKey
{
    public enum Op {OP_READ, OP_ACCEPT};
    private SelChannel channel;
    private ChannelSelector selector;
    private Object attachment;
    private Op interestOps;

    SelKey(SelChannel channel, ChannelSelector selector, Object attachment, Op interestOps)
    {
        this.channel = channel;
        this.selector = selector;
        this.attachment = attachment;
        this.interestOps = interestOps;
    }
    
    public void attach(Object attachment)
    {
        this.attachment = attachment;
    }
    
    public Object attachment()
    {
        return attachment;
    }
    
    public SelChannel channel()
    {
        return channel;
    }

    public ChannelSelector selector()
    {
        return selector;
    }

    public boolean isValid()
    {
        return channel != null && channel.isOpen() && selector.isOpen();
    }

    public void cancel()
    {
        channel.unregister(selector);
        channel = null;
    }

    public Op interestOps()
    {
        return interestOps;
    }

    public SelectionKey interestOps(Op ops)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Op readyOps()
    {
        return interestOps;
    }

    public final boolean isReadable()
    {
        return interestOps == Op.OP_READ;
    }
    public final boolean isWritable()
    {
        return false;
    }
    public final boolean isConnectable()
    {
        return false;
    }
    public final boolean isAcceptable()
    {
        return interestOps == Op.OP_ACCEPT;
    }
}
