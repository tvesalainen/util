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
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.IntUnaryOperator;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.CharSequences;

/**
 * A CharSequence implementation backed by ByteBuffer. Implemented CharSequence
 * is between position and limit.
 * <p>Charset is US_ASCII
 */
public class ByteBufferCharSequence implements CharSequence
{
    private ByteBufferCharSequenceFactory factory;
    private ByteBuffer bb;
    private int position;
    private int limit;
    private final IntUnaryOperator op;
    /**
     * 
     * @param factory 
     */
    ByteBufferCharSequence(ByteBufferCharSequenceFactory factory)
    {
        this.factory = factory;
        this.bb = factory.getBb().asReadOnlyBuffer();
        this.op = factory.getOp();
    }
    /**
     * 
     * @param str 
     */
    public ByteBufferCharSequence(String str)
    {
        this(str, (x)->{return x;});
    }
    /**
     * 
     * @param str
     * @param op An operator for converting characters in equals and hashCode.
     * Default implementation is identity. Using e.g. Character::toLowerCase
     * implements case insensitive equals and hashCode.
     */
    public ByteBufferCharSequence(String str, IntUnaryOperator op)
    {
        this(ByteBuffer.wrap(str.getBytes(StandardCharsets.US_ASCII)), op);
    }
    /**
     * 
     * @param bb 
     */
    public ByteBufferCharSequence(ByteBuffer bb)
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
    public ByteBufferCharSequence(ByteBuffer bb, IntUnaryOperator op)
    {
        this.bb = bb;
        this.position = bb.position();
        this.limit = bb.limit();
        this.op = op;
    }

    void set(int position, int limit)
    {
        bb.clear();
        bb.position(position);
        bb.limit(limit);
        this.position = position;
        this.limit = limit;
    }

    void reset()
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
    /**
     * Returns subSequence of this with the same content as given. 
     * @param seq
     * @return 
     * @throws IllegalArgumentException If given content nt found
     */
    public CharSequence subSequence(CharSequence seq)
    {
        int idx = CharSequences.indexOf(this, seq);
        if (idx == -1)
        {
            throw new IllegalArgumentException(seq+" not found");
        }
        return subSequence(idx, idx+seq.length());
    }
    
    @Override
    public CharSequence subSequence(int start, int end)
    {
        if (factory != null)
        {
            return factory.create(position + start, position + end);
        }
        else
        {
            ByteBufferCharSequence s = new ByteBufferCharSequence(bb.asReadOnlyBuffer(), op);
            s.set(position + start, position + end);
            return s;
        }
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

    public int getPosition()
    {
        return position;
    }

    public int getLimit()
    {
        return limit;
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
        seqs.stream().forEach((ByteBufferCharSequence seq) -> seq.reset());
    }
}
