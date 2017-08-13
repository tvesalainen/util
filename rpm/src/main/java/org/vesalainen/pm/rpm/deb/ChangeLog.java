/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm.rpm.deb;

import java.io.BufferedWriter;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.util.Lists;

/**
 *
 * @author tkv
 */
public class ChangeLog
{
    private Path dir;
    private String name;
    private String version;
    private String release;
    private List<String> distributions = new ArrayList<>();
    private String urgency = "low";
    private String maintainer;
    private List<String> changeDetails = new ArrayList<>();

    public ChangeLog(Path dir, String name, String version, String release, String maintainer)
    {
        this.dir = dir;
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
    
    public void save() throws IOException
    {
        if (changeDetails.isEmpty())
        {
            changeDetails.add("TO DO");
        }
        Path changelog = dir.resolve("changelog");
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
