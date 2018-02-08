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

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxTokenizerTest
{
    
    public FaxTokenizerTest()
    {
    }

    //@Test
    public void test() throws UnsupportedAudioFileException, IOException
    {
        URL url = HFFaxT.class.getResource("/hffax.wav");
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        FaxTokenizer ft = new FaxTokenizer(ais);
        FaxListener fl = new FaxListener() {
            @Override
            public void tone(FaxTone tone, long begin, long end, long span, float amplitude, long error)
            {
                System.err.printf("%8s % 10d - % 10d % 6d %.1f\n", tone, begin, end, span, amplitude);
            }
        };
        ft.addListener(fl);
        ft.run();
    }
    
}
