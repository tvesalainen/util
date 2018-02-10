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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoDB
{
    private Path itshfbc;
    private List<GeoFile> files = new ArrayList<>();

    public GeoDB() throws IOException
    {
        this(Paths.get("C:\\itshfbc"));
    }

    public GeoDB(Path itshfbc) throws IOException
    {
        this.itshfbc = itshfbc;
        try (Stream<Path> paths = Files.find(itshfbc, 3, (p, a)->p.toString().endsWith(".geo")&&a.isRegularFile()))
        {
            paths.forEach(this::load);
        }
    }
    private void load(Path path)
    {
        try
        {
            GeoFile gf = new GeoFile(path);
            files.add(gf);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(path.toString(), ex);
        }
    }
}
