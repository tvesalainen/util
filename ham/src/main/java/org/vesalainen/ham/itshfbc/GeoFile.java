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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.vesalainen.ham.LocationParser;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoFile
{
    private String description;
    private Map<String,Column> columns;
    private MapList<String,String> map = new HashMapList<>();
    private Column indexColumn;
    private Column latCol;
    private Column lonCol;

    public GeoFile(Path path) throws IOException
    {
        try (Stream<String> lines = Files.lines(path, ISO_8859_1))
        {
            lines.forEach(this::line);
        }
    }
    public Location getLocation(String search)
    {
        if (latCol == null || lonCol == null)
        {
            throw new IllegalArgumentException("no coordinates available");
        }
        List<String> list = search(search.toUpperCase());
        switch (list.size())
        {
            case 1:
                return location(list.get(0));
            case 0:
                return null;
            default:
                throw new IllegalArgumentException(search+" resulted several locations");
        }
    }
    private List<String> search(String search)
    {
        List<String> list = map.get(search.toUpperCase());
        if (list.isEmpty())
        {
            list = new ArrayList<>();
            for (Entry<String, List<String>> entry : map.entrySet())
            {
                if (entry.getKey().startsWith(search))
                {
                    list.addAll(entry.getValue());
                }
            }
        }
        return list;
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
                map.add(indexColumn.getColumn(line), line);
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
            return line.substring(begin, end);
        }

        private String trim(String name)
        {
            return CharSequences.trim(name, (c)->!Character.isLetterOrDigit(c)).toString();
        }
    }
}
