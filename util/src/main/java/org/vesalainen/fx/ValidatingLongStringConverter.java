/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ValidatingLongStringConverter extends ValidatingNumberStringConverter<Long>
{

    public ValidatingLongStringConverter(long min, long max)
    {
        super(min, max);
    }

    @Override
    public Long fromString(String string)
    {
        return check(Long.valueOf(string));
    }

}
