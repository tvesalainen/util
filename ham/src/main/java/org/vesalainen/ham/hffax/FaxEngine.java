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
package org.vesalainen.ham.hffax;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.BitSet;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.jtransforms.fft.FloatFFT_1D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxEngine
{
    private int lpm;
    private int ioc;
    private int resolution;
    private float sampleRate;
    private int frameSize;
    private boolean bigEndian;
    private float minFrequency;
    private float maxFrequency;
    private final FloatFFT_1D fft;
    private final byte[] sample;
    private final float[] fftSample;
    private long offset;
    private final ByteBuffer byteBuffer;
    private ShortBuffer shortBuffer;
    private IntBuffer intBuffer;
    private final AudioInputStream ais;
    private final int sampleSize;
    private FrequencyCounter frequencyCounter;
    private BufferedImage image;
    private int line;
    private Graphics2D graphics;

    public FaxEngine(TargetDataLine line)
    {
        this(new AudioInputStream(line));
    }

    public FaxEngine(AudioInputStream ais)
    {
        this(ais, ais.getFormat());
    }

    public FaxEngine(AudioInputStream ais, AudioFormat format)
    {
        this(ais, format.getSampleRate(), format.getFrameSize(), format.isBigEndian());
    }
    
    public FaxEngine(AudioInputStream ais, float sampleRate, int frameSize, boolean bigEndian)
    {
        this(ais, 120, 576, 1810, sampleRate, frameSize, bigEndian, 200, 2600);
    }
    public FaxEngine(AudioInputStream ais, int lpm, int ioc, int resolution, float sampleRate, int frameSize, boolean bigEndian, float minFrequency, float maxFrequency)
    {
        this.ais = ais;
        this.lpm = lpm;
        this.ioc = ioc;
        this.resolution = resolution;
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.bigEndian = bigEndian;
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        
        Objects.requireNonNull(ais, "AudioInputStream");
        
        float lineSpan = (float) (60.0/lpm);
        sampleSize = (int) (lineSpan*sampleRate/resolution);
        fft = new FloatFFT_1D(sampleSize);
        frequencyCounter = new FrequencyCounter((int) sampleRate);
        sample = new byte[sampleSize*frameSize];
        byteBuffer = ByteBuffer.wrap(sample);
        fftSample = new float[sampleSize*2];
        
        switch (frameSize)
        {
            case 1: 
                break;
            case 2:
                byteBuffer.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
                shortBuffer = byteBuffer.asShortBuffer();
                break;
            case 4:
                byteBuffer.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
                intBuffer = byteBuffer.asIntBuffer();
                break;
            default:
                throw new IllegalArgumentException("illegal bytesPerSample "+frameSize);
        }
    }
    public void parse() throws IOException
    {
        try
        {
            while (true)
            {
                int lineLen = sync();
                if (lineLen > 0)
                {
                    render(lineLen);
                }
            }
        }
        catch (EOFException ex)
        {
            
        }
    }
    public void render(int lineLen) throws IOException
    {
        image = new BufferedImage(1810, 1800, TYPE_BYTE_BINARY);
        graphics = image.createGraphics();
        line = 0;
        int ll = (int) (((60.0/lpm)*sampleRate)/sampleSize);
        BitSet bits = new BitSet();
        try
        {
            while (true)
            {
                for (int ii=0;ii<lineLen;ii++)
                {
                    float freq = readFrequency();
                    boolean isBlack = freq > 1000 && freq < 1900;
                    bits.set(ii, isBlack);
                }
                renderLine(bits);
                bits.clear();
                line++;
            }
        }
        catch (EOFException ex)
        {
            ImageIO.write(image, "png", new File("fax.png"));
        }
    }
    private void renderLine(BitSet bits)
    {
        int from = bits.nextClearBit(0);
        int to = bits.nextSetBit(from);
        while (to != -1)
        {
            graphics.drawLine(from, line, to-1, line);
            from = bits.nextClearBit(to);
            if (from == -1)
            {
                break;
            }
            to = bits.nextSetBit(from);
        }
    }
    public int sync() throws IOException
    {
        long start = millis();
        boolean last = false;
        int blen = 0;
        int wlen = 0;
        while (true)
        {
            float freq = readFrequency();
            boolean isBlack = freq > 1000 && freq < 1900;
            if (isBlack != last)
            {
                if (isBlack)
                {
                    long btime = 1000*blen*sampleSize/(long)sampleRate;
                    long wtime = 1000*wlen*sampleSize/(long)sampleRate;
                    if (btime > 200 && btime > wtime)
                    {
                        int l = (int) (60000/(btime+wtime));
                        switch (l)
                        {
                            case 60:
                            case 90:
                            case 100:
                            case 120:
                            case 180:
                            case 240:
                                lpm = l;
                                return blen+wlen;
                        }
                    }
                    blen = 0;
                    wlen = 0;
                }
            }
            last = isBlack;
            if (isBlack)
            {
                blen++;
            }
            else
            {
                wlen++;
            }
        }
    }
    /**
     * Returns time from start in milliseconds.
     * @return 
     */
    public long millis()
    {
        return (1000*offset/(long)sampleRate);
    }
    private float readFrequency() throws IOException
    {
        int off = 0;
        int len = sample.length;
        while (len > 0)
        {
            int rc = ais.read(sample, off, len);
            if (rc == -1)
            {
                throw new EOFException();
            }
            off += rc;
            len -= rc;
        }
        offset += sampleSize;
        switch (frameSize)
        {
            case 1:
                return frequencyCounter.count(sample);
            case 2:
                shortBuffer.clear();
                return frequencyCounter.count(shortBuffer);
            case 4:
                intBuffer.clear();
                return frequencyCounter.count(intBuffer);
            default:
                throw new IllegalArgumentException("illegal frameSize "+frameSize);
        }
    }
    private float readSample() throws IOException
    {
        int off = 0;
        int len = sample.length;
        while (len > 0)
        {
            int rc = ais.read(sample, off, len);
            if (rc == -1)
            {
                throw new EOFException();
            }
            off += rc;
            len -= rc;
        }
        offset += sampleSize;
        switch (frameSize)
        {
            case 1:
                for (int ii=0;ii<sampleSize;ii++)
                {
                    fftSample[2*ii] = sample[ii];
                    fftSample[2*ii+1] = 0;
                }
                break;
            case 2:
                for (int ii=0;ii<sampleSize;ii++)
                {
                    fftSample[2*ii] = shortBuffer.get(ii);
                    fftSample[2*ii+1] = 0;
                }
                break;
            case 4:
                for (int ii=0;ii<sampleSize;ii++)
                {
                    fftSample[2*ii] = intBuffer.get(ii);
                    fftSample[2*ii+1] = 0;
                }
                break;
            default:
                throw new IllegalArgumentException("illegal frameSize "+frameSize);
        }
        fft.complexForward(fftSample);
        float maxStrength = -1;
        float hz = 0;
        int d = ((int)sampleRate/sampleSize);
        int begin = Math.max((int)minFrequency/d, 0);
        int end = Math.min((int)maxFrequency/d, sampleSize);
        for (int ii=begin;ii<end;ii++)
        {
            float strength = (float) Math.hypot(fftSample[2*ii], fftSample[2*ii+1]);
            float freq = ((float)ii/(float)sampleSize)*(float)sampleRate;
            if (strength > maxStrength)
            {
                maxStrength = strength;
                hz = freq;
            }
        }
        return hz;
    }

}
