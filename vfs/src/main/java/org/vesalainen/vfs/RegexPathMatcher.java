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
package org.vesalainen.vfs;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.PatternSyntaxException;
import org.vesalainen.regex.Regex.Option;
import org.vesalainen.regex.RegexMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class RegexPathMatcher implements PathMatcher
{
    private RegexMatcher<Boolean> matcher;
    
    public RegexPathMatcher(String expr, Option... options)
    {
        try
        {
            matcher = new RegexMatcher<>(expr, true, options);
            matcher.compile();
        }
        catch (IllegalArgumentException ex)
        {
            throw new PatternSyntaxException("syntax error", expr, 0);
        }
    }

    @Override
    public boolean matches(Path path)
    {
        Boolean match = matcher.match(path.toString());
        return match == null ? false : match;
    }
    public static final Filter<Path> createFilter(String expr, Option... options)
    {
        RegexPathMatcher matcher = new RegexPathMatcher(expr, options);
        return (p)->matcher.matches(p);
    }
}
