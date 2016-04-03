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
package org.vesalainen.util;

import java.util.function.IntPredicate;

/**
 * A Utility class that contains helper methods for implementing CharSequence.
 * @author tkv
 */
public class CharSequences
{
    /**
     * Converts CharSequence to String
     * @param seq
     * @return 
     * @see java.lang.Object#toString() 
     */
    public static String toString(CharSequence seq)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(seq);
        return sb.toString();
    }
    /**
     * Returns index of pattern or -1 if pattern not found
     * @param seq
     * @param pattern
     * @return 
     * @see java.lang.String#indexOf(java.lang.String) 
     */
    public static int indexOf(CharSequence seq, CharSequence pattern)
    {
        return indexOf(seq, pattern, 0);
    }
    /**
     * Returns index of pattern, starting at fromIndex, or -1 if pattern not found
     * @param seq
     * @param pattern
     * @param fromIndex
     * @return 
     * @see java.lang.String#indexOf(java.lang.String, int) 
     */
    public static int indexOf(CharSequence seq, CharSequence pattern, int fromIndex)
    {
        int len = seq.length();
        int pi=0;
        int pl=pattern.length();
        int ll = len-pl;
        for (int ii=fromIndex;ii<len;ii++)
        {
            if (seq.charAt(ii) == pattern.charAt(pi))
            {
                pi++;
                if (pi == pl)
                {
                    return ii-pl+1;
                }
            }
            else
            {
                if (ii > ll)
                {
                    return -1;
                }
                pi=0;
            }
        }
        return -1;
    }
    /**
     * Returns index of char or -1 if char not found
     * @param seq
     * @param c
     * @return 
     * @see java.lang.String#indexOf(int) 
     */
    public static int indexOf(CharSequence seq, char c)
    {
        return indexOf(seq, (x)->{return x==c;});
    }
    /**
     * Returns index on char where p matches
     * @param seq
     * @param p
     * @return 
     */
    public static int indexOf(CharSequence seq, IntPredicate p)
    {
        return indexOf(seq, p, 0);
    }
    /**
     * Returns index of char, starting from fromIndex, or -1 if char not found
     * @param seq
     * @param c
     * @param fromIndex
     * @see java.lang.String#indexOf(int, int) 
     * @return 
     */
    public static int indexOf(CharSequence seq, char c, int fromIndex)
    {
        return indexOf(seq, (x)->{return x==c;}, fromIndex);
    }
    /**
     * Returns index on char where p matches
     * @param seq
     * @param p
     * @param fromIndex
     * @return 
     */
    public static int indexOf(CharSequence seq, IntPredicate p, int fromIndex)
    {
        int len = seq.length();
        for (int ii=fromIndex;ii<len;ii++)
        {
            if (p.test(seq.charAt(ii)))
            {
                return ii;
            }
        }
        return -1;
    }
    /**
     * Return true if cs1 and cs2 are same object or if their length and content
     * equals.
     * @param cs1
     * @param cs2
     * @return 
     * @see java.lang.Object#equals(java.lang.Object) 
     */
    public static boolean equals(CharSequence cs1, Object other)
    {
        if (cs1 == other)
        {
            return true;
        }
        if (other instanceof CharSequence)
        {
            CharSequence cs2 = (CharSequence) other;
            if (cs1.length() != cs2.length())
            {
                return false;
            }
            int len = cs1.length();
            for (int ii=0;ii<len;ii++)
            {
                if (cs1.charAt(ii) != cs2.charAt(ii))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    /**
     * Calculates hashCode for CharSequence
     * @param seq
     * @return 
     * @see java.lang.Object#hashCode() 
     */
    public static int hashCode(CharSequence seq)
    {
        int len = seq.length();
        int hash = len;
        for (int ii=0;ii<6;ii++)
        {
            hash *= seq.charAt(hash%len);
            if (hash <= 0)
            {
                break;
            }
        }
        return hash;
    }
    /**
     * Returns String backed CharSequence implementation. These CharSequences
     * use same equals and hashCode as ByteBufferCharSequence and can be used together
     * in same HashMap, for example.
     * @param str
     * @return 
     * @see org.vesalainen.nio.ByteBufferCharSequenceFactory
     */
    public static CharSequence getConstant(String str)
    {
        return new SeqConstant(str);
    }
    private static class SeqConstant implements CharSequence
    {
        private final String str;

        private SeqConstant(String str)
        {
            this.str = str;
        }

        @Override
        public int length()
        {
            return str.length();
        }

        @Override
        public char charAt(int index)
        {
            return str.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end)
        {
            return str.subSequence(start, end);
        }

        @Override
        public String toString()
        {
            return str;
        }

        @Override
        public int hashCode()
        {
            return CharSequences.hashCode(str);
        }

        @Override
        public boolean equals(Object obj)
        {
            return CharSequences.equals(str, obj);
        }

    }
}
