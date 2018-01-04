/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.morse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import static org.vesalainen.ham.morse.MorseCode.Part.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://en.wikipedia.org/wiki/Morse_code">Morse code</a>
 */
public class MorseCode implements AutoCloseable
{
    public static int SAMPLE_RATE = 10000;

    public enum Part {DAH, DI};
    public static final Map<Character,Part[]> letters = new HashMap<>();
    static
    {
        letters.put('A', new Part[]{DI, DAH});
        letters.put('B', new Part[]{DAH, DI, DI, DI});
        letters.put('C', new Part[]{DAH, DI, DAH, DI});
        letters.put('D', new Part[]{DAH, DI, DI});
        letters.put('E', new Part[]{DI});
        letters.put('F', new Part[]{DI, DI, DAH, DI});
        letters.put('G', new Part[]{DAH, DAH, DI});
        letters.put('H', new Part[]{DI, DI, DI, DI});
        letters.put('I', new Part[]{DI, DI});
        letters.put('J', new Part[]{DI, DAH, DAH, DAH});
        letters.put('K', new Part[]{DAH, DI, DAH});
        letters.put('L', new Part[]{DI, DAH, DI, DI});
        letters.put('M', new Part[]{DAH, DAH});
        letters.put('N', new Part[]{DAH, DI});
        letters.put('O', new Part[]{DAH, DAH, DAH});
        letters.put('P', new Part[]{DI, DAH, DAH, DI});
        letters.put('Q', new Part[]{DAH, DAH, DI, DAH});
        letters.put('R', new Part[]{DI, DAH, DI});
        letters.put('S', new Part[]{DI, DI, DI});
        letters.put('T', new Part[]{DAH});
        letters.put('U', new Part[]{DI, DI, DAH});
        letters.put('V', new Part[]{DI, DI, DI, DAH});
        letters.put('W', new Part[]{DI, DAH, DAH});
        letters.put('X', new Part[]{DAH, DI, DI, DAH});
        letters.put('Y', new Part[]{DAH, DI, DAH, DAH});
        letters.put('Z', new Part[]{DAH, DAH, DI, DI});
        
        letters.put('1', new Part[]{DI, DAH, DAH, DAH, DAH});
        letters.put('2', new Part[]{DI, DI, DAH, DAH, DAH});
        letters.put('3', new Part[]{DI, DI, DI, DAH, DAH});
        letters.put('4', new Part[]{DI, DI, DI, DI, DAH});
        letters.put('5', new Part[]{DI, DI, DI, DI, DI});
        letters.put('6', new Part[]{DAH, DI, DI, DI, DI});
        letters.put('7', new Part[]{DAH, DAH, DI, DI, DI});
        letters.put('8', new Part[]{DAH, DAH, DAH, DI, DI});
        letters.put('9', new Part[]{DAH, DAH, DAH, DAH, DI});
        letters.put('0', new Part[]{DAH, DAH, DAH, DAH, DAH});
    }
    private int unit;      // millis
    private int spu;       // samples per unit
    private int pitch;     // Hz 600-1000
    private byte[] tone;
    private byte[] silence;
    private final SourceDataLine sdl;

    private MorseCode(int wordsPerMinute) throws LineUnavailableException
    {
        this(wordsPerMinute, 700);
    }
    private MorseCode(int wordsPerMinute, int pitch) throws LineUnavailableException
    {
        this.unit = 1200 / wordsPerMinute;
        this.pitch = pitch;
        spu = unit*SAMPLE_RATE/1000;
        tone = createTone();
        silence = new byte[7*spu];
        AudioFormat af
                = new AudioFormat(
                        SAMPLE_RATE, // sampleRate
                        8, // sampleSizeInBits
                        1, // channels
                        true, // signed
                        false);      // bigEndian
        sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
    }
    public static MorseCode getInstance(int wordsPerMinute) throws LineUnavailableException
    {
        return new MorseCode(wordsPerMinute);
    }
    public static MorseCode getInstance(int wordsPerMinute, int pitch) throws LineUnavailableException
    {
        return new MorseCode(wordsPerMinute, pitch);
    }
    public Collection<Character> getLetters()
    {
        return letters.keySet();
    }
    public String toString(CharSequence seq)
    {
        StringBuilder sb = new StringBuilder();
        int len = seq.length();
        for (int ii=0;ii<len;ii++)
        {
            char cc = seq.charAt(ii);
            if (Character.isSpaceChar(cc))
            {
                sb.append("  ");
            }
            else
            {
                toString(cc, sb);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    public String toString(char cc, StringBuilder sb)
    {
        Part[] part = letters.get(cc);
        if (part == null)
        {
            throw new IllegalArgumentException(cc+" not defined");
        }
        for (Part part1 : part)
        {
            switch (part1)
            {
                case DI:
                    sb.append(".");
                    break;
                case DAH:
                    sb.append("-");
                    break;
            }
        }
        return sb.toString();
    }
    public long key(CharSequence seq)
    {
        long time = 0;
        int len = seq.length();
        for (int ii=0;ii<len;ii++)
        {
            char cc = seq.charAt(ii);
            if (Character.isSpaceChar(cc))
            {
                sdl.write(silence, 0, 7*spu);
                time += 7*unit;
            }
            else
            {
                long t = key(cc);
                time += t;
                sdl.write(silence, 0, 3*spu);
                time += 3*unit;
            }
        }
        return time;
    }
    public long key(char cc)
    {
        long time = 0;
        Part[] part = letters.get(cc);
        if (part == null)
        {
            throw new IllegalArgumentException(cc+" not defined");
        }
        for (Part part1 : part)
        {
            switch (part1)
            {
                case DI:
                    sdl.write(tone, 0, spu);
                    time += unit;
                    break;
                case DAH:
                    sdl.write(tone, 0, 3*spu);
                    time += 3*unit;
                    break;
            }
            sdl.write(silence, 0, spu);
            time += unit;
        }
        return time;
    }
    @Override
    public void close()
    {
        sdl.drain();
        sdl.stop();
        sdl.close();
    }
    private byte[] createTone()
    {
        return createTone(3*spu, SAMPLE_RATE, pitch);
    }
    public static byte[] createTone(int millis, int rate, double pitch)
    {
        byte[] a = new byte[millis*rate/1000];
        double waveLen = rate / pitch;
        double d = (2* Math.PI) / waveLen;
        double x = 0;
        for (int ii=0;ii<a.length;ii++)
        {
            a[ii] = (byte) (32*Math.sin(x));
            x += d;
        }
        return a;
    }
}
