/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.pm.deb;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilesBase
{
    
    protected List<String> files = new ArrayList<>();
    private final String name;

    protected FilesBase(String name)
    {
        this.name = name;
    }
    public void addFile(String filepath)
    {
        files.add(filepath);
    }
    public void save(Path debian) throws IOException
    {
        Path path = debian.resolve(name);
        try (final BufferedWriter bf = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
        {
            for (String file : files)
            {
                bf.append(file).append('\n');
            }
        }
    }
    
}
