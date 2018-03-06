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
public class Honolulu extends DefaultCustomizer
{

    @Override
    public String mapLine(String line)
    {
        return super.mapLine(line)
                .replace("HFO", "")
                .replace("OPC", "")
                .replace("NHC", "")
                .replace("EAST OF 157W", "100W - 157W")
                .replace("EAST OF 180W", "105W - 180W")
                .replace("EAST OF 130W", "78W - 130W")
                .replace("EAST OF 145W", "70W - 145W")
                .trim()
                ;
    }
    
}
