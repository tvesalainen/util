/*
 * Copyright (C) 2010 Timo Vesalainen
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
package org.vesalainen.util.fi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A helper class for parsing with regex
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */

public class StringParser
{

    private static final Pattern ANY = Pattern.compile(".*");
    private List<MatchResult> stack = new ArrayList<MatchResult>();
    private CharSequence text;
    Matcher matcher;
    /**
     * Constructs a parser for given text
     * @param text
     */
    public StringParser(CharSequence text)
    {
        super();
        this.text = text;
        matcher = ANY.matcher(text);
    }
    /**
     * Finds a pattern. If succeeds the skipped text returnned from skipped() method
     * @param pattern
     * @return
     * @see java.util.regex.Matcher#find
     */
    public boolean find(Pattern pattern)
    {
        matcher.usePattern(pattern);
        int start = matcher.regionStart();
        if (matcher.find())
        {
            stack.add(new MatchResultImpl(matcher));
            matcher.region(matcher.end(), matcher.regionEnd());
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     *
     * @param pattern
     * @return
     * @see java.util.regex.Matcher#lookingAt()
     */
    public boolean lookingAt(Pattern pattern)
    {
        matcher.usePattern(pattern);
        if (matcher.lookingAt())
        {
            stack.add(new MatchResultImpl(matcher));
            matcher.region(matcher.end(), matcher.regionEnd());
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     *
     * @param pattern
     * @return
     * @see java.util.regex.Matcher#matches
     */
    public boolean matches(Pattern pattern)
    {
        matcher.usePattern(pattern);
        if (matcher.matches())
        {
            stack.add(new MatchResultImpl(matcher));
            matcher.region(matcher.end(), matcher.regionEnd());
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * Returns the skipped text after successfull call to find
     * @return
     */
    public String skipped()
    {
        int start = 0;
        if (stack.size() > 1)
        {
            start = stack.get(stack.size() - 2).end();
        }
        int end = stack.get(stack.size() - 1).start();
        return text.subSequence(start, end).toString();
    }
    /**
     * @see MatchResult
     * @return
     */
    public String group()
    {
        return stack.get(stack.size() - 1).group();
    }
    /**
     * @see MatchResult
     * @return
     */
    public String group(int grp)
    {
        return stack.get(stack.size() - 1).group(grp);
    }
    /**
     * Returns the unparsed text
     * @return
     */
    public String remaining()
    {
        return text.subSequence(matcher.regionStart(), matcher.regionEnd()).toString();
    }

    @Override
    public String toString()
    {
        return text.toString();
    }


    public class MatchResultImpl implements MatchResult
    {
        private String[] group;
        private int[] start;
        private int[] end;

        public MatchResultImpl(Matcher mm)
        {
            int count = mm.groupCount()+1;
            group = new String[count];
            start = new int[count];
            end = new int[count];
            for (int ii=0;ii<group.length;ii++)
            {
                group[ii] = mm.group(ii);
                start[ii] = mm.start(ii);
                end[ii] = mm.end(ii);
            }
        }

        @Override
        public int start()
        {
            return start[0];
        }

        @Override
        public int start(int group)
        {
            return start[group];
        }

        @Override
        public int end()
        {
            return end[0];
        }

        @Override
        public int end(int group)
        {
            return end[group];
        }

        @Override
        public String group()
        {
            return group[0];
        }

        @Override
        public String group(int grp)
        {
            return group[grp];
        }

        @Override
        public int groupCount()
        {
            return group.length-1;
        }

    }
}
