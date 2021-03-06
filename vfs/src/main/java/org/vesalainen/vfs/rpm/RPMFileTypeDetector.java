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
package org.vesalainen.vfs.rpm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMFileTypeDetector extends FileTypeDetector
{
    static final byte[] LEAD_MAGIC = new byte[]{(byte) 0xed, (byte) 0xab, (byte) 0xee, (byte) 0xdb};

    @Override
    public String probeContentType(Path path) throws IOException
    {
        String filename = path.getFileName().toString();
        if (filename.toLowerCase().endsWith(".rpm"))
        {
            try (InputStream is = Files.newInputStream(path))
            {
                byte[] magic = new byte[LEAD_MAGIC.length];
                is.read(magic);
                if (Arrays.equals(magic, LEAD_MAGIC))
                {
                    return "application/x-rpm";
                }
            }
        }
        return null;
    }
    
}
