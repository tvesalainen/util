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
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.vesalainen.parsers.date.Dates;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.Lists;
import org.vesalainen.vfs.pm.SimpleChangeLog;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DebianChangeLog extends SimpleChangeLog
{
    private String packageName;
    private String version;
    private String release;
    private List<String> distributions;
    private String urgency;

    public DebianChangeLog(
            String packageName, 
            String version, 
            String release,
            String urgency, 
            String maintainer, 
            FileTime time, 
            String text,
            String... distributions)
    {
        super(maintainer, time, text);
        Objects.requireNonNull(packageName, "packageName");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(release, "release");
        Objects.requireNonNull(urgency, "urgency");
        Objects.requireNonNull(maintainer, "maintainer");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(text, "text");
        this.packageName = packageName;
        this.version = version;
        this.release = release;
        this.urgency = urgency;
        this.distributions = Lists.create(distributions);
    }
    
    public DebianChangeLog(BufferedReader br, String line) throws IOException
    {
        this.distributions = new ArrayList<>();
        List<String> changeDetails = new ArrayList<>();
        String[] split = line.split(" ");
        if (split.length < 4)
        {
            throw new IllegalArgumentException(line+" illegal");
        }
        packageName = split[0];
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
        System.err.println("'"+s+"'");
        ZonedDateTime date = Dates.parseRFC1123(s);
        this.time = FileTime.from(date.toInstant());
        this.text = changeDetails.stream().collect(Collectors.joining("\n", "", "\n"));
    }
    public void save(BufferedWriter bf) throws IOException
    {
        bf.append(String.format("%s (%s-%s) %s; urgency=%s\n", 
                packageName, 
                version, 
                release,
                distributions.stream().collect(Collectors.joining(" ")),
                urgency));
        bf.append('\n');
        CharSequences.split(text, '\n').forEach((change)->
        {
            try
            {
                bf.append("  * ");
                bf.append(change);
                bf.append(" \n\n");
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
        bf.append(String.format(" -- %s  %s\n\n", maintainer, ZonedDateTime.ofInstant(time.toInstant(), ZoneId.of("Z")).format(RFC_1123_DATE_TIME)));
    }
    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getRelease()
    {
        return release;
    }

    public void setRelease(String release)
    {
        this.release = release;
    }

    public List<String> getDistributions()
    {
        return distributions;
    }

    public void setDistributions(String... distributions)
    {
        this.distributions = Lists.create(distributions);
    }

    public String getUrgency()
    {
        return urgency;
    }

    public void setUrgency(String urgency)
    {
        this.urgency = urgency;
    }
    
}
