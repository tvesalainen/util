/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dict;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalClass
{
    private String name;
    private MultiplexerIndicator multiplexerIndicator;
    private int startBit;
    private int size;
    private ByteOrder byteOrder;
    private ValueType valueType;
    private double factor;
    private double offset;
    private double min;
    private double max;
    private String unit;
    private List<String> receivers;
    private String comment;
    private Map<String,Attribute> attributes = new HashMap<>();
    private List<ValueDescription> valueDescriptions;

    public SignalClass(String name, MultiplexerIndicator multiplexerIndicator, Integer startBit, Integer size, ByteOrder byteOrder, ValueType valueType, Double factor, Double offset, Double min, Double max, String unit, List<String> receivers)
    {
        this.name = name;
        this.multiplexerIndicator = multiplexerIndicator;
        this.startBit = startBit;
        this.size = size;
        this.byteOrder = byteOrder;
        this.valueType = valueType;
        this.factor = factor;
        this.offset = offset;
        this.min = min;
        this.max = max;
        this.unit = unit;
        this.receivers = receivers;
    }

    public String getName()
    {
        return name;
    }

    public MultiplexerIndicator getMultiplexerIndicator()
    {
        return multiplexerIndicator;
    }

    public int getStartBit()
    {
        return startBit;
    }

    public int getSize()
    {
        return size;
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    public ValueType getValueType()
    {
        return valueType;
    }

    public double getFactor()
    {
        return factor;
    }

    public double getOffset()
    {
        return offset;
    }

    public double getMin()
    {
        return min;
    }

    public double getMax()
    {
        return max;
    }

    public String getUnit()
    {
        return unit;
    }

    public List<String> getReceivers()
    {
        return receivers;
    }

    public String getComment()
    {
        return comment;
    }

    public Map<String, Attribute> getAttributes()
    {
        return attributes;
    }

    public List<ValueDescription> getValueDescriptions()
    {
        return valueDescriptions;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setAttribute(Attribute attribute)
    {
        attributes.put(attribute.getName(), attribute);
    }

    public void setValueDescription(List<ValueDescription> valDesc)
    {
        valueDescriptions = valDesc;
    }
            
    @Override
    public String toString()
    {
        return "Signal{" + name + ", startBit=" + startBit + ", size=" + size + ", byteOrder=" + byteOrder + ", valueType=" + valueType + ", factor=" + factor + ", offset=" + offset + ", unit=" + unit + ", comment=" + comment + '}';
    }

}
