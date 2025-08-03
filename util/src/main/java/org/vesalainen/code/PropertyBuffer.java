/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.code;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PropertyBuffer implements PropertySetter, Runnable
{
    protected Semaphore semaphore = new Semaphore(0);
    protected Thread thread;
    protected PropertySetter observer;
    protected int capacity;
    protected AtomicInteger count = new AtomicInteger();
    protected int index;
    protected JavaType[] typeArr;
    protected String[] propertyArr;
    protected boolean[] booleanArr;
    protected byte[] byteArr;
    protected char[] charArr;
    protected short[] shortArr;
    protected int[] intArr;
    protected long[] longArr;
    protected float[] floatArr;
    protected double[] doubleArr;
    protected Object[] objectArr;

    public PropertyBuffer(PropertySetter observer, int capacity)
    {
        this.observer = observer;
        this.capacity = capacity;
        this.count.set(capacity);
        typeArr = new JavaType[capacity];
        propertyArr = new String[capacity];
    }
    
    public void start()
    {
        thread = new Thread(this, PropertyBuffer.class.getSimpleName());
        thread.start();
    }
    
    public void stop()
    {
        thread.interrupt();
    }
    
    @Override
    public String[] getPrefixes()
    {
        return observer.getPrefixes();
    }

    @Override
    public void set(String property, boolean arg)
    {
        setType(property, JavaType.BOOLEAN);
        booleanArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, byte arg)
    {
        setType(property, JavaType.BYTE);
        byteArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, char arg)
    {
        setType(property, JavaType.CHAR);
        charArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, short arg)
    {
        setType(property, JavaType.SHORT);
        shortArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, int arg)
    {
        setType(property, JavaType.INT);
        intArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, long arg)
    {
        setType(property, JavaType.LONG);
        longArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, float arg)
    {
        setType(property, JavaType.FLOAT);
        floatArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, double arg)
    {
        setType(property, JavaType.DOUBLE);
        doubleArr[index] = arg;
        next();
    }

    @Override
    public void set(String property, Object arg)
    {
        setType(property, JavaType.DECLARED);
        objectArr[index] = arg;
        next();
    }

    @Override
    public void run()
    {
        int idx = 0;
        while (true)
        {
            try
            {
                semaphore.acquire();
                switch (typeArr[idx])
                {
                    case BOOLEAN:
                        observer.set(propertyArr[idx], booleanArr[idx]);
                        break;
                    case BYTE:
                        observer.set(propertyArr[idx], byteArr[idx]);
                        break;
                    case CHAR:
                        observer.set(propertyArr[idx], charArr[idx]);
                        break;
                    case SHORT:
                        observer.set(propertyArr[idx], shortArr[idx]);
                        break;
                    case INT:
                        observer.set(propertyArr[idx], intArr[idx]);
                        break;
                    case LONG:
                        observer.set(propertyArr[idx], longArr[idx]);
                        break;
                    case FLOAT:
                        observer.set(propertyArr[idx], floatArr[idx]);
                        break;
                    case DOUBLE:
                        observer.set(propertyArr[idx], doubleArr[idx]);
                        break;
                    case DECLARED:
                        observer.set(propertyArr[idx], objectArr[idx]);
                        break;
                }
                idx++;
                idx %= capacity;
                count.incrementAndGet();
            }
            catch (InterruptedException ex)
            {
                return; // stopped
            }
        }
    }

    private void setType(String property, JavaType javaType)
    {
        if (count.get() <= 0)
        {
            throw new IllegalStateException("capacity reached");
        }
        propertyArr[index] = property;
        switch (javaType)
        {
            case BOOLEAN:
                if (booleanArr == null)
                {
                    booleanArr = new boolean[capacity];
                }
                break;
            case BYTE:
                if (byteArr == null)
                {
                    byteArr = new byte[capacity];
                }
                break;
            case CHAR:
                if (charArr == null)
                {
                    charArr = new char[capacity];
                }
                break;
            case SHORT:
                if (shortArr == null)
                {
                    shortArr = new short[capacity];
                }
                break;
            case INT:
                if (intArr == null)
                {
                    intArr = new int[capacity];
                }
                break;
            case LONG:
                if (longArr == null)
                {
                    longArr = new long[capacity];
                }
                break;
            case FLOAT:
                if (floatArr == null)
                {
                    floatArr = new float[capacity];
                }
                break;
            case DOUBLE:
                if (doubleArr == null)
                {
                    doubleArr = new double[capacity];
                }
                break;
            case DECLARED:
                if (objectArr == null)
                {
                    objectArr = new Object[capacity];
                }
                break;
            default:
                assert(false);
        }
        typeArr[index] = javaType;
    }

    private void next()
    {
        index++;
        index %= capacity;
        count.decrementAndGet();
        semaphore.release();
    }
    
}
