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

import static java.lang.Math.max;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.util.logging.JavaLogging;

/**
 * @deprecated doesn't work???
 * AbstractFunctionQueue provides put/get methods to queue parameter transfer
 * between threads.
 * <p>It is meant to replace object creation just for queuing.
 * <p>AbstractFunctionQueue is backed by ByteBuffer.
 * <p>Important! Implementing class must create matching put and get methods. Put method 
 * must call hasMoreData() after putting all data. Get method must call
 * hasMoreRoom() after getting corresponding data from buffer.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.util.AbstractFunctionQueue#hasMoreData() 
 * @see org.vesalainen.util.AbstractFunctionQueue#hasMoreRoom() 
 */
public abstract class AbstractFunctionQueue extends JavaLogging
{
    private final int size;
    private final ByteBuffer readBuf;
    private final ByteBuffer writeBuf;
    private final ReentrantLock lock;
    private final Condition hasRoom;
    private final Condition hasData;
    private AtomicInteger writable;
    private int maxQueue;
    
    public AbstractFunctionQueue(int size)
    {
        super(AbstractFunctionQueue.class);
        this.size = size;
        this.writeBuf = ByteBuffer.allocate(size);
        this.readBuf = (ByteBuffer) writeBuf.asReadOnlyBuffer();
        this.lock = new ReentrantLock();
        this.hasData = lock.newCondition();
        this.hasRoom = lock.newCondition();
        this.writable = new AtomicInteger(size);
    }
    protected AbstractFunctionQueue put(byte b) throws InterruptedException
    {
        needToWrite(1);
        writeBuf.put(b);
        return this;
    }
    protected AbstractFunctionQueue putShort(short s) throws InterruptedException
    {
        needToWrite(2);
        writeBuf.putShort(s);
        return this;
    }
    protected AbstractFunctionQueue putChar(char c) throws InterruptedException
    {
        needToWrite(2);
        writeBuf.putChar(c);
        return this;
    }
    protected AbstractFunctionQueue putInt(int i) throws InterruptedException
    {
        needToWrite(4);
        writeBuf.putInt(i);
        return this;
    }
    protected AbstractFunctionQueue putFloat(float f) throws InterruptedException
    {
        needToWrite(4);
        writeBuf.putFloat(f);
        return this;
    }
    protected AbstractFunctionQueue putLong(long l) throws InterruptedException
    {
        needToWrite(8);
        writeBuf.putLong(l);
        return this;
    }
    protected AbstractFunctionQueue putDouble(double d) throws InterruptedException
    {
        needToWrite(8);
        writeBuf.putDouble(d);
        return this;
    }
    protected AbstractFunctionQueue putString(double d) throws InterruptedException
    {
        needToWrite(8);
        writeBuf.putDouble(d);
        return this;
    }
    protected byte getByte() throws InterruptedException
    {
        needToRead(1);
        return readBuf.get();
    }
    protected short getShort() throws InterruptedException
    {
        needToRead(2);
        return readBuf.getShort();
    }
    protected char getChar() throws InterruptedException
    {
        needToRead(2);
        return readBuf.getChar();
    }
    protected int getInt() throws InterruptedException
    {
        needToRead(4);
        return readBuf.getInt();
    }
    protected float getFloat() throws InterruptedException
    {
        needToRead(4);
        return readBuf.getFloat();
    }
    protected long getLong() throws InterruptedException
    {
        needToRead(8);
        return readBuf.getLong();
    }
    protected double getDouble() throws InterruptedException
    {
        needToRead(8);
        return readBuf.getDouble();
    }
    private int readable()
    {
        return size - writable.get();
    }
    private void needToWrite(int length) throws InterruptedException
    {
        while (writable.get() < length)
        {
            await(hasRoom);
        }
        int remaining = writeBuf.remaining();
        if (remaining < length)
        {
            while (writable.get() < remaining+length)
            {
                await(hasRoom);
            }
            writeBuf.clear();
        }
        writable.addAndGet(-length);
        maxQueue = max(readable(), maxQueue);
    }
    private void needToRead(int length) throws InterruptedException
    {
        while (readable() < length)
        {
            await(hasData);
        }
        int remaining = readBuf.remaining();
        if (remaining < length)
        {
            while (readable() < remaining+length)
            {
                await(hasData);
            }
            readBuf.clear();
        }
        writable.addAndGet(length);
    }

    protected void hasMoreData()
    {
        signal(hasData);
    }
    protected void hasMoreRoom()
    {
        signal(hasRoom);
    }
    private void await(Condition condition) throws InterruptedException
    {
        lock.lock();
        try
        {
            condition.await();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void signal(Condition condition)
    {
        lock.lock();
        try
        {
            condition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getQueueLength()
    {
        return readable();
    }

    public int getMaxQueueLength()
    {
        return maxQueue;
    }


}
