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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.vfs.CompressorFactory;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChangeLogFile
{
    private List<DebianChangeLog> logs = new ArrayList<>();
    
    public ChangeLogFile()
    {
    }

    public ChangeLogFile(Path path) throws IOException
    {
        if (Files.exists(path))
        {
            try (FileChannel ch = FileChannel.open(path, READ);
                FilterChannel channel = new FilterChannel(ch, 4096, 0, CompressorFactory.input(path), null))
            {
                Reader reader = Channels.newReader(channel, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null)
                {
                    if (!line.isEmpty())
                    {
                        DebianChangeLog log = new DebianChangeLog(bufferedReader, line);
                        logs.add(log);
                    }
                    line = bufferedReader.readLine();
                }
            }
            Collections.sort(logs);
        }
    }

    public void addChangeLog(DebianChangeLog log)
    {
        logs.add(log);
    }

    public List<? extends DebianChangeLog> getLogs()
    {
        return logs;
    }
    
    public boolean isEmpty()
    {
        return logs.isEmpty();
    }
    
    public void save(Path path) throws IOException
    {
        try (FileChannel ch = FileChannel.open(path, CREATE, WRITE);
            FilterChannel channel = new FilterChannel(ch, 4096, 0, null, CompressorFactory.output(path));
            BufferedWriter bf = new BufferedWriter(Channels.newWriter(channel, "UTf-8"));)
        {
            Collections.sort(logs);
            for (DebianChangeLog log : logs)
            {
                log.save(bf);
            }
        }
    }

}
