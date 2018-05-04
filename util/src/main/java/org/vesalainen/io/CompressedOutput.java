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
package org.vesalainen.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.vesalainen.util.BitArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedOutput extends CompressedIO
{
    private OutputStream out;
    private ByteBuffer bb2;
    private BitArray selfImportantArray;
    private boolean ready;
    private int writeCount;
    private int writeBytes;

    public CompressedOutput(OutputStream out, String source)
    {
        super(source);
        this.out = out;
    }
    
    /**
     * Writes objects fields to stream
     * @return Compression rate
     * @throws IOException 
     */
    public float write() throws IOException
    {
        bitArray.setAll(false);
        for (int ii=0;ii<bytes;ii++)
        {
            bitArray.set(ii, bb1.get(ii) != bb2.get(ii));
        }
        if (!bitArray.any())
        {
            return 0;
        }
        if (!bitArray.and(selfImportantArray))
        {
            return 0;
        }
        if (writeCount == 0)
        {
            DataOutputStream dos = new DataOutputStream(out);

            dos.writeUTF(source);
            dos.writeShort(properties.size());
            List<Property> props = new ArrayList<>(properties.values());
            props.sort(null);
            for (Property prop : props)
            {
                dos.writeUTF(prop.getName());
                dos.writeUTF(prop.getType());
            }
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
            dos.flush();
        }
        out.write(bits);
        int cnt = 0;
        for (int ii=0;ii<bytes;ii++)
        {
            if (bitArray.isSet(ii))
            {
                byte b = bb2.get(ii);
                out.write(b);
                bb1.put(ii, b);
                cnt++;
                writeBytes++;
            }
        }
        writeCount++;
        return (float)cnt/(float)bytes;
    }
    public void ready()
    {
        bb1 = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN);
        bb2 = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN);
        bitArray = new BitArray(bytes);
        bits = bitArray.getArray();
        uuid = UUID.randomUUID();
        selfImportantArray = new BitArray(bytes);
        selfImportantArray.setAll(true);
        for (Property prop : properties.values())
        {
            if (!prop.isSelfImportant())
            {
                selfImportantArray.set(prop.getOffset(), prop.getSize(), false);
            }
        }
        ready = true;
    }
    /**
     * Returns true if write method has been called at least once.
     * @return 
     */
    public boolean hasData()
    {
        return writeCount > 0;
    }
    
    
    public void addBoolean(String property)
    {
        add(property, "boolean", true);
    }
    public void addByte(String property)
    {
        add(property, "byte", true);
    }
    public void addChar(String property)
    {
        add(property, "char", true);
    }
    public void addInt(String property)
    {
        add(property, "int", true);
    }
    public void addLong(String property)
    {
        add(property, "long", true);
    }
    public void addFloat(String property)
    {
        add(property, "float", true);
    }
    public void addDouble(String property)
    {
        add(property, "double", true);
    }
    public void addBoolean(String property, boolean selfImportant)
    {
        add(property, "boolean", selfImportant);
    }
    public void addByte(String property, boolean selfImportant)
    {
        add(property, "byte", selfImportant);
    }
    public void addChar(String property, boolean selfImportant)
    {
        add(property, "char", selfImportant);
    }
    public void addInt(String property, boolean selfImportant)
    {
        add(property, "int", selfImportant);
    }
    public void addLong(String property, boolean selfImportant)
    {
        add(property, "long", selfImportant);
    }
    public void addFloat(String property, boolean selfImportant)
    {
        add(property, "float", selfImportant);
    }
    public void addDouble(String property, boolean selfImportant)
    {
        add(property, "double", selfImportant);
    }
    public void set(String property, Object value)
    {
        Property prop = properties.get(property);
        if (prop == null)
        {
            throw new IllegalArgumentException(property+" not found");
        }
        switch (prop.getType())
        {
            case "boolean":
                setBoolean(property, (boolean) value);
                break;
            case "byte":
                setByte(property, (byte) value);
                break;
            case "char":
                setChar(property, (char) value);
                break;
            case "short":
                setShort(property, (short) value);
                break;
            case "int":
                setInt(property, (int) value);
                break;
            case "float":
                setFloat(property, (float) value);
                break;
            case "long":
                setLong(property, (long) value);
                break;
            case "double":
                setDouble(property, (double) value);
                break;
            default:
                throw new IllegalArgumentException(prop.getType() + " not allowed");
        }
    }
    public void setBoolean(String property, boolean value)
    {
        checkReady();
        Property prop = check(property, "boolean");
        byte b = (byte) (value ? 1 : 0);
        bb2.put(prop.getOffset(), b);
    }
    public void setByte(String property, byte value)
    {
        checkReady();
        Property prop = check(property, "byte");
        bb2.put(prop.getOffset(), value);
    }
    public void setChar(String property, char value)
    {
        checkReady();
        Property prop = check(property, "char");
        bb2.putChar(prop.getOffset(), value);
    }
    public void setShort(String property, short value)
    {
        checkReady();
        Property prop = check(property, "short");
        bb2.putShort(prop.getOffset(), value);
    }
    public void setInt(String property, int value)
    {
        checkReady();
        Property prop = check(property, "int");
        bb2.putInt(prop.getOffset(), value);
    }
    public void setLong(String property, long value)
    {
        checkReady();
        Property prop = check(property, "long");
        bb2.putLong(prop.getOffset(), value);
    }
    public void setFloat(String property, float value)
    {
        checkReady();
        Property prop = check(property, "float");
        bb2.putFloat(prop.getOffset(), value);
    }
    public void setDouble(String property, double value)
    {
        checkReady();
        Property prop = check(property, "double");
        bb2.putDouble(prop.getOffset(), value);
    }
    @Override
    public void close() throws IOException
    {
        out.close();
    }
    private void checkReady()
    {
        if (!ready)
        {
            throw new IllegalStateException("ready not called");
        }
    }
    protected void add(String property, String type, boolean selfImportant)
    {
        if (ready)
        {
            throw new IllegalStateException("header written");
        }
        Property prop = new Property(property, type, bytes, selfImportant);
        Property old = properties.put(property, prop);
        if (old != null)
        {
            throw new IllegalArgumentException(property+" exists");
        }
        bytes += getBytes(type);
    }

}
