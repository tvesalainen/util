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
package org.vesalainen.ham.itshfbc.station;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Halifax extends DefaultCustomizer
{

    @Override
    public String scheduleLine(String line)
    {
        return super.scheduleLine(line)
                .replace("0&12/12&0", "00/12")
                .replace("18&00", "18/00")
                .replace("06&12", "06/12")
                ;
    }
    
}
