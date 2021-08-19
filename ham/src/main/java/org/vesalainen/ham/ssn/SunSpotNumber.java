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

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.math.ClosedCubicSpline;
import org.vesalainen.math.RelaxedCubicSpline;
import org.vesalainen.net.NetFile;

/**
 * Interpolates Sun Spot Numbers using data from WDC-SILSO, Royal Observatory of 
 * Belgium, Brussels
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see WDC-SILSO, Royal Observatory of Belgium, Brussels
 * @see <a href="http://www.sidc.be/silso/prediml">Forecasts : McNish & Lincoln method</a>
 */
public class SunSpotNumber extends NetFile
{
    private FileTime lastModified;
    private RelaxedCubicSpline curve;

    public SunSpotNumber(Path file) throws MalformedURLException
    {
        super(file, new URL("http://www.sidc.be/silso/FORECASTS/prediML.txt"), 100, TimeUnit.DAYS);
    }
    
    public double getSunSpotNumber() throws IOException
    {
        return getSunSpotNumber(OffsetDateTime.now());
    }
    public double getSunSpotNumber(OffsetDateTime dateTime) throws IOException
    {
        refresh();
        int year = dateTime.get(ChronoField.YEAR);
        OffsetDateTime current = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime next = OffsetDateTime.of(year+1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        double millisInYear = current.until(next, ChronoUnit.MILLIS);
        double fromBeginOfYear = current.until(dateTime, ChronoUnit.MILLIS);
        double key = year+fromBeginOfYear/millisInYear;
        readLock.lock();
        try
        {
            return curve.applyAsDouble(key);
        }
        finally
        {
            readLock.unlock();
        }
    }
    @Override
    protected void update(Path file)
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
    private void calculate(Path file) throws IOException
    {
        try (Stream<String> lines = Files.lines(file))
        {
            List<Point2D> points = lines
                    .map((l)->l.split("[ ]+"))
                    .map((a)->new Point2D.Double(Double.parseDouble(a[2]), Double.parseDouble(a[4])))
                    .collect(Collectors.toList());
            curve = new RelaxedCubicSpline(points);
        }
    }
}
