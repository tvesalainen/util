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

import java.util.ArrayList;
import java.util.List;

/**
 * @author tkv
 */
public class OrMatcher implements Matcher
{
    private final List<Matcher> matchers = new ArrayList<>();
    
    public void add(Matcher matcher)
    {
        matchers.add(matcher);
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
            if (s == Status.Match)
            {
                return s;
            }
            highest = Math.max(highest, s.ordinal());
        }
        return Status.values()[highest];
    }
    
}
