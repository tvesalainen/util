/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nio;

import java.nio.ByteBuffer;
import java.util.function.IntUnaryOperator;
import org.vesalainen.util.CharSequences;

/**
 * A CharSequence implementation backed by ByteBuffer. Implemented CharSequence
 * is between 0 and position.
 * <p>Charset is US_ASCII
 */
public class PeekReadCharSequence implements CharSequence
{
    private ByteBuffer bb;
    private final IntUnaryOperator op;
    /**
     * 
     * @param factory 
     */
    PeekReadCharSequence(ByteBufferCharSequenceFactory factory)
    {
        this.bb = factory.getBb().asReadOnlyBuffer();
        this.op = factory.getOp();
    }
    /**
     * 
     * @param bb 
     */
    public PeekReadCharSequence(ByteBuffer bb)
    {
        this(bb, (x)->{return x;});
    }
    /**
     * 
     * @param bb
     * @param op An operator for converting characters in equals and hashCode.
     * Default implementation is identity. Using e.g. Character::toLowerCase
     * implements case insensitive equals and hashCode.
     */
    public PeekReadCharSequence(ByteBuffer bb, IntUnaryOperator op)
    {
        this.bb = bb;
        this.op = op;
    }

    @Override
    public int length()
    {
        return bb.position();
    }

    @Override
    public char charAt(int index)
    {
        return (char) bb.get(index);
    }
    /**
     * Not Implemented!
     * @param start
     * @param end
     * @return 
     */
    @Override
    public CharSequence subSequence(int start, int end)
    {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public String toString()
    {
        return CharSequences.toString(this);
    }

    @Override
    public int hashCode()
    {
        return CharSequences.hashCode(this, op);
    }

    @Override
    public boolean equals(Object obj)
    {
        return CharSequences.equals(this, obj, op);
    }

}
