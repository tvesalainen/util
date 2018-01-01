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
package org.vesalainen.ham.hffax;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.vesalainen.ham.fft.FFT;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HFFax extends CmdArgs
{
    private int lpm = 120;
    private int ioc = 576;
    private int resolution = 256;
    public HFFax()
    {
        addOption(File.class, "-f", "File", "file", false);
        addOption(URL.class, "-u", "URL", "url", false);
    }
    
    public void process() throws UnsupportedAudioFileException, IOException
    {
        switch (getEffectiveGroup())
        {
            case "file":
                processFile(getOption("-f"));
                break;
            case "url":
                processURL(getOption("-u"));
                break;
        }
    }
    public void processFile(File path) throws UnsupportedAudioFileException, IOException
    {
        AudioSystem.getAudioFileFormat(path);
    }
    public void processURL(URL url) throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        AudioFormat format = ais.getFormat();
        float sampleRate = format.getSampleRate();
        int frameSize = format.getFrameSize();
        boolean bigEndian = format.isBigEndian();
        float lineTime = (60F/(float)lpm);
        long sampleNanos = (long) (1000000*lineTime/resolution);
        int size = (int) (sampleRate*lineTime/resolution);
        FFT fft = new FFT(size);
        byte[] buf = new byte[size*frameSize];
        int rc = ais.read(buf);
        int count = 0;
        long time = 0;
        long tim = 0;
        boolean black = true;
        boolean color = false;
        while (rc != -1)
        {
            float frequency = fft.frequency(buf, (int) sampleRate, frameSize, bigEndian, 200F, 2300F);
            if (frequency >= 1000)
            {
                color = frequency < 2000;
                if (color != black)
                {
                    black = color;
                    long elap = time - tim;
                    tim = time;
                    System.err.println(time+": "+black+" "+elap);
                }
            }
            else
            {
                System.err.print(time+": ");
                bar(frequency);
                System.err.println(frequency);
            }
            time += sampleNanos;
            /*
            System.err.print(time+": ");
            bar(frequency);
            System.err.println(frequency);
            if ((count % resolution) == 0)
            {
                System.err.println();
            }
            else
            {
                if (frequency < 2000)
                {
                    System.err.print("x");
                }
                else
                {
                    System.err.print(" ");
                }
            }
            */
            rc = ais.read(buf);
            count++;
        }
    }
    private void bar(float f)
    {
        for (float x=200;x<=f;x+=100)
        {
            System.err.print("x");
        }
    }
    public static void main(String... args)
    {
        try
        {
            HFFax fax = new HFFax();
            fax.command(args);
            fax.process();
        }
        catch (UnsupportedAudioFileException | IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
