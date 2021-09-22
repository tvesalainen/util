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

import static java.lang.Integer.max;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.LinkedMap;
import org.vesalainen.util.SimpleIntRange;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MessageClass extends DBCBase implements AttachedLogger
{
    protected int id;
    protected String name;
    protected int size;
    protected String transmitter = "Vector__XXX";
    protected Map<String,SignalClass> signals = new LinkedMap<>();
    private boolean multiplexed;

    public MessageClass(DBCFile dbcFile, Integer id, String name, Integer size, String transmitter, List<SignalClass> signals)
    {
        super(dbcFile);
        this.id = id;
        this.name = name;
        this.size = size;
        this.transmitter = transmitter;
        for (SignalClass signal : signals)
        {
            if (signal.isMultiplexing())
            {
                this.multiplexed = true;
            }
            this.signals.put(signal.getName(), signal);
        }
    }

    public void forEach(Consumer<? super SignalClass> action)
    {
        signals.values().forEach(action);
    }

    public int getSignalCount()
    {
        return signals.size();
    }
    
    public IntRange getRepeatRange()
    {
        Long s = (Long) getAttributeValue("RepeatSize");
        int sz = (int) (s == null ? 0 : s);
        if (sz > 0)
        {
            Long st = (Long) getAttributeValue("RepeatStartBit");
            int start = (int) (st == null ? 0 : st);
            return new SimpleIntRange(start, start+sz);
        }
        else
        {
            return SimpleIntRange.EMPTY_RANGE;
        }
    }
    
    public boolean isMultiplexed()
    {
        return multiplexed;
    }
    
    public int getId()
    {
        return id;
    }

    public long getPrintId()
    {
        return Integer.toUnsignedLong(id);
    }
    
    public String getName()
    {
        return name;
    }

    public int getSize()
    {
        return size;
    }

    public int getMinSize()
    {
        int bitLimit = 0;
        for (Map.Entry<String, SignalClass> entry : signals.entrySet())
        {
            SignalClass s = entry.getValue();
            bitLimit = max(bitLimit, s.getStartBit()+s.getSize());
        }
        int calc = (int) Math.ceil(bitLimit/8.0);
        //info("min of %s size=%d calc=%d -> %d", name, size, calc, max(size, calc));
        return max(size, calc);
    }
    public String getTransmitter()
    {
        return transmitter;
    }


    public Map<String,SignalClass> getSignals()
    {
        return signals;
    }

    public SignalClass getSignal(String signalName)
    {
        SignalClass signal = signals.get(signalName);
        if (signal == null)
        {
            throw new IllegalArgumentException(signalName+" signal not found");
        }
        return signal;
    }
    public void setSignalComment(String name, String comment)
    {
        SignalClass signal = getSignal(name);
        signal.setComment(comment);
    }


    public void setSignalAttribute(String signalName, String name, Object value)
    {
        SignalClass signal = getSignal(signalName);
        signal.setAttributeValue(name, value);
    }

    @Override
    public String toString()
    {
        return "Message{" + id + ", name=" + name + ", size=" + size + ", comment=" + comment + '}';
    }

    public void setSignalValueDescription(String signalName, List<ValueDescription> valDesc)
    {
        SignalClass signal = getSignal(signalName);
        signal.setValueDescription(valDesc);
    }

    public void addMultiplexedSignal(String multiplexedSignalName, String multiplexorSwitchName, List<IntRange> multiplexorValueRanges)
    {
        SignalClass multiplexedSignal = getSignal(multiplexedSignalName);
        SignalClass multiplexorSwitch = getSignal(multiplexorSwitchName);
        MultiplexerIndicator multiplexerIndicator = multiplexedSignal.getMultiplexerIndicator();
        multiplexerIndicator.setMultiplexor(multiplexorSwitch, multiplexorValueRanges);
    }

    void print(AppendablePrinter out)
    {
        out.format("BO_ %d %s: %d %s\n", Integer.toUnsignedLong(id), name, size, transmitter);
        signals.values().forEach((s)->s.print(out));
        out.println();
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 31 * hash + this.id;
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
        final MessageClass other = (MessageClass) obj;
        if (this.id != other.id)
        {
            return false;
        }
        if (this.size != other.size)
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.transmitter, other.transmitter))
        {
            return false;
        }
        if (!Objects.equals(this.signals, other.signals))
        {
            return false;
        }
        return true;
    }

}
