/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BitArray implements CharSequence
{
    private int bits;
    private byte[] array;

    public BitArray(int bits)
    {
        this(new byte[(int)Math.ceil((double)bits/8.0)], bits);
    }
    public BitArray(byte[] array)
    {
        this(array, array.length);
    }
    public BitArray(byte[] array, int bits)
    {
        this.array = array;
        this.bits = bits;
    }
    /**
     * Returns number of bits
     * @return 
     */
    public int bits()
    {
        return bits;
    }
    /**
     * Set count of bits starting from index to value
     * @param index
     * @param count
     * @param value 
     */
    public void set(int index, int count, boolean value)
    {
        for (int ii=0;ii<count;ii++)
        {
            set(ii+index, value);
        }
    }
    /**
     * Set bit at index to value
     * @param index
     * @param value 
     */
    public void set(int index, boolean value)
    {
        check(index);
        if (value)
        {
            array[index / 8] |= (1 << (index % 8));
        }
        else
        {
            array[index / 8] &= ~(1 << (index % 8));
        }
    }
    /**
     * Returns bit at index
     * @param index
     * @return 
     */
    public boolean isSet(int index)
    {
        check(index);
        return (array[index / 8] & (1 << (index % 8))) != 0;
    }
    /**
     * Sets all bits to value
     * @param value 
     */
    public void setAll(boolean value)
    {
        if (value)
        {
            Arrays.fill(array, (byte) 0b11111111);
        }
        else
        {
            Arrays.fill(array, (byte) 0);
        }
    }
    /**
     * Returns true if any bit is set
     * @return 
     */
    public boolean any()
    {
        int l = bits/8;
        for (int ii=0;ii<l;ii++)
        {
            if (array[ii] != 0)
            {
                return true;
            }
        }
        for (int ii=l*8;ii<bits;ii++)
        {
            if (isSet(ii))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * returns the first set bits index
     * @return 
     */
    public int first()
    {
        for (int ii=0;ii<array.length;ii++)
        {
            if (array[ii] != 0)
            {
                for (int jj=ii*8;jj<bits;jj++)
                {
                    if (isSet(jj))
                    {
                        return jj;
                    }
                }
            }
        }
        return -1;
    }
    /**
     * returns the last set bits index
     * @return 
     */
    public int last()
    {
        for (int ii=array.length-1;ii>=0;ii--)
        {
            if (array[ii] != 0)
            {
                for (int jj=Math.min(bits,(ii+1)*8)-1;jj>=0;jj--)
                {
                    if (isSet(jj))
                    {
                        return jj;
                    }
                }
            }
        }
        return -1;
    }
    /**
     * For each set bet index.
     * @param consumer 
     */
    public void forEach(IntConsumer consumer)
    {
        for (int ii=0;ii<array.length;ii++)
        {
            if (array[ii] != 0)
            {
                int lim = Math.min(bits, (ii+1)*8);
                for (int jj=ii*8;jj<lim;jj++)
                {
                    if (isSet(jj))
                    {
                        consumer.accept(jj);
                    }
                }
            }
        }
    }
    /**
     * Returns set bit indexes as stream
     * @return 
     */
    public IntStream stream()
    {
        IntStream.Builder builder = IntStream.builder();
        forEach(builder);
        return builder.build();
    }
    /**
     * Returns number of set bits
     * @return 
     */
    public int count()
    {
        int count = 0;
        for (int ii=bits-1;ii>=0;ii--)
        {
            if (isSet(ii))
            {
                count++;
            }
        }
        return count;
    }
    /**
     * Returns true if bit in this and other is set in any same index.
     * @param other
     * @return 
     */
    public boolean and(BitArray other)
    {
        if (bits != other.bits)
        {
            throw new IllegalArgumentException("number of bits differ");
        }
        int l = bits/8;
        for (int ii=0;ii<l;ii++)
        {
            if ((array[ii] & other.array[ii]) != 0)
            {
                return true;
            }
        }
        for (int ii=l*8;ii<bits;ii++)
        {
            if (isSet(ii) && other.isSet(ii))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + this.bits;
        hash = 79 * hash + Arrays.hashCode(this.array);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final BitArray other = (BitArray) obj;
        if (this.bits != other.bits)
        {
            return false;
        }
        if (!Arrays.equals(this.array, other.array))
        {
            return false;
        }
        return true;
    }
    /**
     * Returns backing array
     * @return 
     */
    public byte[] getArray()
    {
        return array;
    }

    private void check(int index)
    {
        if (index <0 || index >= bits)
        {
            throw new IllegalArgumentException(index+" is out of range");
        }
    }
    
    @Override
    public int length()
    {
        return bits;
    }

    @Override
    public char charAt(int index)
    {
        if (isSet(index))
        {
            return '1';
        }
        else
        {
            return '0';
        }
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
        return toString().subSequence(start, end);
    }

    @Override
    public String toString()
    {
        return new StringBuilder(this).toString();
    }
    
}
