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
package org.vesalainen.vfs.pm.deb;

import java.io.BufferedWriter;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FilesBase
{
    
    protected Set<Path> files;
    private final String name;

    protected FilesBase(String name)
    {
        this.name = name;
        this.files = new HashSet<>();
    }
    
    protected FilesBase(String name, Path debian) throws IOException
    {
        this.name = name;
        Path path = debian.resolve(name);
        if (Files.exists(path))
        {
            FileSystem fs = debian.getFileSystem();
            files = Files.lines(path, UTF_8)
                    .map((l)->fs.getPath(l))
                    .collect(Collectors.toSet());
        }
        else
        {
            this.files = Collections.EMPTY_SET;
        }
    }
    public boolean contains(Path path)
    {
        return files.contains(path);
    }

    public Set<Path> getFiles()
    {
        return files;
    }
    
    public void addFile(Path filepath)
    {
        files.add(filepath);
    }
    public void save(Path debian) throws IOException
    {
        Path path = debian.resolve(name);
        try (final BufferedWriter bf = Files.newBufferedWriter(path, UTF_8))
        {
            for (Path file : files)
            {
                bf.append(file.toString()).append('\n');
            }
        }
    }
    
}
