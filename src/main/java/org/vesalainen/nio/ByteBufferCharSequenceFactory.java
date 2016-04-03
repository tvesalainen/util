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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.CharSequences;

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
     * Selection is 0 - position
     * @return 
     */
    public ByteBufferCharSequence afterRead()
    {
        return create(0, bb.position());
    }
    /**
     * Creates or reuses a ByteBufferCharSequence object.
     * Selection is position - limit
     * @return 
     */
    public ByteBufferCharSequence positionToLimit()
    {
        return create(bb.position(), bb.limit());
    }
    /**
     * Creates or reuses a ByteBufferCharSequence object.
     * Selection is 0 - limit
     * @return 
     */
    public ByteBufferCharSequence all()
    {
        return create(0, bb.limit());
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
            seq = new ByteBufferCharSequence(bb);
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
     * of arguments. Argument s2 must immediately follow s1, otherwise  
     * IllegalArgumentException is thrown.
     * @param s1
     * @param s2
     * @return 
     */
    public ByteBufferCharSequence concat(ByteBufferCharSequence s1, ByteBufferCharSequence s2)
    {
        if (s1.bb.limit() != s2.bb.position())
        {
            throw new IllegalArgumentException("sequences are not next to each other");
        }
        return create(s1.bb.position(), s2.bb.limit());
    }
    /**
     * Writes seq content to channel
     * @param channel
     * @param seq
     * @throws IOException 
     */
    public static void writeAll(WritableByteChannel channel, ByteBufferCharSequence seq) throws IOException
    {
        ChannelHelper.writeAll(channel, seq.bb);
        seq.reset();
    }
    /**
     * Writes seqs content to channel
     * @param channel
     * @param seqs
     * @throws IOException 
     */
    public static void writeAll(GatheringByteChannel channel, ByteBufferCharSequence... seqs) throws IOException
    {
        int length = seqs.length;
        ByteBuffer[] bba = new ByteBuffer[length];
        for (int ii=0;ii<length;ii++)
        {
            bba[ii] = seqs[ii].bb;
        }
        ChannelHelper.writeAll(channel, bba);
        for (int ii=0;ii<length;ii++)
        {
            seqs[ii].reset();
        }
    }
    /**
     * Writes seqs content to channel
     * @param channel
     * @param seqs
     * @throws IOException 
     */
    public static void writeAll(GatheringByteChannel channel, Collection<ByteBufferCharSequence> seqs) throws IOException
    {
        int length = seqs.size();
        ByteBuffer[] bba = new ByteBuffer[length];
        int ii=0;
        for (ByteBufferCharSequence seq : seqs)
        {
            bba[ii++] = seq.bb;
        }
        ChannelHelper.writeAll(channel, bba);
        seqs.stream().forEach((seq) -> seq.reset());
    }
    /**
     * A CharSequence implementation backed by ByteBuffer
     */
    public class ByteBufferCharSequence implements CharSequence
    {
        private ByteBuffer bb;
        private int position;
        private int limit;

        private ByteBufferCharSequence(ByteBuffer bb)
        {
            this.bb = bb.slice();
        }

        private void set(int position, int limit)
        {
            bb.clear();
            bb.position(position);
            bb.limit(limit);
            this.position = position;
            this.limit = limit;
        }
        private void reset()
        {
            bb.clear();
            bb.position(position);
            bb.limit(limit);
        }
        @Override
        public int length()
        {
            return bb.remaining();
        }

        @Override
        public char charAt(int index)
        {
            return (char) bb.get(position + index);
        }

        @Override
        public CharSequence subSequence(int start, int end)
        {
            return create(position + start, position + end);
        }
        @Override
        public String toString()
        {
            return CharSequences.toString(this);
        }

        @Override
        public int hashCode()
        {
            return CharSequences.hashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return CharSequences.equals(this, obj);
        }

    }
}
