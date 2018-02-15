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

package org.vesalainen.lang;

import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.vesalainen.util.CharSequences;

/**
 * Numbers class contains number parsing methods parsing numbers from CharSequence
 * rather that String. This is the main difference to parsing methods in Integer,
 * Float, ...
 * <p>
 * Additionally there are more useful methods.
 * 
 * <p>For most numbers, methods in this class return the same as methods in 
 * java.lang. 
 * 
 * @see java.lang.Byte
 * @see java.lang.Short
 * @see java.lang.Integer
 * @see java.lang.Long
 * @see java.lang.Float
 * @see java.lang.Double
 * @author Timo Vesalainen
 */
public class Primitives
{
    private static final int INT_LIMIT = Integer.MAX_VALUE/10-10;
    private static final long LONG_LIMIT = Long.MAX_VALUE/10-10;
    /**
     * Returns IntStream where each item is a digit. I.e zero leading digits.
     * @param v
     * @param length
     * @param radix
     * @return 
     */
    public static final IntStream toDigits(long v, int length, int radix)
    {
        Spliterator.OfInt spliterator = Spliterators.spliterator(new PRimIt(v, length, radix), Long.MAX_VALUE, 0);
        return StreamSupport.intStream(spliterator, false);
    }
    private static class PRimIt implements PrimitiveIterator.OfInt
    {
        private long v;
        private int radix;
        private long div;

        public PRimIt(long v, int length, int radix)
        {
            this.v = v;
            this.radix = radix;
            div = Casts.castLong(Math.pow(radix, length-1));
            if (Long.divideUnsigned(v, div) >= div)
            {
                throw new IllegalArgumentException(v+" doesn't fit to length="+length);
            }
        }
        
        @Override
        public int nextInt()
        {
            long d = Long.divideUnsigned(v, div);
            div /= radix;
            return Character.forDigit((int) Long.remainderUnsigned(d, radix), radix);
        }

