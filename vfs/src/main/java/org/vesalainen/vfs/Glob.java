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

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Regex.Option;
import org.vesalainen.regex.RegexMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.vfs.GlobImpl")
@GrammarDef
public abstract class Glob
{
    @Rule("part+")
    protected String parts(List<String> list)
    {
        return list.stream().collect(Collectors.joining());
    }
    @Rule("literal")
    @Rule("suffixes")
    @Rule("star")
    @Rule("dot")
    @Rule("questionMark")
    @Rule("oneDir")
    @Rule("multiDir")
    @Rule("slash")
    @Rule("bracketExpression")
    protected String part(String part)
    {
        return part;
    }
    @Rule("'\\[' expr '\\]'")
    protected String bracketExpression(String expr)
    {
        if (expr.indexOf('/') != -1)
        {
            throw new IllegalArgumentException("'/' cannot be matched");
        }
        StringBuilder sb = new StringBuilder();
        int len = expr.length();
        int ii=0;
        sb.append('[');
        if (expr.charAt(ii) == '!')
        {
            sb.append('^');
            ii++;
        }
        sb.append(Regex.escape(expr.substring(ii, ii+1)));
        ii++;
        for (;ii<len;ii++)
        {
            char cc = expr.charAt(ii);
            switch (cc)
            {
                case '*':
                    sb.append("\\*");
                    break;
                case '?':
                    sb.append("\\?");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(cc);
            }
        }
        sb.append(']');
        return sb.toString();
    }
    @Terminal(expression="[^\\]/]+")
    protected String expr(String rest)
    {
        return rest;
    }
    @Rule("'\\{' suffix ( '\\,' suffix)* '\\}'")
    protected String suffixes(String prefix, List<String> list)
    {
        list.add(0, prefix);
        return list.stream().map((s)->".*"+s).collect(Collectors.joining("|", "(", ")"));
    }
    @Terminal(expression="/\\*")
    protected String oneDir()
    {
        return "/[^/]*";
    }
    @Terminal(expression="/\\*\\*")
    protected String multiDir()
    {
        return "/.*";
    }
    @Terminal(expression="\\*")
    protected String star()
    {
        return ".*";
    }
    @Terminal(expression="\\.")
    protected String dot()
    {
        return "\\.";
    }
    @Terminal(expression="/")
    protected String slash()
    {
        return "/";
    }
    @Terminal(expression="\\?")
    protected String questionMark()
    {
        return ".?";
    }
    @Terminal(expression="[^\\,\\}/]+")
    protected String suffix(String literal)
    {
        return literal.trim();
    }
    @Terminal(expression="[^\\*\\.\\?\\[\\{/]+")
    protected String literal(String literal)
    {
        return literal;
    }
    @ParseMethod(start="parts")
    public abstract String parse(CharSequence text);
    
    public PathMatcher globMatcher(String expr, Option...options)
    {
        try
        {
            PathMatcher pathMatcher = new PathMatcherImpl(expr, options);
            return pathMatcher;
        }
        catch (IllegalArgumentException ex)
        {
            throw new PatternSyntaxException("syntax error", expr, 0);
        }
    }
    public static  Glob newInstance()
    {
        return (Glob) GenClassFactory.loadGenInstance(Glob.class);
    }
    public class PathMatcherImpl implements PathMatcher
    {
        private boolean wholePath;
        RegexMatcher<Boolean> regexMatcher;

        public PathMatcherImpl(String expression, Option...options)
        {
            this.wholePath = expression.startsWith("/");
            regexMatcher = new RegexMatcher<>(parse(expression), true, options);
            regexMatcher.compile();
        }
        
        @Override
        public boolean matches(Path path)
        {
            if (path == null)
            {
                return false;   // it's root
            }
            Boolean match;
            if (wholePath)
            {
                match = regexMatcher.match(path.toString());
            }
            else
            {
                match = regexMatcher.match(path.getName(path.getNameCount()-1).toString());
            }
            return match == null ? false : match;
        }
        
    }
}
