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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.vesalainen.util.concurrent.Locks;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConcurrentCompressedOutput extends CompressedOutput
{
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReadLock readLock = readWriteLock.readLock();
    private WriteLock writeLock = readWriteLock.writeLock();
    
    public ConcurrentCompressedOutput(OutputStream out, String source)
    {
        super(out, source);
    }

    @Override
    public void close() throws IOException
    {
        Locks.lockedIO(writeLock, ()->super.close());
    }

    @Override
    public void setDouble(String property, double value)
    {
        Locks.locked(writeLock, ()->super.setDouble(property, value));
    }

    @Override
    public void setFloat(String property, float value)
    {
        Locks.locked(writeLock, ()->super.setFloat(property, value));
    }

    @Override
    public void setLong(String property, long value)
    {
        Locks.locked(writeLock, ()->super.setLong(property, value));
    }

    @Override
    public void setInt(String property, int value)
    {
        Locks.locked(writeLock, ()->super.setInt(property, value));
    }

    @Override
    public void setShort(String property, short value)
    {
        Locks.locked(writeLock, ()->super.setShort(property, value));
    }

    @Override
    public void setChar(String property, char value)
    {
        Locks.locked(writeLock, ()->super.setChar(property, value));
    }

    @Override
    public void setByte(String property, byte value)
    {
        Locks.locked(writeLock, ()->super.setByte(property, value));
    }

    @Override
    public void setBoolean(String property, boolean value)
    {
        Locks.locked(writeLock, ()->super.setBoolean(property, value));
    }

    @Override
    public float write() throws IOException
    {
        return Locks.lockedIO(readLock, ()->super.write());
    }

    @Override
    public double getDouble(String property)
    {
        return Locks.locked(readLock, ()->super.getDouble(property));
    }

    @Override
    public float getFloat(String property)
    {
        return Locks.locked(readLock, ()->super.getFloat(property));
    }

    @Override
    public long getLong(String property)
    {
        return Locks.locked(readLock, ()->super.getLong(property));
    }

    @Override
    public int getInt(String property)
    {
        return Locks.locked(readLock, ()->super.getInt(property));
    }

    @Override
    public short getShort(String property)
    {
        return Locks.locked(readLock, ()->super.getShort(property));
    }

    @Override
    public char getChar(String property)
    {
        return Locks.locked(readLock, ()->super.getChar(property));
    }

    @Override
    public byte getByte(String property)
    {
        return Locks.locked(readLock, ()->super.getByte(property));
    }

    @Override
    public boolean getBoolean(String property)
    {
        return Locks.locked(readLock, ()->super.getBoolean(property));
    }

}