        @Override
        public boolean hasNext()
        {
            return div > 0;
        }
    }
    private enum FloatState {Significand, Decimal, Exponent};
    /**
     * Write value to byte array
     * @param value
     * @return 
     * @see java.nio.ByteBuffer#putFloat(float) 
     */
    public static final byte[] writeFloat(float value)
    {
        byte[] array = new byte[4];
        writeFloat(value, array);
        return array;
    }
    /**
     * Write value to byte array
     * @param value
     * @param array 
     * @see java.nio.ByteBuffer#putFloat(float) 
     */
    public static final void writeFloat(float value, byte[] array)
    {
        writeFloat(value, array, 0);
    }
    /**
     * Write value to byte array
     * @param value
     * @param array
     * @param offset 
     * @see java.nio.ByteBuffer#putFloat(float) 
     */
    public static final void writeFloat(float value, byte[] array, int offset)
    {
        writeInt(Float.floatToRawIntBits(value), array, offset);
    }
    /**
     * Read value from byte array
     * @param array
     * @return 
     * @see java.nio.ByteBuffer#getFloat() 
     */
    public static final float readFloat(byte[] array)
    {
        return Float.intBitsToFloat(readInt(array, 0));
    }
    /**
     * Read value from byte array
     * @param array
     * @param offset
     * @return 
     * @see java.nio.ByteBuffer#getFloat() 
     */
    public static final float readFloat(byte[] array, int offset)
    {
        return Float.intBitsToFloat(readInt(array, offset));
    }
    /**
     * Write value to byte array
     * @param value
     * @return 
     * @see java.nio.ByteBuffer#putDouble(double) 
     */
    public static final byte[] writeDouble(double value)
    {
        byte[] array = new byte[8];
        writeDouble(value, array);
        return array;
    }
    /**
     * Write value to byte array
     * @param value
     * @param array 
     * @see java.nio.ByteBuffer#putDouble(double) 
     */
    public static final void writeDouble(double value, byte[] array)
    {
        writeDouble(value, array, 0);
    }
    /**
     * Write value to byte array
     * @param value
     * @param array
     * @param offset 
     * @see java.nio.ByteBuffer#putDouble(double) 
     */
    public static final void writeDouble(double value, byte[] array, int offset)
    {
        writeLong(Double.doubleToRawLongBits(value), array, offset);
    }
    /**
     * Read value from byte array
     * @param array
     * @return 
     * @see java.nio.ByteBuffer#getDouble() 
     */
    public static final double readDouble(byte[] array)
    {
        return readDouble(array, 0);
    }
    /**
     * Read value from byte array
     * @param array
     * @param offset
     * @see java.nio.ByteBuffer#getDouble() 
     * @return 
     */
    public static final double readDouble(byte[] array, int offset)
    {
        return Double.longBitsToDouble(readLong(array, offset));
    }
    /**
     * Write value to byte array
     * @param value
     * @return 
     * @see java.nio.ByteBuffer#putLong(long) 
     */
    public static final byte[] writeLong(long value)
    {
        byte[] array = new byte[8];
        writeLong(value, array);
        return array;
    }
    /**
     * Write value to byte array
     * @param value
     * @param array 
     * @see java.nio.ByteBuffer#putLong(long) 
     */
    public static final void writeLong(long value, byte[] array)
    {
        writeLong(value, array, 0);
    }
    /**
     * Write value to byte array
     * @param value
     * @param array
     * @param offset 
     * @see java.nio.ByteBuffer#putLong(long) 
     */
    public static final void writeLong(long value, byte[] array, int offset)
    {
        if (array.length < offset + 8)
        {
            throw new IllegalArgumentException("no room in array");
        }
        array[offset] = (byte) (value >> 56);
        array[offset + 1] = (byte) ((value >> 48) & 0xff);
        array[offset + 2] = (byte) ((value >> 40) & 0xff);
        array[offset + 3] = (byte) ((value >> 32) & 0xff);
        array[offset + 4] = (byte) ((value >> 24) & 0xff);
        array[offset + 5] = (byte) ((value >> 16) & 0xff);
        array[offset + 6] = (byte) ((value >> 8) & 0xff);
        array[offset + 7] = (byte) (value & 0xff);
    }
    /**
     * Read value from byte array
     * @param array
     * @return 
     * @see java.nio.ByteBuffer#getLong(long) 
     */
    public static final long readLong(byte[] array)
    {
        return readLong(array, 0);
    }
    /**
     * Read value from byte array
     * @param array
     * @param offset
     * @return 
     * @see java.nio.ByteBuffer#getLong(long) 
     */
    public static final long readLong(byte[] array, int offset)
    {
        if (array.length < offset + 8)
        {
            throw new IllegalArgumentException("no room in array");
        }
        return
                (((long)array[offset]<<56) & 0xff00000000000000L) +
                (((long)array[offset + 1]<<48) & 0xff000000000000L)  +
                (((long)array[offset + 2]<<40) & 0xff0000000000L)  +
                (((long)array[offset + 3]<<32) & 0xff00000000L)  +
                (((long)array[offset + 4]<<24) & 0xff000000L)  +
                (((long)array[offset + 5]<<16) & 0xff0000L)  +
                (((long)array[offset + 6]<<8) & 0xff00L)  +
                ((long)array[offset + 7] & 0xff );
    }
    /**
     * Write value to byte array
     * @param value
     * @return 
     * @see java.nio.ByteBuffer#putInt(int) 
     */
    public static final byte[] writeInt(int value)
    {
        byte[] array = new byte[4];
        writeInt(value, array);
        return array;
    }
    /**
     * Write value to byte array
     * @param value
     * @param array 
     * @see java.nio.ByteBuffer#putInt(int) 
     */
    public static final void writeInt(int value, byte[] array)
    {
        writeInt(value, array, 0);
    }
    /**
     * Write value to byte array
     * @param value
     * @param array
     * @param offset 
     * @see java.nio.ByteBuffer#putInt(int) 
     */
    public static final void writeInt(int value, byte[] array, int offset)
    {
        if (array.length < offset + 4)
        {
            throw new IllegalArgumentException("no room in array");
        }
        array[offset] = (byte) (value >> 24);
        array[offset + 1] = (byte) ((value >> 16) & 0xff);
        array[offset + 2] = (byte) ((value >> 8) & 0xff);
        array[offset + 3] = (byte) (value & 0xff);
    }
    /**
     * Read value from byte array
     * @param array
     * @return 
     * @see java.nio.ByteBuffer#getInt(int) 
     */
    public static final int readInt(byte[] array)
    {
        return readInt(array, 0);
    }
    /**
     * Read value from byte array
     * @param array
     * @param offset
     * @return 
     * @see java.nio.ByteBuffer#getInt(int) 
     */
    public static final int readInt(byte[] array, int offset)
    {
        if (array.length < offset + 4)
        {
            throw new IllegalArgumentException("no room in array");
        }
        return
                ((array[offset]<<24) & 0xff000000) +
                ((array[offset + 1]<<16) & 0xff0000)  +
                ((array[offset + 2]<<8) & 0xff00)  +
                (array[offset + 3] & 0xff );
    }
    /**
     * Write value to byte array
     * @param value
     * @return 
     * @see java.nio.ByteBuffer#putShort(short) 
     */
    public static final byte[] writeShort(short value)
    {
        byte[] array = new byte[2];
        writeShort(value, array);
        return array;
    }
    /**
     * Write value to byte array
     * @param value
     * @param array 
     * @see java.nio.ByteBuffer#putShort(short) 
     */
    public static final void writeShort(short value, byte[] array)
    {
        writeShort(value, array, 0);
    }
    /**
     * Write value to byte array
     * @param value
     * @param array
     * @param offset 
     * @see java.nio.ByteBuffer#putShort(short) 
     */
    public static final void writeShort(short value, byte[] array, int offset)
    {
        if (array.length < offset + 2)
        {
            throw new IllegalArgumentException("no room in array");
        }
        array[offset] = (byte) (value >> 8);
        array[offset + 1] = (byte) (value & 0xff);
    }
    /**
     * Read value from byte array
     * @param array
     * @return 
     * @see java.nio.ByteBuffer#getShort(short) 
     */
    public static final short readShort(byte[] array)
    {
        return readShort(array, 0);
    }
    /**
     * Read value from byte array
     * @param array
     * @param offset
     * @return 
     * @see java.nio.ByteBuffer#getShort(short) 
     */
    public static final short readShort(byte[] array, int offset)
    {
        if (array.length < offset + 2)
        {
            throw new IllegalArgumentException("no room in array");
        }
        return 
                (short) (((array[offset]<<8) & 0xff00) +
                (array[offset + 1] & 0xff ));
    }
    /**
     * Return -1 for negative value, 1 for positive value and 0 for 0
     * @param value
     * @return 
     */
    public static final int signum(int value)
    {
        return value < 0 ? -1 : value > 0 ? 1 : 0;
    }
    /**
     * Return -1 for negative value, 1 for positive value and 0 for 0
     * @param value
     * @return 
     */
    public static final int signum(long value)
    {
        return value < 0 ? -1 : value > 0 ? 1 : 0;
    }
    /**
     * Returns char from input.
     * @param cs
     * @return 
     * @throws IllegalArgumentException if input length is not 1.
     */
    public static final char parseChar(CharSequence cs)
    {
        return parseChar(cs, 0, cs.length());
    }
    /**
     * Returns char from input.
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws IllegalArgumentException if input length is not 1.
     */
    public static final char parseChar(CharSequence cs, int beginIndex, int endIndex)
    {
        if (endIndex - beginIndex != 1)
        {
            throw new IllegalArgumentException("input length must be 1");
        }
        return cs.charAt(beginIndex);
    }
    /**
     * Parses the char sequence argument as a boolean. The boolean returned represents 
     * the value true if the char sequence argument is not null and is equal, ignoring 
     * case, to the string "true".
     * @param cs
     * @return 
     * @throws IllegalArgumentException if input length is not 4.
     * @see java.lang.Boolean#parseBoolean(java.lang.String) 
     */
    public static final boolean parseBoolean(CharSequence cs)
    {
        return parseBoolean(cs, 0, cs.length());
    }
    /**
     * Parses the char sequence argument as a boolean. The boolean returned represents 
     * the value true if the char sequence argument is not null and is equal, ignoring 
     * case, to the string "true".
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws IllegalArgumentException if input length is not 4.
     * @see java.lang.Boolean#parseBoolean(java.lang.String) 
     */
    public static final boolean parseBoolean(CharSequence cs, int beginIndex, int endIndex)
    {
        return 
                endIndex - beginIndex == 4 &&
                Character.codePointCount(cs, beginIndex, endIndex) == 4 &&
                Character.toUpperCase(Character.codePointAt(cs, beginIndex)) == 'T' &&
                Character.toUpperCase(Character.codePointAt(cs, beginIndex+1)) == 'R' &&
                Character.toUpperCase(Character.codePointAt(cs, beginIndex+2)) == 'U' &&
                Character.toUpperCase(Character.codePointAt(cs, beginIndex+3)) == 'E';
    }
    /**
     * Parses the char sequence argument as a boolean. The boolean returned represents 
     * the value true if the char sequence argument is not null and it's digit value
     * is 1.
     * @param cs
     * @param radix Must be 2.
     * @return 
     * @throws IllegalArgumentException radix != 2 or if code point count != 1
     * or if input digit is not 0/1.
     * @see java.lang.Character#digit(int, int) 
     * @see java.lang.Character#codePointCount(java.lang.CharSequence, int, int) 
     */
    public static final boolean parseBoolean(CharSequence cs, int radix)
    {
        return parseBoolean(cs, radix, 0, cs.length());
    }
    /**
     * Parses the char sequence argument as a boolean. The boolean returned represents 
     * the value true if the char sequence argument is not null and it's digit value
     * is 1.
     * @param cs
     * @param radix Must be 2.
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws IllegalArgumentException radix != 2 or if code point count != 1
     * or if input digit is not 0/1.
     * @see java.lang.Character#digit(int, int) 
     * @see java.lang.Character#codePointCount(java.lang.CharSequence, int, int) 
     */
    public static final boolean parseBoolean(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        if (radix != 2)
        {
            throw new IllegalArgumentException("radix must be 2");
        }
        if (Character.codePointCount(cs, beginIndex, endIndex) != 1)
        {
            throw new IllegalArgumentException("input length must be 1");
        }
        int digit = Character.digit(Character.codePointAt(cs, beginIndex), 2);
        switch (digit)
        {
            case 1:
                return true;
            case 0:
                return false;
            default:
                throw new IllegalArgumentException("input must be 0/1");
        }
    }
    /**
     * Parses float from decimal floating point representation.
     * 
     * <p>Input can start with '-' or '+'.
     * <p>All numbers are decimals.
     * <p>Decimal separator is '.'
     * <p>Exponent separator is 'e' or 'E'
     * <p>Examples
     * <p>-1234.56
     * <p>+1234.56e12
     * <p>-1234.56E-12
     * @param cs
     * @return 
     * @see java.lang.Float#parseFloat(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * float.
     */
    public static final float parseFloat(CharSequence cs)
    {
        return parseFloat(cs, 0, cs.length());
    }
    /**
     * Parses float from decimal floating point representation.
     * 
     * <p>Input can start with '-' or '+'.
     * <p>All numbers are decimals.
     * <p>Decimal separator is '.'
     * <p>Exponent separator is 'e' or 'E'
     * <p>Examples
     * <p>-1234.56
     * <p>+1234.56e12
     * <p>-1234.56E-12
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @see java.lang.Float#parseFloat(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * float.
     */
    public static final float parseFloat(CharSequence cs, int beginIndex, int endIndex)
    {
        FloatState fs = FloatState.Significand;
        int end = endIndex;
        int significand = 0;
        int sign = 1;
        int index = beginIndex;
        int decimal = 0;
        int exponent = 0;
        int exponentSign = 1;
        boolean overFlow = false;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            index++;
        }
        if (cp == '-')
        {
            sign = -1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        while (index < end)
        {
            cp = Character.codePointAt(cs, index);
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
            switch (cp)
            {
                default:
                    int digit = Character.digit(cp, 10);
                    if (digit != -1)
                    {
                        if (!overFlow && significand > INT_LIMIT)
                        {
                            overFlow = true;
                        }
                        switch (fs)
                        {
                            case Significand:
                                if (!overFlow)
                                {
                                    significand *= 10;
                                    significand += digit;
                                }
                                else
                                {
                                    decimal++;
                                }
                                break;
                            case Decimal:
                                if (!overFlow)
                                {
                                    significand *= 10;
                                    significand += digit;
                                    decimal--;
                                }
                                break;
                            case Exponent:
                                exponent = 10*exponent + digit;
                                break;
                        }
                    }
                    else
                    {
                        throw new NumberFormatException("no float "+cs.subSequence(beginIndex, endIndex));
                    }
                    break;
                case '.':
                    if (fs != FloatState.Significand)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    fs = FloatState.Decimal;
                    break;
                case 'e':
                case 'E':
                    if (fs == FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    fs = FloatState.Exponent;
                    break;
                case '-':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    exponentSign = -1;
                    break;
                case '+':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    break;
            }
        }
        return (float) (sign * significand * Math.pow(10, exponentSign*exponent+decimal));
    }
    /**
     * Parses double from decimal floating point representation.
     * 
     * <p>Input can start with '-' or '+'.
     * <p>All number are decimals.
     * <p>Decimal separator is '.'
     * <p>Exponent separator is 'e' or 'E'
     * <p>Examples
     * <p>-1234.56
     * <p>+1234.56e12
     * <p>-1234.56E-12
     * @param cs
     * @return 
     * @see java.lang.Double#parseDouble(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * double.
     */
    public static final double parseDouble(CharSequence cs)
    {
        return parseDouble(cs, 0, cs.length());
    }
    /**
     * Parses double from decimal floating point representation.
     * 
     * <p>Input can start with '-' or '+'.
     * <p>All number are decimals.
     * <p>Decimal separator is '.'
     * <p>Exponent separator is 'e' or 'E'
     * <p>Examples
     * <p>-1234.56
     * <p>+1234.56e12
     * <p>-1234.56E-12
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @see java.lang.Double#parseDouble(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * double.
     */
    public static final double parseDouble(CharSequence cs, int beginIndex, int endIndex)
    {
        FloatState fs = FloatState.Significand;
        int end = endIndex;
        long significand = 0;
        int sign = 1;
        int index = beginIndex;
        int decimal = 0;
        int exponent = 0;
        int exponentSign = 1;
        boolean overFlow = false;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            index++;
        }
        if (cp == '-')
        {
            sign = -1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        while (index < end)
        {
            cp = Character.codePointAt(cs, index);
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
            switch (cp)
            {
                default:
                    int digit = Character.digit(cp, 10);
                    if (digit != -1)
                    {
                        if (!overFlow && significand > LONG_LIMIT)
                        {
                            overFlow = true;
                        }
                        switch (fs)
                        {
                            case Significand:
                                if (!overFlow)
                                {
                                    significand *= 10;
                                    significand += digit;
                                }
                                else
                                {
                                    decimal++;
                                }
                                break;
                            case Decimal:
                                if (!overFlow)
                                {
                                    significand *= 10;
                                    significand += digit;
                                    decimal--;
                                }
                                break;
                            case Exponent:
                                exponent = 10*exponent + digit;
                                break;
                        }
                    }
                    else
                    {
                        throw new NumberFormatException("no float "+cs.subSequence(beginIndex, endIndex));
                    }
                    break;
                case '.':
                    if (fs != FloatState.Significand)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    fs = FloatState.Decimal;
                    break;
                case 'e':
                case 'E':
                    if (fs == FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    fs = FloatState.Exponent;
                    break;
                case '-':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    exponentSign = -1;
                    break;
                case '+':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs.subSequence(beginIndex, endIndex)+" to float");
                    }
                    break;
            }
        }
        return sign * significand * Math.pow(10, exponentSign*exponent+decimal);
    }
    /**
     * Equal to calling parseInt(cs, 10).
     * @param cs
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseInt(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseInt(CharSequence cs)
    {
        if (CharSequences.startsWith(cs, "0b"))
        {
            return parseInt(cs, 2, 2, cs.length());
        }
        if (CharSequences.startsWith(cs, "0x"))
        {
            return parseInt(cs, 16, 2, cs.length());
        }
        return parseInt(cs, 10);
    }
    /**
     * Parses int from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseInt(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseInt(CharSequence cs, int radix)
    {
        return parseInt(cs, radix, 0, cs.length());
    }
    /**
     * Parses int from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseInt(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseInt(CharSequence cs, int beginIndex, int endIndex)
    {
        return parseInt(cs, 10, beginIndex, endIndex);
    }
    /**
     * Parses int from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseInt(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseInt(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        int size = Integer.SIZE;
        int end = endIndex;
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size, beginIndex, endIndex);
        }
        else
        {
            check(cs, radix, NumberRanges.IntRange, beginIndex, endIndex);
        }
        int result = 0;
        int sign = -1;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            sign = 1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        int count = Character.codePointCount(cs, index, end);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < end)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
            }
            result -= digit;
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (!twoComp || -result < (1<<(count-1)))
        {
            return sign*result;
        }
        else
        {
            return -result + (-1<<count);
        }
    }
    /**
     * Equal to calling parseLong(cs, 10).
     * @param cs
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * long.
     * @see java.lang.Long#parseLong(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final long parseLong(CharSequence cs)
    {
        if (CharSequences.startsWith(cs, "0b"))
        {
            return parseLong(cs, 2, 2, cs.length());
        }
        if (CharSequences.startsWith(cs, "0x"))
        {
            return parseLong(cs, 16, 2, cs.length());
        }
        return parseLong(cs, 10);
    }
    /**
     * Parses long from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * long.
     * @see java.lang.Long#parseLong(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final long parseLong(CharSequence cs, int radix)
    {
        return parseLong(cs, radix, 0, cs.length());
    }
    /**
     * Parses long from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * long.
     * @see java.lang.Long#parseLong(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final long parseLong(CharSequence cs, int beginIndex, int endIndex)
    {
        return parseLong(cs, 10, beginIndex, endIndex);
    }
    /**
     * Parses long from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * long.
     * @see java.lang.Long#parseLong(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final long parseLong(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        int size = Long.SIZE;
        int end = endIndex;
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size, beginIndex, endIndex);
        }
        else
        {
            check(cs, radix, NumberRanges.LongRange, beginIndex, endIndex);
        }
        long result = 0;
        int sign = -1;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            sign = 1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        int count = Character.codePointCount(cs, index, end);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < end)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
            }
            result -= digit;
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (!twoComp || -result < (1<<(count-1)))
        {
            return sign*result;
        }
        else
        {
            return -result + (-1<<count);
        }
    }
    /**
     * Equal to calling parseShort(cs, 10).
     * @param cs
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * short.
     * @see java.lang.Short#parseShort(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final short parseShort(CharSequence cs)
    {
        if (CharSequences.startsWith(cs, "0b"))
        {
            return parseShort(cs, 2, 2, cs.length());
        }
        if (CharSequences.startsWith(cs, "0x"))
        {
            return parseShort(cs, 16, 2, cs.length());
        }
        return parseShort(cs, 10);
    }
    /**
     * Parses short from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * short.
     * @see java.lang.Short#parseShort(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final short parseShort(CharSequence cs, int radix)
    {
        return parseShort(cs, radix, 0, cs.length());
    }
    /**
     * Parses short from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * short.
     * @see java.lang.Short#parseShort(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final short parseShort(CharSequence cs, int beginIndex, int endIndex)
    {
        return parseShort(cs, 10, beginIndex, endIndex);
    }
    /**
     * Parses short from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * short.
     * @see java.lang.Short#parseShort(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final short parseShort(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        int size = Short.SIZE;
        int end = endIndex;
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size, beginIndex, endIndex);
        }
        else
        {
            check(cs, radix, NumberRanges.ShortRange, beginIndex, endIndex);
        }
        short result = 0;
        int sign = -1;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            sign = 1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        int count = Character.codePointCount(cs, index, end);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < end)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
            }
            result -= digit;
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (!twoComp || -result < (1<<(count-1)))
        {
            return (short) (sign*result);
        }
        else
        {
            return (short) (-result + (-1<<count));
        }
    }
    /**
     * Equal to calling parseByte(cs, 10).
     * @param cs
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * byte.
     * @see java.lang.Byte#parseByte(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final byte parseByte(CharSequence cs)
    {
        return parseByte(cs, 10);
    }
    /**
     * Parses byte from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * byte.
     * @see java.lang.Byte#parseByte(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final byte parseByte(CharSequence cs, int radix)
    {
        return parseByte(cs, radix, 0, cs.length());
    }
    /**
     * Parses byte from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * byte.
     * @see java.lang.Byte#parseByte(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final byte parseByte(CharSequence cs, int beginIndex, int endIndex)
    {
        return parseByte(cs, 10, beginIndex, endIndex);
    }
    /**
     * Parses byte from input.
     * <p>Input can start with '-' or '+'.
     * <p>Numeric value is according to radix
     * <p>Radix can also be -2, where input is parsed as 2-complement binary string.
     * Input beginning with '1' is always negative. Eg. '111' == -1, '110' == -2
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * byte.
     * @see java.lang.Byte#parseByte(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final byte parseByte(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        int size = Byte.SIZE;
        int end = endIndex;
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size, beginIndex, endIndex);
        }
        else
        {
            check(cs, radix, NumberRanges.ByteRange, beginIndex, endIndex);
        }
        short result = 0;
        int sign = -1;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs.subSequence(beginIndex, endIndex));
            }
            sign = 1;
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        int count = Character.codePointCount(cs, index, end);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < end)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
            }
            result -= digit;
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (!twoComp || -result < (1<<(count-1)))
        {
            return (byte) (sign*result);
        }
        else
        {
            return (byte) (-result + (-1<<count));
        }
    }
    /**
     * Equal to calling parseUnsignedInt(cs, 10).
     * @param cs
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * unsigned int.
     * @see java.lang.Integer#parseUnsignedInt(java.lang.String) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseUnsignedInt(CharSequence cs)
    {
        return parseUnsignedInt(cs, 10);
    }
    /**
     * Parses unsigned int from input.
     * <p>Input can start with '+'.
     * <p>Numeric value is according to radix
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseUnsignedInt(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseUnsignedInt(CharSequence cs, int radix)
    {
        return parseUnsignedInt(cs, radix, 0, cs.length());
    }
    /**
     * Parses unsigned int from input.
     * <p>Input can start with '+'.
     * <p>Numeric value is according to radix
     * @param cs
     * @param radix A value between Character.MIN_RADIX and Character.MAX_RADIX or -2
     * @param beginIndex the index to the first char of the text range.
     * @param endIndex the index after the last char of the text range.
     * @return 
     * @throws java.lang.NumberFormatException if input cannot be parsed to proper
     * int.
     * @see java.lang.Integer#parseUnsignedInt(java.lang.String, int) 
     * @see java.lang.Character#digit(int, int) 
     */
    public static final int parseUnsignedInt(CharSequence cs, int radix, int beginIndex, int endIndex)
    {
        check(cs, radix, NumberRanges.UnsignedIntRange, beginIndex, endIndex);
        int end = endIndex;
        int result = 0;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            index++;
        }
        if (index >= end)
        {
            throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
        }
        while (index < end)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs.subSequence(beginIndex, endIndex));
            }
            result += digit;
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        return result;
    }
    /**
     * Returns true if character is 0-9, point (.), plus (+), minus (-), (e) or (E).
     * @param cc
     * @return 
     */
    public static final boolean isScientificDigit(int cc)
    {
        return isFloatDigit(cc) || cc == 'e' || cc == 'E';
    }
    /**
     * Returns true if character is 0-9, point (.), plus (+) or minus (-).
     * @param cc
     * @return 
     */
    public static final boolean isFloatDigit(int cc)
    {
        return isDecimalDigit(cc) || cc == '.';
    }
    /**
     * Returns true is character is 0-9
     * @param cc
     * @return 
     */
    public static final boolean isDecimalDigit(int cc)
    {
        switch (cc)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '+':
            case '-':
                return true;
            default:
                return false;
        }
    }
    /**
     * Returns true is character is 0-9, a-f or A-F
     * @param cc
     * @return 
     */
    public static final boolean isHexDigit(int cc)
    {
        switch (cc)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;
            default:
                return false;
        }
    }
    /**
     * Returns true is character is 0-7
     * @param cc
     * @return 
     */
    public static final boolean isOctalDigit(int cc)
    {
        switch (cc)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
                return true;
            default:
                return false;
        }
    }
    private static void check(CharSequence cs, int radix, CharSequence[][] range, int beginIndex, int endIndex)
    {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
        {
            throw new NumberFormatException(cs+" radix "+radix+" not supported");
        }
        CharSequence lower = range[radix][0];
        CharSequence upper = range[radix][1];
        int sign = 1;
        int index = beginIndex;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            index++;
        }
        if (cp == '-')
        {
            sign = -1;
            index++;
        }
        int count = Character.codePointCount(cs, index, endIndex);
        cp = Character.codePointAt(cs, index);
        int digit = Character.digit(cp, radix);
        while (digit == 0 && count > 1)
        {
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
            cp = Character.codePointAt(cs, index);
            digit = Character.digit(cp, radix);
            count--;
        }
        if (sign == -1)
        {
            if (Character.codePointAt(lower, 0) != '-')
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+" not in range["+lower+" - "+upper+"]");
            }
            if (count+1 > lower.length())
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+" not in range["+lower+" - "+upper+"]");
            }
            if (count+1 == lower.length() && isGreater(cs, radix, lower, beginIndex, endIndex))
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+" not in range["+lower+" - "+upper+"]");
            }
        }
        else
        {
            if (count > upper.length())
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+" not in range["+lower+" - "+upper+"]");
            }
            if (count == upper.length() && isGreater(cs, radix, upper, beginIndex, endIndex))
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+" not in range["+lower+" - "+upper+"]");
            }
        }
    }
    private static boolean isGreater(CharSequence cs, int radix, CharSequence range, int beginIndex, int endIndex)
    {
        int end = endIndex;
        int count = Character.codePointCount(cs, beginIndex, end);
        int ii = 0;
        int jj = 0;
        switch (cs.charAt(beginIndex))
        {
            case '-':
                ii = 1;
                jj = 1;
                break;
            case '+':
                ii = 1;
                break;
        }
        for (;ii<count;ii++)
        {
            int cp1 = Character.codePointAt(cs, jj);
            int d1 = Character.digit(cp1, radix);
            if (d1 == -1)
            {
                throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+"not valid number");
            }
            int cp2 = Character.codePointAt(range, ii);
            int d2 = Character.digit(cp2, radix);
            if (d1 > d2)
            {
                return true;
            }
            if (d1 < d2)
            {
                return false;
            }
            if (Character.isBmpCodePoint(cp1))
            {
                jj++;
            }
            else
            {
                jj += 2;
            }
        }
        return false;
    }
    private static void check(CharSequence cs, int size, int beginIndex, int endIndex)
    {
        int end = endIndex;
        int index = beginIndex;
        int count = Character.codePointCount(cs, index, end);
        if (count > size)
        {
            throw new NumberFormatException(cs.subSequence(beginIndex, endIndex)+"not valid number");
        }
    }
}
