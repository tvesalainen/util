/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham;

import java.util.function.Predicate;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationFilter extends JavaLogging implements Predicate<Schedule>
{
    
    private Location location;

    public LocationFilter(Location location)
    {
        super(LocationFilter.class);
        this.location = location;
    }

    @Override
    public boolean test(Schedule schedule)
    {
        if (schedule instanceof HfFax)
        {
            HfFax fax = (HfFax) schedule;
            boolean b = fax.inMap(location) || fax.getStation().inAnyMap(location);
            if (!b)
            {
                finer("dropping %s because not in an map", schedule);
            }
            return b;
        }
        return true;
    }
    
}
