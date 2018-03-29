/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.fft;

import java.io.IOException;
import static java.nio.ByteOrder.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.filter.Filter;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterAudioInputStream extends AudioInputStream
{
    private Filter filter;
    private byte[] array;
    private IntArray intArray;
    private int offset;
    private int length;
    private boolean eof;
    
    public FilterAudioInputStream(AudioInputStream stream, int size, double low, double high)
    {
        super(stream, stream.getFormat(), stream.getFrameLength());
        init(size, low, high);
    }

    public FilterAudioInputStream(TargetDataLine line, int size, double low, double high)
    {
        super(line);
        init(size, low, high);
    }
    private void init(int size, double low, double high)
    {
        AudioFormat fmt = getFormat();
        if (fmt.getChannels() != 1)
        {
            throw new UnsupportedOperationException("only mono supported");
        }
        float sampleFrequency = fmt.getSampleRate();
        array = new byte[size*fmt.getFrameSize()];
        intArray = IntArray.getInstance(array, fmt.getSampleSizeInBits(), fmt.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);
        this.filter = new FFTFilter(sampleFrequency, low, high, size);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException
    {
        if (length == 0)
        {
            if (eof)
            {
                return -1;
            }
            int l = array.length;
            int o = 0;
            while (l > 0)
            {
                int rc = super.read(array, o, l);
                if (rc == -1)
                {
                    if (o == 0)
                    {
                        return -1;
                    }
                    eof = true;
                    break;
                }
                o += rc;
                l -= rc;
            }
            offset = 0;
            length = array.length-l;
            filter.filter(intArray);
        }
        int count = Math.min(length, len);
        System.arraycopy(array, offset, buf, off, count);
        offset += count;
        length -= count;
        return count;
    }
    
}
