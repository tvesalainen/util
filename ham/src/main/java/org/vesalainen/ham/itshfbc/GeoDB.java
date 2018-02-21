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
package org.vesalainen.ham.itshfbc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.vesalainen.util.Lists;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoDB extends JavaLogging
{
    private Path itshfbc;
    private List<GeoFile> files = new ArrayList<>();

    public GeoDB() throws IOException
    {
        this(Paths.get("C:\\itshfbc"));
    }

    public GeoDB(Path itshfbc) throws IOException
    {
        super(GeoDB.class);
        this.itshfbc = itshfbc;
        try (Stream<Path> paths = Files.find(itshfbc, 3, (p, a)->p.toString().toLowerCase().endsWith(".geo")&&a.isRegularFile()))
        {
            paths.forEach(this::load);
        }
    }
    /**
     * Returns any GeoLocation which matches all tests and is unique in deltaNM
     * range.
     * @param deltaNM
     * @param searches
     * @return 
     */
    public GeoLocation search(double deltaNM, GeoSearch... searches)
    {
        List<GeoLocation> list = search(false, searches);
        if (!list.isEmpty() && GeoDB.isUnique(list, deltaNM))
        {
            return list.get(0);
        }
        else
        {
            list.removeIf((gl)->!gl.match(true, searches));
            if (!list.isEmpty() && GeoDB.isUnique(list, deltaNM))
            {
                return list.get(0);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * Returns a list of GeoLocations which match all the searches
     * @param searches
     * @return 
     */
    public List<GeoLocation> search(boolean strict, GeoSearch... searches)
    {
        List<GeoLocation> list = new ArrayList<>();
        for (GeoFile gf : files)
        {
            gf.search(strict, list, searches);
        }
        return list;
    }
    /**
     * Returns true if greatest distance of any location from their center is
     * less than given delta in nm.
     * @param list
     * @param delta
     * @return 
     */
    public static boolean isUnique(List<GeoLocation> list, double delta)
    {
        Location[] array = new Location[list.size()];
        int index = 0;
        for (GeoLocation gl : list)
        {
            array[index++] = gl.getLocation();
        }
        return (Location.radius(array) <= delta);
    }
    private void load(Path path)
    {
        try
        {
            config("loading %s", path);
            GeoFile gf = new GeoFile(path);
            if (gf.isValid())
            {
                files.add(gf);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(path.toString(), ex);
        }
    }
}
