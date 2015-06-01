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
import java.util.List;
import java.util.Set;

/**
 * A class that contains a set of matchers.
 * @author tkv
 * @param <T> Attachment type
 */
public class OrMatcher<T> implements Matcher
{
    private final Set<Matcher> matchers = new HashSet<>();
    private final MapList<Matcher,T> map = new HashMapList<>();
    private List<T> lastMatched;

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
    }
    /**
     * Add matcher with attachment. If matcher exist only attachment is stored.
     * @param matcher
     * @param attachment 
     */
    public void add(Matcher matcher, T attachment)
    {
        matchers.add(matcher);
        map.add(matcher, attachment);
    }
    public void add(Matcher matcher, Collection<T> attachments)
    {
        matchers.add(matcher);
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
        for (Matcher matcher : matchers)
        {
            Status s = matcher.match(cc);
            if (s == Status.Match || s == Status.WillMatch)
            {
                lastMatched = map.get(matcher);
                return s;
            }
            highest = Math.max(highest, s.ordinal());
        }
        return Status.values()[highest];
    }
    /**
     * Returns attachment of last matched matcher.
     * @return 
     */
    public List<T> getLastMatched()
    {
        return lastMatched;
    }
    
}
