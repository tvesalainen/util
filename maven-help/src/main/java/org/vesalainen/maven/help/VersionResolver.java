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
package org.vesalainen.maven.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VersionResolver
{
    private ArtifactKey key;
    private Stream<Version> versions;
    private List<Version> preferredList = new ArrayList<>();
    private MavenKey mavenKey;

    public VersionResolver(ArtifactKey key, VersionRange range, List<Version> versions)
    {
        this.key = key;
        this.versions = versions.stream().filter(range.getPredicate());
        Version preferred = range.getPreferred();
        if (preferred != null)
        {
            preferredList.add(preferred);
        }
    }
    
    public void addRange(VersionRange range)
    {
    }
    
    public MavenKey resolv()
    {
        if (mavenKey == null)
        {
            List<Version> filtered = versions.sorted().collect(Collectors.toList());
            if (filtered.isEmpty())
            {
                throw new IllegalArgumentException("version conflick for "+key);
            }
            preferredList.retainAll(filtered);
            Version version;
            if (!preferredList.isEmpty())
            {
                version = preferredList.get(preferredList.size()-1);
            }
            else
            {
                version = filtered.get(filtered.size()-1);
            }
            mavenKey = new MavenKey(key.getGroupId(), key.getArtifactId(), version.toString(), key.getType());
        }
        return mavenKey;
    }
        

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final VersionResolver other = (VersionResolver) obj;
        if (!Objects.equals(this.key, other.key))
        {
            return false;
        }
        return true;
    }
}
