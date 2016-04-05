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
import java.util.Collection;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.CharSequences;

/**
 * A CharSequence implementation backed by ByteBuffer
 */
public class ByteBufferCharSequence implements CharSequence
{
    private final ByteBufferCharSequenceFactory factory;
    private ByteBuffer bb;
    private int position;
    private int limit;

    ByteBufferCharSequence(ByteBufferCharSequenceFactory factory)
    {
        this.factory = factory;
        this.bb = factory.getBb().slice();
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
        return factory.create(position + start, position + end);
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
        seqs.stream().forEach((org.vesalainen.nio.ByteBufferCharSequence seq) -> seq.reset());
    }
}
