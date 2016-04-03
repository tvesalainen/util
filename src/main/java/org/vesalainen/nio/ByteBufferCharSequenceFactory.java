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
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A factory to create ByteBufferCharSequence views to ByteBuffer. These views
 * can be reused by using reset(). There are also write methods to write content 
 * to Channels.
 * 
 * <p>CharSequence implementations use hashCode method implemented in CharSequences. 
 * Classes with 
 * same hashCode implementation can be used in HashMaps etc.
 * in CharSequences. 
 * @author tkv
 * @see org.vesalainen.util.CharSequences#hashCode(java.lang.CharSequence) 
 */
public class ByteBufferCharSequenceFactory
{
    private final ByteBuffer bb;
    private final Deque<ByteBufferCharSequence> freshStack = new ArrayDeque<>();
    private final Deque<ByteBufferCharSequence> usedStack = new ArrayDeque<>();
    /**
     * Creates ByteBufferCharSequenceFactory backed by ByteBuffer.
     * @param bb 
     */
    public ByteBufferCharSequenceFactory(ByteBuffer bb)
    {
        this.bb = bb;
    }

    ByteBuffer getBb()
    {
        return bb;
    }
    
    /**
     * After reset all ByteBufferCharSequence instances created by create or concat
     * methods of this factory are reused. Their behavior is undefined.
     */
    public void reset()
    {
        freshStack.addAll(usedStack);
        usedStack.clear();
    }
    /**
     * Creates or reuses a ByteBufferCharSequence object.
     * Selection is position - limit
     * @return 
     */
    public ByteBufferCharSequence allRemaining()
    {
        return create(bb.position(), bb.limit());
    }
    /**
     * Creates or reuses a ByteBufferCharSequence object.
     * @param position
     * @param limit
     * @return 
     */
    public ByteBufferCharSequence create(int position, int limit)
    {
        ByteBufferCharSequence seq = null;
        if (freshStack.isEmpty())
        {
            seq = new ByteBufferCharSequence(this);
        }
        else
        {
            seq = freshStack.pop();
        }
        seq.set(position, limit);
        usedStack.push(seq);
        return seq;
    }
    /**
     * Creates or reuses a ByteBufferCharSequence object. Objects is concatenation
     * of arguments and everything between them.
     * @param s1
     * @param s2
     * @return 
     */
    public ByteBufferCharSequence concat(ByteBufferCharSequence s1, ByteBufferCharSequence s2)
    {
        return create(s1.getPosition(), s2.getLimit());
    }
}
