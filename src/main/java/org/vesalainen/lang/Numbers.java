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
                    if (Character.isDigit(cp))
                    {
                        int digit = Character.digit(cp, 10);
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
                    if (Character.isDigit(cp))
                    {
                        int digit = Character.digit(cp, 10);
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
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
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
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            result -= Character.digit(cp, radix);
            if (result > 0)
            {
                throw new NumberFormatException("too long "+cs);
            }
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (result == Integer.MIN_VALUE && sign == -1)
        {
            throw new NumberFormatException("too long "+cs);
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
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
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
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            result -= Character.digit(cp, radix);
            if (result > 0)
            {
                throw new NumberFormatException("too long "+cs);
            }
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (result == Long.MIN_VALUE && sign == -1)
        {
            throw new NumberFormatException("too long "+cs);
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
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
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
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            result -= Character.digit(cp, radix);
            if (result > 0)
            {
                throw new NumberFormatException("too long "+cs);
            }
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (result == Short.MIN_VALUE && sign == -1)
        {
            throw new NumberFormatException("too long "+cs);
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
        int length = cs.length();
        boolean twoComp = false;
        if (radix < 0)
        {
            twoComp = true;
            radix = -radix;
        }
        byte result = 0;
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
        while (index < length)
        {
            result *= radix;
            cp = Character.codePointAt(cs, index);
            result -= Character.digit(cp, radix);
            if (result > 0)
            {
                throw new NumberFormatException("too long "+cs);
            }
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (result == Byte.MIN_VALUE && sign == -1)
        {
            throw new NumberFormatException("too long "+cs);
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
}
