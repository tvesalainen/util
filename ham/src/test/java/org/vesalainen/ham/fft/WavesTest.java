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
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WavesTest
{
    
    public WavesTest()
    {
    }

    @Test
    public void testSamplesFM() throws IOException
    {
        TimeDomain td = Waves.createFMSample(4096, 200, 40, 80, TimeUnit.MILLISECONDS, 250, 250, 250, 250);
        IntArray samples = td.getSamples();
        Waves.addWhiteNoise(samples, 300);
        Waves.plot(samples, Paths.get("fm.png"));
        FrequencyDomain fd = Waves.fft(td);
        Waves.plot(fd, Paths.get("fftfm.png"));
        Waves.window(fd, 30, 90);
        TimeDomain ifft = Waves.ifft(fd);
        Waves.plot(ifft.getSamples(), Paths.get("ifftfm.png"));
    }
    @Test
    public void testSamples0() throws IOException
    {
        TimeDomain td = Waves.createSample(4096, 10, 1, TimeUnit.SECONDS, 
                Waves.of(10, 500, Math.PI/2)
        );
        IntArray samples = td.getSamples();
        Waves.plot(samples, Paths.get("samples.png"));
        FrequencyDomain fd = Waves.fft(td);
        Waves.plot(fd, Paths.get("fft.png"));
        TimeDomain ifft = Waves.ifft(fd);
        Waves.plot(ifft.getSamples(), Paths.get("ifft.png"));
    }
    @Test
    public void testSamples1() throws IOException
    {
        double phase = -Math.PI/4;
        TimeDomain td = Waves.createSample(4096, 10, 1, TimeUnit.SECONDS, 
                Waves.of(10, 500, phase)
        );
        FrequencyDomain fd = Waves.fft(td);
        assertEquals(250, fd.getMagnitude(10), 1);
        System.err.println(Math.toDegrees(phase));
        System.err.println(Math.toDegrees(fd.getPhase(10)));
        System.err.println(fd.getRe(10)+", "+fd.getIm(10));
        assertEquals(phase, fd.getPhase(10), 1e-5);
    }    
}
