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
package org.vesalainen.ham.riff;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.time.Duration;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.SampleBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WaveFileTest
{
    
    public WaveFileTest()
    {
    }

    @Test
    public void test1() throws IOException, UnsupportedAudioFileException
    {
        Path file = Paths.get("src", "test", "resources", "hffax2.wav");
        WaveFile wave = (WaveFile) RIFFFile.open(file);
        AudioFormat audioFormat = wave.getAudioFormat();
        AudioFormat exp = AudioSystem.getAudioFileFormat(file.toFile()).getFormat();
        assertEquals(exp.toString(), audioFormat.toString());
        SampleBuffer sampleBuffer = wave.getSampleBuffer(4096);
        Duration duration = sampleBuffer.getDuration();
        assertEquals(29*60+37, duration.getSeconds());
        Duration half = duration.dividedBy(2);
        sampleBuffer.goTo(half);
        assertEquals((29*60+37)/2, sampleBuffer.remaining().getSeconds());
        assertEquals(2, sampleBuffer.getChannels());
    }
    @Test
    public void test2() throws UnsupportedAudioFileException, IOException
    {
        Path in = Paths.get("src", "test", "resources", "hffax2.wav");
        Path out = Paths.get("hffax2test.wav");
        WaveFile wave = new WaveFile();
        wave.setName("timo");
        wave.setGenre("fax");
        wave.setArtist("artist");
        wave.setComments("kommentit");
        wave.setCopyright("copy");
        wave.setKeywords("sanata");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(in.toFile());
        wave.store(audioInputStream, out, CREATE, WRITE);
        RIFFFile.open(out);
    }    
}
