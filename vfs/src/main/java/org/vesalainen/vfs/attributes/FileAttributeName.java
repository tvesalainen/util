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
package org.vesalainen.vfs.attributes;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class FileAttributeName
{
    public static final String SET_UID = "org.vesalainen.vfs:set-uid";
    public static final String SET_GID = "org.vesalainen.vfs:set-gid";
    public static final String STICKY_BIT = "org.vesalainen.vfs:sticky-bit";
    
    public static final String PERMISSIONS = "posix:permissions";
    public static final String GROUP = "posix:group";
    
    public static final String LAST_MODIFIED_TIME = "basic:lastModifiedTime";
    public static final String LAST_ACCESS_TIME = "basic:lastAccessTime";
    public static final String CREATION_TIME = "basic:creationTime";
    public static final String SIZE = "basic:size";
    public static final String IS_REGULAR = "basic:isRegularFile";
    public static final String IS_DIRECTORY = "basic:isDirectory";
    public static final String IS_SYMBOLIC_LINK = "basic:isSymbolicLink";
    public static final String IS_OTHER = "basic:isOther";
    public static final String FILE_KEY = "basic:fileKey";
    
    public static final String OWNER = "basic:owner";
    
    public static final String normalize(String str)
    {
        if (str.indexOf(':') != str.lastIndexOf(':'))
        {
            throw new IllegalArgumentException(str+" not FileAttribute");
        }
        if (str.indexOf(':') == -1)
        {
            return "basic:"+str;
        }
        else
        {
            return str;
        }
    }
    
    public static class FileAttributeNameMatcher
    {
        private Name[] array;

        public FileAttributeNameMatcher(String expr)
        {
            String[] ss = expr.split(",");
            array = new Name[ss.length];
            int len = ss.length;
            for (int ii=0;ii<len;ii++)
            {
                array[ii] = new Name(normalize(ss[ii]));
            }
        }
        public boolean any(String name)
        {
            for (Name n : array)
            {
                if (n.match(new Name(name)))
                {
                    return true;
                }
            }
            return false;
        }
        
    }
    private static class Name
    {
        private String view;
        private String name;

        public Name(String str)
        {
            if ("*".equals(str))
            {
                view = "*";
                name = "*";
            }
            else
            {
                String[] ss = str.split(":");
                view = ss[0];
                name = ss[1];
            }
        }
        public boolean match(Name other)
        {
            return ("*".equals(view) || view.equals(other.view)) &&
                    ("*".equals(name) || name.equals(other.name));
        }
    }
}
