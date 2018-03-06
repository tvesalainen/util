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
public class PtReyes extends DefaultCustomizer
{

    @Override
    public String mapLine(String line)
    {
        return super.mapLine(line)
                .replace("EAST OF 145W", "145W - 70W")
                .replace("EAST OF 180W", "180W - 105W")
                .replace("EAST OF 150W", "150W - 100W")
                .replace("EAST OF 130W", "125W - 78W")
                .replace("EAST OF 157W", "157W - 100W")
                .replace("EAST OF 136W", "136W - 122W")
                ;
    }

    
}
