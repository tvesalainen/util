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
package org.vesalainen.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A class that contains a set of matchers.
 * @author tkv
 * @param <T> Attachment type
 */
public class OrMatcher<T> implements Matcher, Iterable<Matcher>
{
    private final Set<Matcher> matchers = new HashSet<>();
    private final Set<Matcher> active = new HashSet<>();
    private final MapList<Matcher,T> map = new HashMapList<>();
    private Matcher lastMatched;

    public OrMatcher()
    {
    }
    /**
     * Add matcher
     * @param matcher 
     */
    public void add(Matcher matcher)
    {
        matchers.add(matcher);
        active.add(matcher);
    }
    /**
     * Add matcher with attachment. If matcher exist only attachment is stored.
     * @param matcher
     * @param attachment 
     */
    public void add(Matcher matcher, T attachment)
    {
        add(matcher);
        map.add(matcher, attachment);
    }
    public void add(Matcher matcher, Collection<T> attachments)
    {
        add(matcher);
        for (T attachment : attachments)
        {
            map.add(matcher, attachment);
        }
    }
    /**
     * If one matches returns Match. If all return Error returns error.
     * Otherwise returns Ok.
     * @param cc
     * @return 
     */
    @Override
    public Status match(int cc)
    {
        int highest = -1;
        Iterator<Matcher> iterator = active.iterator();
        while (iterator.hasNext())
        {
            Matcher matcher = iterator.next();
            Status s = matcher.match(cc);
            highest = Math.max(highest, s.ordinal());
            switch (s)
            {
                case Match:
                    lastMatched = matcher;
                    clear();
                    return s;
                case WillMatch:
                    lastMatched = matcher;
                    return s;
                case Error:
                    iterator.remove();
                    break;
            }
        }
        if (active.isEmpty())
        {
            clear();
            return Status.Error;
        }
        return Status.values()[highest];
    }
    
    /**
     * Returns attachment of last matched matcher.
     * @return 
     */
    public List<T> getLastMatched()
    {
        return map.get(lastMatched);
    }

    @Override
    public void clear()
    {
        for (Matcher matcher : matchers)
        {
            matcher.clear();
        }
        active.addAll(matchers);
    }

    @Override
    public Iterator<Matcher> iterator()
    {
        return matchers.iterator();
    }
    public boolean isEmpty()
    {
        return matchers.isEmpty();
    }

    @Override
    public Object getMatched()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
