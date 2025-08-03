/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RegexFilter implements Filter
{
    private Pattern pattern;

    public RegexFilter(String regex)
    {
        this.pattern = Pattern.compile(regex);
    }

    public RegexFilter(String regex, int flags)
    {
        this.pattern = Pattern.compile(regex, flags);
    }

    @Override
    public boolean isLoggable(LogRecord record)
    {
        Matcher matcher = pattern.matcher(record.getMessage());
        return matcher.lookingAt();
    }
    
}
