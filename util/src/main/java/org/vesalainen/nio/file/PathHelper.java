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
package org.vesalainen.nio.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.vesalainen.util.OperatingSystem;
import static org.vesalainen.util.OperatingSystem.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PathHelper
{
    private static final String SEP = File.separator;
    /**
     * Transform path from relative oldBase to newBase.
     * <p>
     * If oldBase = /a/b and newBase = /q/w/e then path /a/b/c/d will
     * transform to /q/w/e/c/d
     * @param oldBase
     * @param newBase
     * @param path
     * @return 
     */
    public static final Path transform(Path oldBase, Path newBase, Path path)
    {
        if (!path.startsWith(oldBase))
        {
            throw new IllegalArgumentException(path+" not in "+oldBase);
        }
        return newBase.resolve(oldBase.relativize(path));
    }
    /**
     * Converts '/' separated path to Path. Path is absolute if it starts with '/'.
     * @param posix
     * @return 
     */
    public static final Path fromPosix(String posix)
    {
        String path = posix.replace("/", SEP);
        if (OperatingSystem.is(Windows) && path.startsWith("\\"))
        {
            path = "c:"+path;
        }
            
        return Paths.get(path);
    }
    /**
     * Converts path to posix path. Converted path starts with '/' if path is 
     * absolute and ends with '/' if path is directory.
     * @param path
     * @return 
     * @see java.nio.file.Path#isAbsolute() 
     * @see java.nio.file.Files#
     */
    public static final String posixString(Path path)
    {
        String prefix = path.isAbsolute() ? "/" : "";
        String suffix = Files.isDirectory(path) ? "/" : "";
        return StreamSupport.stream(path.spliterator(), false)
                .map((p)->p.toString())
                .collect(Collectors.joining("/", prefix, suffix));
    }
}
