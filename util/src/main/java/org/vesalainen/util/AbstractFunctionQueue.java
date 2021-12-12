/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractFunctionQueue provides put/get methods to queue parameter transfer
 * between threads.
 * <p>It is meant to replace object creation just for queuing.
 * <p>AbstractFunctionQueue is backed by ByteBuffer.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractFunctionQueue
{
    private final ByteBuffer readBuf;
    private final ByteBuffer writeBuf;
    protected final ReentrantLock lock;
    private final Condition hasRoom;
    private final Condition hasData;
    private int readable;
    private int writable;
    
    public AbstractFunctionQueue(int size)
    {
        this.writeBuf = ByteBuffer.allocate(size);
        this.readBuf = (ByteBuffer) writeBuf.asReadOnlyBuffer();
        this.lock = new ReentrantLock();
        this.hasData = lock.newCondition();
        this.hasRoom = lock.newCondition();
        this.writable = size;
    }
    protected AbstractFunctionQueue put(byte b)
    {
        needToWrite(1);
        writeBuf.put(b);
        return this;
    }
    protected AbstractFunctionQueue putShort(short s)
    {
        needToWrite(2);
        writeBuf.putShort(s);
        return this;
    }
    protected AbstractFunctionQueue putChar(char c)
    {
        needToWrite(2);
        writeBuf.putChar(c);
        return this;
    }
    protected AbstractFunctionQueue putInt(int i)
    {
        needToWrite(4);
        writeBuf.putInt(i);
        return this;
    }
    protected AbstractFunctionQueue putFloat(float f)
    {
        needToWrite(4);
        writeBuf.putFloat(f);
        return this;
    }
    protected AbstractFunctionQueue putLong(long l)
    {
        needToWrite(8);
        writeBuf.putLong(l);
        return this;
    }
    protected AbstractFunctionQueue putDouble(double d)
    {
        needToWrite(8);
        writeBuf.putDouble(d);
        return this;
    }
    protected byte getByte()
    {
        needToRead(1);
        return readBuf.get();
    }
    protected short getShort()
    {
        needToRead(2);
        return readBuf.getShort();
    }
    protected char getChar()
    {
        needToRead(2);
        return readBuf.getChar();
    }
    protected int getInt()
    {
        needToRead(4);
        return readBuf.getInt();
    }
    protected float getFloat()
    {
        needToRead(4);
        return readBuf.getFloat();
    }
    protected long getLong()
    {
        needToRead(8);
        return readBuf.getLong();
    }
    protected double getDouble()
    {
        needToRead(8);
        return readBuf.getDouble();
    }
    private void needToWrite(int size)
    {
        try
        {
            while (writable < size)
            {
                hasRoom.await();
            }
            int remaining = writeBuf.remaining();
            if (remaining < size)
            {
                while (writable < remaining+size)
                {
                    hasRoom.await();
                }
                writeBuf.clear();
            }
            writable -= size;
            readable += size;
            hasData.signal();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private void needToRead(int size)
    {
        try
        {
            while (readable < size)
            {
                hasData.await();
            }
            int remaining = readBuf.remaining();
            if (remaining < size)
            {
                while (readable < remaining+size)
                {
                    hasData.await();
                }
                readBuf.clear();
            }
            writable += size;
            readable -= size;
            hasRoom.signal();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }

}
