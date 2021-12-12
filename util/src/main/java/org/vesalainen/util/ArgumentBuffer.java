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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArgumentBuffer
{
    private final ByteBuffer readBuf;
    private final ByteBuffer writeBuf;
    private final ReentrantLock lock;
    private final Condition hasRoom;
    private final Condition hasData;
    
    public ArgumentBuffer(int size)
    {
        this.writeBuf = ByteBuffer.allocate(size);
        this.readBuf = (ByteBuffer) writeBuf.asReadOnlyBuffer().limit(0);
        this.lock = new ReentrantLock();
        this.hasData = lock.newCondition();
        this.hasRoom = lock.newCondition();
    }
    public ArgumentBuffer put(byte b)
    {
        lock.lock();
        try
        {
            needToWrite(1);
            writeBuf.put(b);
            wrote(1);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putShort(short s)
    {
        lock.lock();
        try
        {
            needToWrite(2);
            writeBuf.putShort(s);
            wrote(2);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putChar(char c)
    {
        lock.lock();
        try
        {
            needToWrite(2);
            writeBuf.putChar(c);
            wrote(2);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putInt(int i)
    {
        lock.lock();
        try
        {
            needToWrite(4);
            writeBuf.putInt(i);
            wrote(4);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putFloat(float f)
    {
        lock.lock();
        try
        {
            needToWrite(4);
            writeBuf.putFloat(f);
            wrote(4);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putLong(long l)
    {
        lock.lock();
        try
        {
            needToWrite(8);
            writeBuf.putLong(l);
            wrote(8);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public ArgumentBuffer putDouble(double d)
    {
        lock.lock();
        try
        {
            needToWrite(8);
            writeBuf.putDouble(d);
            wrote(8);
            return this;
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            lock.unlock();
        }
    }
    public byte getByte()
    {
        lock.lock();
        try
        {
            needToRead(1);
            return readBuf.get();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public short getShort()
    {
        lock.lock();
        try
        {
            needToRead(2);
            return readBuf.getShort();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public char getChar()
    {
        lock.lock();
        try
        {
            needToRead(2);
            return readBuf.getChar();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public int getInt()
    {
        lock.lock();
        try
        {
            needToRead(4);
            return readBuf.getInt();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public float getFloat()
    {
        lock.lock();
        try
        {
            needToRead(4);
            return readBuf.getFloat();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public long getLong()
    {
        lock.lock();
        try
        {
            needToRead(8);
            return readBuf.getLong();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    public double getDouble()
    {
        lock.lock();
        try
        {
            needToRead(8);
            return readBuf.getDouble();
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }        
        finally
        {
            hasRoom.signal();
            lock.unlock();
        }
    }
    private void needToWrite(int size) throws InterruptedException
    {
        while (writeBuf.remaining() < size)
        {
            if (writeBuf.capacity() - writeBuf.limit() < size)
            {
                writeBuf.position(0);
                writeBuf.limit(readBuf.position());
            }
            else
            {
                hasRoom.await();
            }
        }
    }
    private void wrote(int len)
    {
        if (readBuf.capacity() - readBuf.limit() < len)
        {
            readBuf.position(0);
            readBuf.limit(writeBuf.position());
        }
        else
        {
            readBuf.limit(readBuf.limit()+len);
        }
        hasData.signal();
    }
    private void needToRead(int size) throws InterruptedException
    {
        while (readBuf.remaining() < size)
        {
            if (readBuf.capacity() - readBuf.limit() < size)
            {
                readBuf.position(0);
                readBuf.limit(writeBuf.position());
            }
            else
            {
                hasData.await();
            }
        }
    }

}
