/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.time;

import java.time.temporal.ChronoField;
import java.util.EnumMap;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;

/**
 *
 * @author tkv
 */
public class SimpleMutableTime implements MutableTime
{
    private IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    
    @Override
    public int get(ChronoField chronoField)
    {
        checkField(chronoField);
        if (fields.containsKey(chronoField))
        {
            return fields.getInt(chronoField);
        }
        else
        {
            return (int) chronoField.range().getMinimum();
        }
    }
    @Override
    public void set(ChronoField chronoField, int amount)
    {
        checkField(chronoField);
        chronoField.checkValidIntValue(amount);
        fields.put(chronoField, amount);
    }
}
