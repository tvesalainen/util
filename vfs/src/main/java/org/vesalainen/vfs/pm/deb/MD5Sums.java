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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.util.HexUtil;
import static org.vesalainen.vfs.attributes.FileAttributeName.MD5;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MD5Sums
{
    private Map<Path,String> map = new HashMap<>();
    private Path path;

    public MD5Sums()
    {
    }

    public MD5Sums(Path controlRoot, Path root) throws IOException
    {
        path = controlRoot.resolve("md5sums");
        if (Files.exists(path))
        {
            Files.lines(path, UTF_8).forEach((l)->
            {
                try
                {
                    String[] split = l.split("[ ]+");
                    Path p = root.resolve(split[1]);
                    byte[] fileDigest = (byte[]) Files.getAttribute(p, MD5);
                    String digStr = HexUtil.toString(fileDigest);
                    if (!digStr.equalsIgnoreCase(split[0]))
                    {
                        throw new IllegalArgumentException(p+" md5 conflict");
                    }
                    map.put(p, split[0]);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public static final void save(Path controlRoot, Path root) throws IOException
    {
        Path path = controlRoot.resolve("md5sums");
        try (BufferedWriter w = Files.newBufferedWriter(path))
        {
            Files.walk(root).filter((p)->Files.isRegularFile(p)).forEach((p)->
            {
                try
                {
                    byte[] digest = (byte[]) Files.getAttribute(p, MD5);
                    Path r = root.relativize(p);
                    w.append(HexUtil.toString(digest)).append("  ").append(r.toString()).append('\n');
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
}
