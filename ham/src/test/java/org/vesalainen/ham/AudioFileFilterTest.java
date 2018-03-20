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
package org.vesalainen.ham;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AudioFileFilterTest
{
    
    public AudioFileFilterTest()
    {
    }

    //@Test
    public void test0() throws IOException, UnsupportedAudioFileException
    {
        Path cur = Paths.get(".", "src", "test", "resources");
        Path in = cur.resolve("wefax1.wav");
        Path out = cur.resolve("wefax1_fil.wav");
        AudioFileFilter.filter(in, out);
    }
    @Test
    public void test() throws IOException, UnsupportedAudioFileException
    {
        Path temp = Paths.get("c:\\temp");
        Path in = temp.resolve("F3C_PT. REYES, CALIFORNIA, U.S.A._WIND_WAVE ANALYSIS_16_19_13.wav");
        Path out = temp.resolve("FILTERED_F3C_PT. REYES, CALIFORNIA, U.S.A._WIND_WAVE ANALYSIS_16_19_13.wav");
        AudioFileFilter.filter(in, out);
    }
    
}
