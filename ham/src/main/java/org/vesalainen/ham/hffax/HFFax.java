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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nmea.icommanager.IcomManager;
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
        addOption(String.class, "-l", "Mixer", "line", false);
        addOption(File.class, "-fd", "Fax Directory", null, true);
        addOption("-info", "Show Info", null, false);
        addOption("-id", "Icom Id", "line", 0);
        addOption("-freq", "Frequency", "line", 0.0);
        addOption(String.class, "-ip", "Icom Comm Port", "line", false);
    }
    
    public void process() throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException
    {
        if (getOption("-info"))
        {
            info();
        }
        switch (getEffectiveGroup())
        {
            case "file":
                processFile(getOption("-f"));
                break;
            case "url":
                processURL(getOption("-u"));
                break;
            case "line":
                processLine(getOption("-l"));
                break;
        }
    }
    private void processFile(File path) throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream ais = AudioSystem.getAudioInputStream(path);
        FaxEngine fax = new FaxEngine(getOption("-fd"), ais);
        fax.parse();
    }
    private void processURL(URL url) throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        FaxEngine fax = new FaxEngine(getOption("-fd"), ais);
        fax.parse();
    }
    private void processLine(String mixer) throws LineUnavailableException, IOException, InterruptedException
    {
        double frequency = getOption("-freq");
        if (frequency != 0.0)
        {
            try (IcomManager icomManager = createIcomManager())
            {
                icomManager.setRemote(true);
                icomManager.setReceiveFrequency(frequency-1900.0);
                parseLine(mixer);
            }
        }
        else
        {
            parseLine(mixer);
        }
    }
    private void parseLine(String mixer) throws LineUnavailableException, IOException, InterruptedException
    {
        AudioFormat audioFormat = new AudioFormat(44000, 16, 1, true, false);
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);//, mixerInfo(mixer));
        System.err.println(targetDataLine.getLineInfo());
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        FaxEngine fax = new FaxEngine(getOption("-fd"), targetDataLine);
        fax.parse();
    }
    public Mixer.Info mixerInfo(String mixer)
    {
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
        {
            String name = mixerInfo.getName();
            if (name.startsWith(mixer))
            {
                return mixerInfo;
            }
        }
        return null;
    }
    public static void info()
    {
        System.err.println("Mixers:");
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
        {
            System.err.println(mixerInfo.getName());
        }
        System.err.println("Free comm ports:");
        for (String port : SerialChannel.getFreePorts())
        {
            System.err.println(port);
        }
    }
    private IcomManager createIcomManager() throws IOException, InterruptedException
    {
        int id = getOption("-id");
        String port = getOption("-ip");
        if (port != null && !port.isEmpty())
        {
            return new IcomManager(id, port);
        }
        else
        {
            return IcomManager.getInstance(id);
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
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

}
