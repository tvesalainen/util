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
public class Athens extends DefaultCustomizer
{

    @Override
    public String mapLine(String line)
    {
        return super.mapLine(line)
                .replace("- SOUTH EUROPE , MEDITERRANEAN SEA, BLACK SEA", "29N - 48N, 12W - 0E - 42E")
                .replace("- MEDITERRANEAN", "30N - 47N, 6W - 0E - 37E")
                .replace("- AEGEAN", "34N - 41N, 21E - 29E")
                ;
    }
    
}
