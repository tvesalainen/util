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
package org.vesalainen.can.dbc;

import java.nio.ByteOrder;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.util.List;
import static java.util.Locale.US;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import org.vesalainen.can.SignalType;
import static org.vesalainen.can.SignalType.*;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.util.IndexMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalClass extends DBCBase
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
    private String unit = "";
    private List<String> receivers;
    private IndexMap<String> lookupMap;
    private List<ValueDescription> valueDescriptions;

    public SignalClass(String name, MultiplexerIndicator multiplexerIndicator, Integer startBit, Integer size, ByteOrder byteOrder, ValueType valueType, Double factor, Double offset, Double min, Double max, String unit, List<String> receivers)
    {
        this.name = name;
        this.multiplexerIndicator = multiplexerIndicator;
        this.startBit = normalizeStartBit(startBit, byteOrder);
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
    public static int normalizeStartBit(int ab, ByteOrder byteOrder)
    {
        if (byteOrder == BIG_ENDIAN)
        {
            return 8*(ab/8)+7-ab%8; 
        }
        return ab;
    }
    public static int abnormalizeStartBit(int no, ByteOrder byteOrder)
    {
        if (byteOrder == BIG_ENDIAN)
        {
            return 8*(no/8)+7-no%8; 
        }
        return no;
    }
    public SignalType getSignalType()
    {
        String type = getStringAttribute("SignalType");
        if (type != null)
        {
            switch (type)
            {
                case "Integer":
                    //assert getType(size, valueType == SIGNED, factor, offset) == INT;
                    return INT;
                case "Latitude":
                case "Longitude":
                case "Time":
                case "Temperature":
                case "Temperature (hires)":
                case "Pressure":
                case "Pressure (hires)":
                    return DOUBLE;
                case "Lookup table":
                    return LOOKUP;
                case "Binary data":
                case "Manufacturer code":
                    return BINARY;
                case "Date":
                    return INT;
                case "ASCII text":
                    return ASCII;
                default:
                    throw new UnsupportedOperationException(type+" not supported");
            }
        }
        else
        {
            return getType(size, valueType == SIGNED, factor, offset);
        }
    }
    public IntFunction getMapper()
    {
        return lookupMap::get;
    }
    public static SignalType getType(int bits, boolean signed, double factor, double offset)
    {
        if (isInteger(factor, offset))
        {
            if (signed)
            {
                return bits <= 32 ? INT : LONG;
            }
            else
            {
                return bits <= 31 ? INT : LONG;
            }
        }
        else
        {
            return DOUBLE;
        }
    }
    public static boolean isInteger(double factor, double offset)
    {
        return Math.floor(factor) == factor && Math.floor(offset) == offset;
    }
    public String getDescription(int value)
    {
        if (lookupMap != null)
        {
            return lookupMap.get(value);
        }
        throw new IllegalArgumentException(name+" doesn't have lookup map");
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

    public List<ValueDescription> getValueDescriptions()
    {
        return valueDescriptions;
    }

    public void setValueDescription(List<ValueDescription> valDesc)
    {
        this.valueDescriptions = valDesc;
        IndexMap.Builder<String> builder = new IndexMap.Builder<>();
        valDesc.forEach((vd)->builder.put(vd.getValue(), vd.getDescription()));
        lookupMap = builder.build();
    }
            
    @Override
    public String toString()
    {
        return "Signal{" + name + ", startBit=" + startBit + ", size=" + size + ", byteOrder=" + byteOrder + ", valueType=" + valueType + ", factor=" + factor + ", offset=" + offset + ", unit=" + unit + ", comment=" + comment + '}';
    }

    void print(AppendablePrinter out)
    {
        out.format(US, " SG_ %s : %d|%d@%c%c (%s, %s) [%s|%s] \"%s\" %s\n", 
                name,
                abnormalizeStartBit(startBit, byteOrder),
                size,
                byteOrder == BIG_ENDIAN ? '0' : '1',
                valueType == UNSIGNED ? '+' : '-',
                Double.toString(factor),
                Double.toString(offset),
                Double.toString(min),
                Double.toString(max),
                unit,
                receivers.stream().collect(Collectors.joining(","))
        );
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final SignalClass other = (SignalClass) obj;
        if (this.startBit != other.startBit)
        {
            return false;
        }
        if (this.size != other.size)
        {
            return false;
        }
        if (Double.doubleToLongBits(this.factor) != Double.doubleToLongBits(other.factor))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.offset) != Double.doubleToLongBits(other.offset))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(other.min))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max))
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.unit, other.unit))
        {
            return false;
        }
        if (!Objects.equals(this.multiplexerIndicator, other.multiplexerIndicator))
        {
            return false;
        }
        if (!Objects.equals(this.byteOrder, other.byteOrder))
        {
            return false;
        }
        if (this.valueType != other.valueType)
        {
            return false;
        }
        if (!Objects.equals(this.receivers, other.receivers))
        {
            return false;
        }
        if (!Objects.equals(this.valueDescriptions, other.valueDescriptions))
        {
            return false;
        }
        return true;
    }
}
