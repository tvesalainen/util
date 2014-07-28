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
 *
 * @author Timo Vesalainen
 */
public class Numbers
{
    private static final int IntLimit = Integer.MAX_VALUE/10-10;
    private static final long LongLimit = Long.MAX_VALUE/10-10;
    private enum FloatState {Significand, Decimal, Exponent};
    
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
        int sign = 1;
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
            sign = -1;
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
            result += Character.digit(cp, radix);
            if (Character.isBmpCodePoint(cp))
            {
                index++;
            }
            else
            {
                index += 2;
            }
        }
        if (result < 0)
        {
            if (sign == -1)
            {
                sign = 1;
            }
            else
            {
                throw new NumberFormatException("too long "+cs);
            }
        }
        if (!twoComp || result < (1<<(count-1)))
        {
            return sign*result;
        }
        else
        {
            return result + (-1<<count);
        }
    }
}
