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

import static java.lang.Integer.max;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MessageClass extends JavaLogging
{
    protected int id;
    protected String name;
    protected int size;
    protected String transmitter;
    protected Map<String,SignalClass> signals = new HashMap<>();
    protected String comment;
    protected Map<String,Attribute> attributes = new HashMap<>();

    public MessageClass(Integer id, String name, Integer size, String transmitter, List<SignalClass> signals)
    {
        super(MessageClass.class);
        this.id = id;
        this.name = name;
        this.size = size;
        this.transmitter = transmitter;
        for (SignalClass signal : signals)
        {
            this.signals.put(signal.getName(), signal);
        }
    }

    public void forEach(Consumer<? super SignalClass> action)
    {
        signals.values().forEach(action);
    }
    public int getId()
    {
        return id;
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

    public String getComment()
    {
        return comment;
    }

    public Map<String,SignalClass> getSignals()
    {
        return signals;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setSignalComment(String name, String comment)
    {
        SignalClass signal = signals.get(name);
        signal.setComment(comment);
    }

    public void setAttribute(Attribute attribute)
    {
        attributes.put(attribute.getName(), attribute);
    }

    public void setSignalAttribute(String signalName, Attribute attribute)
    {
        SignalClass signal = signals.get(signalName);
        signal.setAttribute(attribute);
    }

    @Override
    public String toString()
    {
        return "Message{" + Integer.toHexString(id) + ", name=" + name + ", size=" + size + ", comment=" + comment + '}';
    }

    public void setSignalValueDescription(String signalName, List<ValueDescription> valDesc)
    {
        SignalClass signal = signals.get(signalName);
        signal.setValueDescription(valDesc);
    }

}
