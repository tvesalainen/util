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

import java.util.function.Predicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VersionRange
{
    private Predicate<Version> predicate;
    private String toString;
    private Version preferred;

    public VersionRange(Predicate<Version> predicate, String toString)
    {
        this.predicate = predicate;
        this.toString = toString;
    }

    public VersionRange(Predicate<Version> predicate, String toString, Version preferred)
    {
        this.predicate = predicate;
        this.toString = toString;
        this.preferred = preferred;
    }
    
    public boolean in(String version)
    {
        VersionParser vp = VersionParser.getInstance();
        Version v = vp.parseVersion(version);
        return in(v);
    }
    public boolean in(Version version)
    {
        return predicate.test(version);
    }

    public Predicate<Version> getPredicate()
    {
        return predicate;
    }

    public Version getPreferred()
    {
        return preferred;
    }

    @Override
    public String toString()
    {
        return toString;
    }
    
}
