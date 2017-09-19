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
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.time.ZonedDateTime;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.util.Lists;
import org.vesalainen.vfs.CompressorFactory;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChangeLog
{
    private String name;
    private String version;
    private String release;
    private List<String> distributions = new ArrayList<>();
    private String urgency = "low";
    private String maintainer;
    private List<String> changeDetails = new ArrayList<>();
    private ZonedDateTime date;

    public ChangeLog()
    {
    }

    public ChangeLog(Path path) throws IOException
    {
        if (Files.exists(path))
        {
            try (FileChannel ch = FileChannel.open(path, READ);
                FilterChannel channel = new FilterChannel(ch, 4096, 0, CompressorFactory.input(path), null))
            {
                Reader reader = Channels.newReader(channel, "UTF-8");
                BufferedReader br = new BufferedReader(reader);
                String line = br.readLine();
                String[] split = line.split(" ");
                if (split.length < 4)
                {
                    throw new IllegalArgumentException(line+" illegal");
                }
                name = split[0];
                String[] split2 = split[1].substring(1, split[1].length()-1).split("-");
                if (split2.length != 2)
                {
                    throw new IllegalArgumentException(line+" illegal version/release");
                }
                version = split2[0];
                release = split2[1];
                int index = 1;
                do
                {
                    index++;
                    String s = split[index];
                    distributions.add(s.endsWith(";") ? s.substring(0, s.length()-1) : s);
                } while (!split[index].endsWith(";"));
                index++;
                if (!split[index].startsWith("urgency="))
                {
                    throw new IllegalArgumentException(line+" illegal urgency");
                }
                urgency = split[index].substring(split[index].indexOf('=')+1);
                line = br.readLine();
                while (!line.startsWith(" --"))
                {
                    if (line.startsWith("  "))
                    {
                        changeDetails.add(line.trim());
                    }
                    line = br.readLine();
                }
                String s = line.substring(3).trim();
                int idx = s.indexOf('>');
                if (idx ==-1)
                {
                    throw new IllegalArgumentException(line+" illegal maintainer");
                }
                maintainer = s.substring(0, idx+1);
                s = s.substring(idx+2).trim();
                date = ZonedDateTime.parse(s, RFC_1123_DATE_TIME);
            }
        }
    }

    public void set(String name, String version, String release, String maintainer)
    {
        this.name = name;
        this.version = version;
        this.release = release;
        this.maintainer = maintainer;
        distributions.add("unstable");
    }

    public void addChangeDetail(String text)
    {
        changeDetails.add(text);
    }
    public void setUrgency(String urgency)
    {
        this.urgency = urgency;
    }

    public void setDistributions(String... distributions)
    {
        this.distributions = Lists.create(distributions);
    }
    
    public void save(Path debian) throws IOException
    {
        if (changeDetails.isEmpty())
        {
            changeDetails.add("TO DO");
        }
        Path changelog = debian.resolve("changelog");
        try (BufferedWriter bf = Files.newBufferedWriter(changelog, UTF_8))
        {
            bf.append(String.format("%s (%s-%s) %s; urgency=%s\n", 
                    name, 
                    version, 
                    release,
                    distributions.stream().collect(Collectors.joining(" ")),
                    urgency));
            bf.append('\n');
            for (String change : changeDetails)
            {
                bf.append("  * ");
                bf.append(change);
                bf.append(" \n\n");
            }
            bf.append(String.format(" -- %s  %s\n\n", maintainer, ZonedDateTime.now().format(RFC_1123_DATE_TIME)));
        }
        
    }
}
