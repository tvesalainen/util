/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nio.channels;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

/**
 *
 * @author tkv
 */
public class MultiProviderSelectionKey extends AbstractSelectionKey
{
    private final MultiProviderSelector selector;
    private final SelectionKey sk;

    public MultiProviderSelectionKey(MultiProviderSelector selector, SelectionKey sk)
    {
        this.selector = selector;
        this.sk = sk;
    }
    
    @Override
    public SelectableChannel channel()
    {
        return sk.channel();
    }

    @Override
    public Selector selector()
    {
        return selector;
    }

    @Override
    public int interestOps()
    {
        return sk.interestOps();
    }

    @Override
    public SelectionKey interestOps(int ops)
    {
        sk.interestOps(ops);
        return this;
    }

    @Override
    public int readyOps()
    {
        return sk.readyOps();
    }

    void doCancel()
    {
        assert(!isValid());
        sk.cancel();
        sk.selector().wakeup();
    }

    public SelectionKey getRealSelectionKey()
    {
        return sk;
    }
    
}
