/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.util.function.Funcs;
import org.vesalainen.util.function.IntBiPredicate;
import org.vesalainen.util.stream.Streams;

/**
 * A Utility class that contains helper methods for CharSequences.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CharSequences
{
    /**
     * Compares two CharSequences
     * @param s1
     * @param s2
     * @return 
     */
    public static int compare(CharSequence s1, CharSequence s2)
    {
        return compare(s1, s2, Funcs::same);
    }
    /**
     * Compares two CharSequences with codepoint mapping
     * @param s1
     * @param s2
     * @param op Codepoint mapping before comparison.
     * @return 
     */
    public static int compare(CharSequence s1, CharSequence s2, IntUnaryOperator op)
    {
        return Streams.compare(s1.codePoints().map(op), s2.codePoints().map(op));
    }
    /**
     * Return true if seq start match pattern exactly.
     * @param seq
     * @param pattern
     * @return 
     */
    public static boolean startsWith(CharSequence seq, CharSequence pattern)
    {
        return startsWith(seq, pattern, Funcs::same);
    }
    /**
     * Return true if seq start match pattern after both characters have been converted
     * with op.
     * @param seq
     * @param pattern
     * @param op
     * @return 
     */
    public static boolean startsWith(CharSequence seq, CharSequence pattern, IntUnaryOperator op)
    {
        if (pattern.length() > seq.length())
        {
            return false;
        }
        int length = pattern.length();
        for (int ii=0;ii<length;ii++)
        {
            if (op.applyAsInt(seq.charAt(ii)) != op.applyAsInt(pattern.charAt(ii)))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Return true if seq end match pattern exactly.
     * @param seq
     * @param pattern
     * @return 
     */
    public static boolean endsWith(CharSequence seq, CharSequence pattern)
    {
        return endsWith(seq, pattern, Funcs::same);
    }
    /**
     * Return true if seq end match pattern after both characters have been converted
     * @param seq
     * @param pattern
     * @param op
     * @return 
     */
    public static boolean endsWith(CharSequence seq, CharSequence pattern, IntUnaryOperator op)
    {
        if (pattern.length() > seq.length())
        {
            return false;
        }
        int ls = seq.length();
        int lp = pattern.length();
        for (int ii=1;ii<=lp;ii++)
        {
            if (op.applyAsInt(seq.charAt(ls-ii)) != op.applyAsInt(pattern.charAt(lp-ii)))
            {
                return false;
            }
        }
        return true;
    }
    public static boolean startsWith(CharSequence seq, char cc)
    {
        return startsWith(seq, cc, (c)->{return c==cc;});
    }
    public static boolean startsWith(CharSequence seq, char cc, IntPredicate p)
    {
        return p.test(seq.charAt(0));
    }
    public static boolean endsWith(CharSequence seq, char cc)
    {
        return endsWith(seq, cc, (c)->{return c==cc;});
    }
    public static boolean endsWith(CharSequence seq, char cc, IntPredicate p)
    {
        return p.test(seq.charAt(seq.length()-1));
    }
    /**
     * Returns CharSequence where white spaces are removed from start and end
     * or return same if nothing to trim.
     * @param seq
     * @return 
     * @see java.lang.Character#isWhitespace(int) 
     */
    public static CharSequence trim(CharSequence seq)
    {
        return trim(seq, Character::isWhitespace);
    }
    /**
     * Returns CharSequence where trimming is done by p.
     * @param seq
     * @param p
     * @return 
     */
    public static CharSequence trim(CharSequence seq, IntPredicate p)
    {
        int length = seq.length();
        int start = 0;
        while (start < length && p.test(seq.charAt(start)))
        {
            start++;
        }
        int end = seq.length()-1;
        while (end > start && p.test(seq.charAt(end)))
        {
            end--;
        }
        end++;
        if (end - start == length)
        {
            return seq;
        }
        else
        {
            return seq.subSequence(start, end);
        }
    }
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
    public static int indexOf(CharSequence seq, CharSequence pattern, IntBiPredicate p)
    {
        return indexOf(seq, pattern, (int a, int b)->{return a==b;}, 0);
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
        return indexOf(seq, pattern, (int a, int b)->{return a==b;}, fromIndex);
    }
    public static int indexOf(CharSequence seq, CharSequence pattern, IntBiPredicate p, int fromIndex)
    {
        int len = seq.length();
        int pi=0;
        int pl=pattern.length();
        int ll = len-pl;
        for (int ii=fromIndex;ii<len;ii++)
        {
            if (p.test(seq.charAt(ii), pattern.charAt(pi)))
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
     * Returns last index of char or -1 if char not found
     * @param seq
     * @param c
     * @return 
     * @see java.lang.String#indexOf(int) 
     */
    public static int lastIndexOf(CharSequence seq, char c)
    {
        return lastIndexOf(seq, (x)->{return x==c;});
    }
    /**
     * Returns last index on char where p matches
     * @param seq
     * @param p
     * @param fromIndex
     * @return 
     */
    public static int lastIndexOf(CharSequence seq, IntPredicate p)
    {
        int len = seq.length();
        for (int ii=len-1;ii>=0;ii--)
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
        return equals(cs1, other, Funcs::same);
    }
    /**
     * Return true if cs1 and cs2 are same object or if their length and content
     * @param cs1
     * @param other
     * @param op An operator for converting characters in equals and hashCode.
     * Default implementation is identity. Using e.g. Character::toLowerCase
     * implements case insensitive equals and hashCode.
     * @return 
     */
    public static boolean equals(CharSequence cs1, Object other, IntUnaryOperator op)
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
                if (op.applyAsInt(cs1.charAt(ii)) != op.applyAsInt(cs2.charAt(ii)))
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
        return hashCode(seq, (x)->{return x;});
    }
    /**
     * Calculates hashCode for CharSequence
     * @param seq
     * @param op An operator for converting characters in equals and hashCode.
     * Default implementation is identity. Using e.g. Character::toLowerCase
     * implements case insensitive equals and hashCode.
     * @return 
     */
    public static int hashCode(CharSequence seq, IntUnaryOperator op)
    {
        int len = seq.length();
        int hash = len;
        for (int ii=0;ii<6;ii++)
        {
            hash *= op.applyAsInt(seq.charAt(hash%len));
            if (hash <= 0)
            {
                break;
            }
        }
        return hash;
    }
    /**
     * Returns String backed CharSequence concatenation. These CharSequences
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
    /**
     * Returns concatenated CharSequnce constructed by toString methods. 
     * @param ob
     * @param obs
     * @return 
     */
    public static CharSequence concat(Object ob, Object... obs)
    {
        return concat((x)->{return x;}, ob, obs);
    }
    /**
     * Returns concatenated CharSequnce constructed by toString methods. 
     * @param op
     * @param ob
     * @param obs
     * @return 
     */
    public static CharSequence concat(IntUnaryOperator op, Object ob, Object... obs)
    {
        if (obs.length == 0)
        {
            return new SeqConstant(ob.toString(), op);
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(ob.toString());
            for (Object o : obs)
            {
                sb.append(o.toString());
            }
            return new SeqConstant(sb.toString(), op);
        }
    }
    /**
     * Returns String backed CharSequence implementation. These CharSequences
     * use same equals and hashCode as ByteBufferCharSequence and can be used together
     * in same HashMap, for example if op's equals.
     * @param str
     * @param op An operator for converting characters in equals and hashCode.
     * Default implementation is identity. Using e.g. Character::toLowerCase
     * implements case insensitive equals and hashCode.
     * @return 
     */
    public static CharSequence getConstant(String str, IntUnaryOperator op)
    {
        return new SeqConstant(str, op);
    }
    private static class SeqConstant implements CharSequence
    {
        private final String str;
        private final IntUnaryOperator op;

        private SeqConstant(String str)
        {
            this(str, (x)->{return x;});
        }
        private SeqConstant(String str, IntUnaryOperator op)
        {
            this.str = str;
            this.op = op;
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
            return CharSequences.hashCode(str, op);
        }

        @Override
        public boolean equals(Object obj)
        {
            return CharSequences.equals(str, obj, op);
        }

    }
    /**
     * Returns stream splitted with character
     * @param seq
     * @param cc
     * @return 
     */
    public static Stream<CharSequence> split(CharSequence seq, char cc)
    {
        if (seq.length() == 0)
        {
            return Stream.empty();
        }
        return StreamSupport.stream(new SpliteratorImpl(seq, (x)->{return x==cc;}), false);
    }
    /**
     * Returns stream splitted with function
     * @param seq
     * @param p
     * @return 
     */
    public static Stream<CharSequence> split(CharSequence seq, IntPredicate p)
    {
        if (seq.length() == 0)
        {
            return Stream.empty();
        }
        return StreamSupport.stream(new SpliteratorImpl(seq, p), false);
    }
    private static class SpliteratorImpl implements Spliterator<CharSequence>
    {
        private final CharSequence seq;
        private final IntPredicate p;
        private final int length;
        private int start;
        private int end;

        public SpliteratorImpl(CharSequence seq, IntPredicate p)
        {
            this.seq = seq;
            this.p = p;
            this.length = seq.length();
        }
        
        @Override
        public boolean tryAdvance(Consumer<? super CharSequence> action)
        {
            if (start > length)
            {
                return false;
            }
            if (start == length)
            {
                if (p.test(seq.charAt(start-1)))    // ends with delim
                {
                    action.accept(seq.subSequence(start-1, start-1));
                    start++;
                    return true;
                }
                return false;
            }
            end = start;
            while (end < length && !p.test(seq.charAt(end)))
            {
                end++;
            }
            action.accept(seq.subSequence(start, end));
            start = end+1;
            return true;
        }

        @Override
        public Spliterator<CharSequence> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return 1;
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
    /**
     * Creates a CharSequence from a file with ASCII content.
     * @param file
     * @return
     * @throws IOException 
     */
    public static final CharSequence getAsciiCharSequence(File file) throws IOException
    {
        return new ASCIICharSequence(file);
    }
    /**
     * Creates a CharSequence from path with ASCII content
     * @param path
     * @return
     * @throws IOException 
     */
    public static final CharSequence getAsciiCharSequence(Path path) throws IOException
    {
        return new ASCIICharSequence(path);
    }
    /**
     * Creates a CharSequence from a ByteBuffer. ByteBuffer must support array.
     * @param bb
     * @return
     * @throws IOException 
     */
    public static final CharSequence getAsciiCharSequence(ByteBuffer bb) throws IOException
    {
        return new ASCIICharSequence(bb);
    }
    /**
     * Creates a CharSequence from byte buffer.
     * @param buf
     * @return 
     */
    public static final CharSequence getAsciiCharSequence(byte[] buf)
    {
        return new ASCIICharSequence(buf);
    }
    /**
     * Creates a CharSequence from byte buffer.
     * @param buf
     * @param offset
     * @param length
     * @return 
     */
    public static final CharSequence getAsciiCharSequence(byte[] buf, int offset, int length)
    {
        return new ASCIICharSequence(buf, offset, length);
    }
    private static class ASCIICharSequence implements CharSequence
    {
        private byte[] buf;
        private int offset;
        private int length;

        public ASCIICharSequence(File file) throws IOException
        {
            this(file.toPath());
        }

        public ASCIICharSequence(Path path) throws IOException
        {
            this(Files.readAllBytes(path));
        }

        public ASCIICharSequence(ByteBuffer bb)
        {
            this(bb.array(), bb.arrayOffset(), bb.capacity());
        }

        public ASCIICharSequence(byte[] buf)
        {
            this(buf, 0, buf.length);
        }

        public ASCIICharSequence(byte[] buf, int offset, int length)
        {
            this.buf = buf;
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public int length()
        {
            return length;
        }

        @Override
        public char charAt(int index)
        {
            if (index < 0 || index >= length)
            {
                throw new IllegalArgumentException("index "+index+" out of range");
            }
            return (char) buf[index+offset];
        }

        @Override
        public CharSequence subSequence(int start, int end)
        {
            return new ASCIICharSequence(buf, offset+start, end-start);
        }

        @Override
        public String toString()
        {
            return new String(buf, offset, length, US_ASCII);
        }
        
    }
}
