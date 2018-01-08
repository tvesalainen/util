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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.hffax.FaxTokenizer.Tone;
import static org.vesalainen.ham.hffax.FaxTokenizer.Tone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxEngine
{
    private int lpm;
    private int ioc;
    private int resolution = 1810;
    private BufferedImage image;
    private int line;
    private FaxTokenizer tokenizer;

    public FaxEngine(TargetDataLine line)
    {
        this(new AudioInputStream(line));
    }

    public FaxEngine(AudioInputStream ais)
    {
        Objects.requireNonNull(ais, "AudioInputStream");
        tokenizer = new FaxTokenizer(ais);
    }
    public void parse() throws IOException
    {
        try
        {
            int index = 0;
            while (true)
            {
                long lineLen = sync();
                if (lineLen > 0)
                {
                    image = new BufferedImage(resolution, 3000, TYPE_BYTE_BINARY);
                    Graphics2D graphics = image.createGraphics();
                    int lines = render(lineLen, graphics);
                    BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), lines);
                    ImageIO.write(subimage, "png", new File("fax"+index+".png"));
                }
                index++;
            }
        }
        catch (EOFException ex)
        {
            
        }
    }
    private long getLineLen(long[] lineLens)
    {
        long avg = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int ii=0;ii<lineLens.length;ii++)
        {
            long v = lineLens[ii];
            avg += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        avg = (avg-max-min)/(lineLens.length-2);
        return avg;
    }
    private long sync() throws IOException
    {
        Tone state = UNKNOWN;
        long prev = 0;
        int lineLen = 0;
        while (true)
        {
            Tone tone = tokenizer.nextTone();
            int span = (int) (tokenizer.getMicros() - prev);
            switch (state)
            {
                case BLACK:
                    if (span > 200000)
                    {
                        switch (tone)
                        {
                            case WHITE:
                                lineLen = span;
                                state = WHITE;
                                break;
                            default:
                                state = UNKNOWN;
                        }
                    }
                    else
                    {
                        state = UNKNOWN;
                    }
                    break;
                case WHITE:
                    switch (tone)
                    {
                        case BLACK:
                            lineLen += span;
                            long ll = getLPM(lineLen);
                            if (ll > 0)
                            {
                                return ll;
                            }
                            else
                            {
                                state = UNKNOWN;
                            }
                            break;
                        default:
                            state = UNKNOWN;
                    }
                    break;
                default:
                    if (tone == BLACK)
                    {
                        state = BLACK;
                    }
            }
            prev = tokenizer.getMicros();
        }
    }
    private long getLPM(long lineLen)
    {
        int r = (int) ((lineLen+50)/100);
        switch (r)
        {
            case 5000:
                return 1000000*60/120;  // 120
            default:
                return -1;
        }
    }
    public int render(long lineLen, Graphics2D... graphics) throws IOException
    {
        Rectangle[] bounds = new Rectangle[graphics.length];
        for (int ii=0;ii<graphics.length;ii++)
        {
            graphics[ii].setBackground(Color.BLACK);
            graphics[ii].setColor(Color.white);
            bounds[ii] = graphics[ii].getDeviceConfiguration().getBounds();
        }
        long start = tokenizer.getMicros();
        long beg = 0;
        long time;
        int lin=0;
        long prev = 0;
        long span = 0;
        try
        {
            while (true)
            {
                Tone tone = tokenizer.nextTone();
                time = tokenizer.getMicros()-start;
                span = time - prev;
                lin = (int) (time / lineLen);
                if (lin == 1200)
                {
                    System.err.println();
                }
                switch (tone)
                {
                    case BLACK:
                        for (int ii=0;ii<graphics.length;ii++)
                        {
                            int begin = (int) (bounds[ii].width*(beg % lineLen)/lineLen);
                            int col = (int) (bounds[ii].width*(time % lineLen)/lineLen);
                            if (begin < col)
                            {
                                graphics[ii].drawLine(begin, lin, col, lin);
                            }
                            else
                            {
                                graphics[ii].drawLine(begin, lin, resolution, lin);
                            }
                        }
                        break;
                    case WHITE:
                        beg = time;
                        break;
                    default:
                        System.err.printf("line=%d span=%d tone=%s\n", lin, span, tone);
                        if (span > 1000000)
                        {
                            return lin;
                        }
                        break;
                }
                prev = time;
            }
        }
        catch (EOFException ex)
        {
        }
        return lin;
    }

}
