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
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.vesalainen.ham.LocationParser;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoFile extends JavaLogging
{
    private Path path;
    private String description;
    private Map<String,Column> columns;
    private List<GeoLocation> locations = new ArrayList<>();
    private Column indexColumn;
    private Column latCol;
    private Column lonCol;

    public GeoFile(Path path) throws IOException
    {
        super(GeoFile.class);
        this.path = path;
        try (Stream<String> lines = Files.lines(path, ISO_8859_1))
        {
            lines.forEach(this::line);
        }
    }
    public boolean isValid()
    {
        return columns != null && !locations.isEmpty();
    }
    public void search(List<GeoLocation> list, GeoSearch... searches)
    {
        fine("search %s", searches);
        for (GeoSearch search : searches)
        {
            String attribute = search.getAttribute();
            if (!columns.containsKey(attribute))
            {
                fine("attribute %s not found", attribute);
                return;
            }
        }
        for (GeoLocation loc : locations)
        {
            if (loc.match(searches))
            {
                list.add(loc);
            }
        }
    }
    private Location location(String line)
    {
        if (latCol.end == lonCol.begin)
        {
            String coord = line.substring(latCol.begin, lonCol.end);
            return LocationParser.parse(coord);
        }
        else
        {
            return LocationParser.parse(latCol.getColumn(line)+lonCol.getColumn(line));
        }
    }
    private void line(String line)
    {
        if (description == null)
        {
            description = line;
        }
        else
        {
            if (columns == null)
            {
                parseColumns(line);
            }
            else
            {
                if (line.contains("("))
                {
                    System.err.println(line);
                }
                locations.add(new GeoLocationImpl(line));
            }
        }
    }

    private void parseColumns(String line)
    {
        String[] titles = line.split("\\|");
        if (titles.length > 2)
        {
            columns = new HashMap<>();
            for (String title : titles)
            {
                if (!title.isEmpty())
                {
                    int idx = line.indexOf(title);
                    if (idx != -1)
                    {
                        Column col = new Column(title, idx-1, idx+title.length());
                        columns.put(col.name, col);
                        if (indexColumn == null)
                        {
                            indexColumn = col;
                        }
                    }
                }
            }
            latCol = columns.get("LATITUDE");
            lonCol = columns.get("LONGITUDE");
        }
    }
    public class GeoLocationImpl implements GeoLocation
    {
        private String line;

        public GeoLocationImpl(String line)
        {
            this.line = line.toUpperCase();
        }

        @Override
        public boolean match(GeoSearch... searches)
        {
            for (GeoSearch search : searches)
            {
                String value = getAttribute(search.getAttribute());
                if (value == null)
                {
                    return false;
                }
                else
                {
                    if (!value.contains(search.getValue()))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        
        @Override
        public Collection<String> getAttributes()
        {
            return columns.keySet();
        }

        @Override
        public String getAttribute(String attribute)
        {
            Column col = columns.get(attribute);
            if (col == null)
            {
                throw new IllegalArgumentException(attribute+" not found");
            }
            return col.getColumn(line);
        }

        @Override
        public Location getLocation()
        {
            return location(line);
        }

        @Override
        public String toString()
        {
            return path+": "+line;
        }
        
    }
    private class Column
    {
        private String name;
        private int begin;
        private int end;

        public Column(String name, int begin, int end)
        {
            this.name = trim(name);
            this.begin = begin;
            this.end = end;
        }
        
        private String getColumn(String line)
        {
            int len = line.length();
            if (begin < len)
            {
                return line.substring(begin, Math.min(end, len));
            }
            else
            {
                return null;
            }
        }

        private String trim(String name)
        {
            return CharSequences.trim(name, (c)->!Character.isLetterOrDigit(c)).toString();
        }
    }
}
