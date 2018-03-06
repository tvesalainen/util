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
package org.vesalainen.ham.station;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NorthwoodPersianGulf extends DefaultCustomizer
{

    @Override
    public boolean isMapStart(String line)
    {
        return line.startsWith("ALL MAPS");
    }

    @Override
    public String mapLine(String line)
    {
        return super.mapLine(line)
                .replace("ALL MAPS 40°30′N.15°30′E 40°30′N.80°E     03°N.15°30′E 3°N.80°E", "40.5N 15.5E 40.5N 80E     03N 15.5E 3N80E")
                ;
    }
    
}
