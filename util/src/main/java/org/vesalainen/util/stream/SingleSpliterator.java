/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class SingleSpliterator<T> implements Spliterator<T>
{
    private T item;

    public SingleSpliterator(T item)
    {
        this.item = item;
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        if (item != null)
        {
            action.accept(item);
            item = null;
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit()
    {
        return null;
    }

    @Override
    public long estimateSize()
    {
        return 1;
    }

    @Override
    public int characteristics()
    {
        return 0;
    }
    
}
