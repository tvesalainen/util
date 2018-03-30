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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxDecoderTest
{
    
    public FaxDecoderTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }

    @Test
    public void test() throws UnsupportedAudioFileException, IOException
    {
        try
        {
            Path in = Paths.get("src\\test\\resources\\hffax2.wav");
            //Path in = Paths.get("c:\\tmp\\J3C_BOSTON, MASSACHUSETTS, U.S.A._96 HR WIND_WAVE FORECAST_30_20_55.wav");
            FaxDecoder decoder = new FaxDecoder(120, 576, in, Paths.get("hffax.png"));
            decoder.parse();
        }
        catch(EOFException ex)
        {
        }
    }
    
}
