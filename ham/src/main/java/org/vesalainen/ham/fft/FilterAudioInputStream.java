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
import java.util.Collection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.nio.IntArray;
import org.vesalainen.util.Listener;
import org.vesalainen.util.ListenerSupport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilterAudioInputStream extends AudioInputStream
{
    protected byte[] array;
    protected IntArray intArray;
    protected int offset;
    protected int length;
    protected boolean eof;
    protected ListenerSupport<IntArray> listeners = new ListenerSupport<>();
    
    public FilterAudioInputStream(AudioInputStream stream, int size)
    {
        super(stream, stream.getFormat(), stream.getFrameLength());
        init(size);
    }

    public FilterAudioInputStream(TargetDataLine line, int size)
    {
        super(line);
        init(size);
    }
    private void init(int size)
    {
        AudioFormat fmt = getFormat();
        if (fmt.getChannels() != 1)
        {
            throw new UnsupportedOperationException("only mono supported");
        }
        array = new byte[size*fmt.getFrameSize()];
        intArray = IntArray.getInstance(array, fmt.getSampleSizeInBits(), fmt.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);
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
                if (rc <= 0)    // returns 0 when closed ?????
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
            fire(intArray);
        }
        int count = Math.min(length, len);
        System.arraycopy(array, offset, buf, off, count);
        offset += count;
        length -= count;
        return count;
    }

    public void addListener(Listener<IntArray> listener)
    {
        this.listeners.addListener(listener);
    }

    public void addListeners(Collection<Listener<IntArray>> listeners)
    {
        this.listeners.addListeners(listeners);
    }

    public void removeListener(Listener<IntArray> listener)
    {
        this.listeners.removeListener(listener);
    }

    public void fire(IntArray item)
    {
        this.listeners.fire(item);
    }

}
