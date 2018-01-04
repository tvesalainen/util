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
        FaxEngine fax = new FaxEngine(ais);
        fax.parse();
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
