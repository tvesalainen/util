/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.io.Writer;

/**
 *
 * @author Timo Vesalainen
 */
@TransactionalSetterClass("org.vesalainen.code.TSImpl")
public abstract class TS extends TransactionalSetter implements TrIntf
{
    private boolean z;
    private byte b;
    private char c;
    private short s;
    private int i;
    private long l;
    private float f;
    private double d;
    private Writer writer;
    private String string;

    public boolean isZ()
    {
        return z;
    }

    public void setZ(boolean z)
    {
        this.z = z;
    }

    public byte getB()
    {
        return b;
    }

    public void setB(byte b)
    {
        this.b = b;
    }

    public char getC()
    {
        return c;
    }

    public void setC(char c)
    {
        this.c = c;
    }

    public short getS()
    {
        return s;
    }

    public void setS(short s)
    {
        this.s = s;
    }

    public int getI()
    {
        return i;
    }

    public void setI(int i)
    {
        this.i = i;
    }

    public long getL()
    {
        return l;
    }

    public void setJ(long l)
    {
        this.l = l;
    }

    public float getF()
    {
        return f;
    }

    public void setF(float f)
    {
        this.f = f;
    }

    public double getD()
    {
        return d;
    }

    public void setD(double d)
    {
        this.d = d;
    }

    public Writer getWriter()
    {
        return writer;
    }

    public void setWriter(Writer writer)
    {
        this.writer = writer;
    }

    public String getString()
    {
        return string;
    }

    public void setString(String string)
    {
        this.string = string;
    }

}
