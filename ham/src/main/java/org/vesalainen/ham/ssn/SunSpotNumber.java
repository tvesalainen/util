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
package org.vesalainen.ham.ssn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.vesalainen.net.NetFile;

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see WDC-SILSO, Royal Observatory of Belgium, Brussels
 * @see <a href="http://www.sidc.be/silso/prediml">Forecasts : McNish & Lincoln method</a>
 */
public class SunSpotNumber
{
    private NetFile prediml;
    private FileTime lastModified;
    private Semaphore semaphore = new Semaphore(0);
    
    public SunSpotNumber(Path dir)
    {
        try
        {
            this.prediml = new NetFile(dir, new URL("http://www.sidc.be/silso/FORECASTS/prediML.txt"), this::update, 100, TimeUnit.DAYS);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
        
    }
    
    private void update(Path file)
    {
        try
        {
            FileTime lmt = Files.getLastModifiedTime(file);
            if (lastModified == null || lastModified.compareTo(lmt) != 0)
            {
                calculate(file);
            }
            lastModified = lmt;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private void calculate(Path file)
    {
        
    }
}
