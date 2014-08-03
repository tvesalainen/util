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

/**
 * Numbers class contains number parsing methods parsing numbers from CharSequence
 * rather that String. This is the main difference to parsing methods in Integer,
 * Float, ...
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
public class Numbers
{
    private static final int IntLimit = Integer.MAX_VALUE/10-10;
    private static final long LongLimit = Long.MAX_VALUE/10-10;

    private enum FloatState {Significand, Decimal, Exponent};
    
    public static boolean parseBoolean(CharSequence cs)
    {
        return 
                cs.length() == 4 &&
                Character.codePointCount(cs, 0, 4) == 4 &&
                Character.toUpperCase(Character.codePointAt(cs, 0)) == 'T' &&
                Character.toUpperCase(Character.codePointAt(cs, 0)) == 'R' &&
                Character.toUpperCase(Character.codePointAt(cs, 0)) == 'U' &&
                Character.toUpperCase(Character.codePointAt(cs, 0)) == 'E';
    }
    public static boolean parseBoolean(CharSequence cs, int radix)
    {
        if (radix != 2)
        {
            throw new IllegalArgumentException("radix must be 2");
        }
        if (cs.length() != 1)
        {
            throw new IllegalArgumentException("input length must be 1");
        }
        if (Character.codePointCount(cs, 0, 4) != 1)
        {
            throw new IllegalArgumentException("input length must be 1");
        }
        return 
                Character.digit(Character.codePointAt(cs, 0), 2) == 1;
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
    public static float parseFloat(CharSequence cs)
    {
        FloatState fs = FloatState.Significand;
        int length = cs.length();
        int significand = 0;
        int sign = 1;
        int index = 0;
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
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        while (index < length)
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
                        if (!overFlow && significand > IntLimit)
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
                        throw new NumberFormatException("no float "+cs);
                    }
                    break;
                case '.':
                    if (fs != FloatState.Significand)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    fs = FloatState.Decimal;
                    break;
                case 'e':
                case 'E':
                    if (fs == FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    fs = FloatState.Exponent;
                    break;
                case '-':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    exponentSign = -1;
                    break;
                case '+':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
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
    public static double parseDouble(CharSequence cs)
    {
        FloatState fs = FloatState.Significand;
        int length = cs.length();
        long significand = 0;
        int sign = 1;
        int index = 0;
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
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        while (index < length)
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
                        if (!overFlow && significand > LongLimit)
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
                        throw new NumberFormatException("no float "+cs);
                    }
                    break;
                case '.':
                    if (fs != FloatState.Significand)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    fs = FloatState.Decimal;
                    break;
                case 'e':
                case 'E':
                    if (fs == FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    fs = FloatState.Exponent;
                    break;
                case '-':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
                    }
                    exponentSign = -1;
                    break;
                case '+':
                    if (fs != FloatState.Exponent)
                    {
                        throw new NumberFormatException("cannot convert "+cs+" to float");
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
    public static int parseInt(CharSequence cs)
    {
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
    public static int parseInt(CharSequence cs, int radix)
    {
        int size = Integer.SIZE;
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size);
        }
        else
        {
            check(cs, radix, NumberRanges.IntRange);
        }
        int result = 0;
        int sign = -1;
        int index = 0;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            sign = 1;
            index++;
        }
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        int count = Character.codePointCount(cs, index, length);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs);
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
    public static long parseLong(CharSequence cs)
    {
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
    public static long parseLong(CharSequence cs, int radix)
    {
        int size = Long.SIZE;
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size);
        }
        else
        {
            check(cs, radix, NumberRanges.LongRange);
        }
        long result = 0;
        int sign = -1;
        int index = 0;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            sign = 1;
            index++;
        }
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        int count = Character.codePointCount(cs, index, length);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs);
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
    public static short parseShort(CharSequence cs)
    {
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
    public static short parseShort(CharSequence cs, int radix)
    {
        int size = Short.SIZE;
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size);
        }
        else
        {
            check(cs, radix, NumberRanges.ShortRange);
        }
        short result = 0;
        int sign = -1;
        int index = 0;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            sign = 1;
            index++;
        }
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        int count = Character.codePointCount(cs, index, length);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs);
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
    public static byte parseByte(CharSequence cs)
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
    public static byte parseByte(CharSequence cs, int radix)
    {
        int size = Byte.SIZE;
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
            check(cs, size);
        }
        else
        {
            check(cs, radix, NumberRanges.ByteRange);
        }
        short result = 0;
        int sign = -1;
        int index = 0;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            index++;
        }
        if (cp == '-')
        {
            if (twoComp)
            {
                throw new NumberFormatException("no signs for 2-complement "+cs);
            }
            sign = 1;
            index++;
        }
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        int count = Character.codePointCount(cs, index, length);
        if (count == size)
        {
            twoComp = false;
        }
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs);
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
    public static int parseUnsignedInt(CharSequence cs)
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
    public static int parseUnsignedInt(CharSequence cs, int radix)
    {
        check(cs, radix, NumberRanges.UnsignedIntRange);
        int length = cs.length();
        int result = 0;
        int index = 0;
        int cp = Character.codePointAt(cs, index);
        if (cp == '+')
        {
            index++;
        }
        if (index >= length)
        {
            throw new NumberFormatException("unparsable number "+cs);
        }
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            int digit = Character.digit(cp, radix);
            if (digit == -1)
            {
                throw new NumberFormatException("unparsable number "+cs);
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
    private static void check(CharSequence cs, int radix, CharSequence[][] range)
    {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
        {
            throw new NumberFormatException(cs+" radix "+radix+" not supported");
        }
        CharSequence lower = range[radix][0];
        CharSequence upper = range[radix][1];
        int sign = 1;
        int index = 0;
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
        int count = Character.codePointCount(cs, index, cs.length());
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
                throw new NumberFormatException(cs+" not in range["+lower+" - "+upper+"]");
            }
            if (count+1 > lower.length())
            {
                throw new NumberFormatException(cs+" not in range["+lower+" - "+upper+"]");
            }
            if (count+1 == lower.length() && isGreater(cs, radix, lower))
            {
                throw new NumberFormatException(cs+" not in range["+lower+" - "+upper+"]");
            }
        }
        else
        {
            if (count > upper.length())
            {
                throw new NumberFormatException(cs+" not in range["+lower+" - "+upper+"]");
            }
            if (count == upper.length() && isGreater(cs, radix, upper))
            {
                throw new NumberFormatException(cs+" not in range["+lower+" - "+upper+"]");
            }
        }
    }
    private static boolean isGreater(CharSequence cs, int radix, CharSequence range)
    {
        int len = cs.length();
        int count = Character.codePointCount(cs, 0, len);
        int ii = 0;
        int jj = 0;
        switch (cs.charAt(0))
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
                throw new NumberFormatException(cs+"not valid number");
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
    private static void check(CharSequence cs, int size)
    {
        int len = cs.length();
        int index = 0;
        int count = Character.codePointCount(cs, index, cs.length());
        if (count > size)
        {
            throw new NumberFormatException(cs+"not valid number");
        }
    }
}
