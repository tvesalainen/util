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
package org.vesalainen.nio;

import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PathHelp
{
    public static final Path getTimePath(Path dir, String ext)
    {
        return getTimePath(dir, ext, ZonedDateTime.now(ZoneOffset.UTC));
    }
    public static final Path getTimePath(Path dir, String ext, ZonedDateTime dateTime)
    {
        if (!ext.startsWith("."))
        {
            ext = "."+ext;
        }
        String pattern = "yyyyMMddHHmmss'"+ext+"'";
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendPattern(pattern);
        DateTimeFormatter formatter = builder.toFormatter();
        return dir.resolve(dateTime.format(formatter));
    }
}
