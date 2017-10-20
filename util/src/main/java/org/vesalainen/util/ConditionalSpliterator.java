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
package org.vesalainen.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConditionalSpliterator<T> implements Spliterator<T>
{
    private Spliterator<T> spliterator;
    private Predicate<T> predicate;

    public ConditionalSpliterator(Spliterator<T> spliterator, Predicate<T> predicate)
    {
        this.spliterator = spliterator;
        this.predicate = predicate;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        Ref<T> ref = new Ref();
        while(spliterator.tryAdvance((t)->ref.t=t))
        {
            if (predicate.test(ref.t))
            {
                action.accept(ref.t);
                return true;
            }
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit()
    {
        return spliterator.trySplit();
    }

    @Override
    public long estimateSize()
    {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics()
    {
        return spliterator.characteristics();
    }
    
    private class Ref<T>
    {
        private T t;
    }
}
