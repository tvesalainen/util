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
package org.vesalainen.code;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.vesalainen.util.TimeToLiveSet;

/**
 * A Set of fresh properties. Fresh property set method has been called not
 * longer that timeout ago.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeToLivePropertySetter extends AbstractPropertySetter
{
    private TimeToLiveSet<String> ttls;

    public TimeToLivePropertySetter(long timeout, TimeUnit unit)
    {
        ttls = new TimeToLiveSet(timeout, unit);
    }
    
    @Override
    public void setProperty(String property, Object arg)
    {
        ttls.refresh(property);
    }

    public boolean isAlive(String property)
    {
        return ttls.isAlive(property);
    }

    public Stream<String> stream()
    {
        return ttls.stream();
    }

    public Spliterator<String> spliterator()
    {
        return ttls.spliterator();
    }

    public Iterator<String> iterator()
    {
        return ttls.iterator();
    }

    public void forEach(Consumer<? super String> action)
    {
        ttls.forEach(action);
    }
    
}
