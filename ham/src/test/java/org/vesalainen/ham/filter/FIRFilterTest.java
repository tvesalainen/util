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
package org.vesalainen.ham.filter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.fft.TimeDomain;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FIRFilterTest
{
    
    public FIRFilterTest()
    {
    }

    @Test
    public void testSamplesFM2() throws IOException
    {
        TimeDomain td = Waves.createFMSample(4096, 200, 40.5, 80.5, TimeUnit.MILLISECONDS, 250, 250, 250, 250);
        IntArray samples = td.getSamples();
        Waves.addWhiteNoise(samples, 300);
        Waves.plot(samples, Paths.get("unfilteredFIR.png"));
        FIRFilter filter = new FIRFilter(40, 4096, 150);
        filter.update(samples);
        Waves.plot(samples, Paths.get("filteredFIR.png"));
    }
    
}
