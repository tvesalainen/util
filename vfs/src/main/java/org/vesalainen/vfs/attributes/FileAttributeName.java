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

import java.nio.ByteBuffer;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.vesalainen.util.Bijection;
import org.vesalainen.util.HashBijection;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.Lists;
import org.vesalainen.util.MapSet;
import org.vesalainen.vfs.unix.UnixFileAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class FileAttributeName
{
    public static final String BASIC_VIEW = "basic";
    public static final String ACL_VIEW = "acl";
    public static final String POSIX_VIEW = "posix";
    public static final String OWNER_VIEW = "owner";
    public static final String DOS_VIEW = "dos";
    public static final String USER_VIEW = "user";
    public static final String UNIX_VIEW = "org.vesalainen.vfs.unix";
    public static final String DIGEST_VIEW = "org.vesalainen.vfs.digest";
    // digest
    public static final String CONTENT = DIGEST_VIEW+":org.vesalainen.vfs.content";
    public static final String CPIO_CHECKSUM = DIGEST_VIEW+":org.vesalainen.vfs.cpiochecksum";
    public static final String CRC32 = DIGEST_VIEW+":org.vesalainen.vfs.crc32";
    public static final String MD5 = DIGEST_VIEW+":org.vesalainen.vfs.md5";
    public static final String SHA1 = DIGEST_VIEW+":org.vesalainen.vfs.sha1";
    public static final String SHA256 = DIGEST_VIEW+":org.vesalainen.vfs.sha256";
    
    public static final String DEVICE = UNIX_VIEW+":org.vesalainen.vfs.device";
    public static final String INODE = UNIX_VIEW+":org.vesalainen.vfs.inode";
    public static final String NLINK = UNIX_VIEW+":org.vesalainen.vfs.nlink";
    public static final String SETUID = UNIX_VIEW+":org.vesalainen.vfs.setuid";
    public static final String SETGID = UNIX_VIEW+":org.vesalainen.vfs.setgid";
    public static final String STICKY = UNIX_VIEW+":org.vesalainen.vfs.sticky";
    // posix
    public static final String PERMISSIONS = "posix:permissions";
    public static final String GROUP = "posix:group";
    // basic
    public static final String LAST_MODIFIED_TIME = "basic:lastModifiedTime";
    public static final String LAST_ACCESS_TIME = "basic:lastAccessTime";
    public static final String CREATION_TIME = "basic:creationTime";
    public static final String SIZE = "basic:size";
    public static final String IS_REGULAR = "basic:isRegularFile";
    public static final String IS_DIRECTORY = "basic:isDirectory";
    public static final String IS_SYMBOLIC_LINK = "basic:isSymbolicLink";
    public static final String IS_OTHER = "basic:isOther";
    public static final String FILE_KEY = "basic:fileKey";
    
    public static final String OWNER = "owner:owner";
    
    public static final String ACL = "acl:acl";
    
    public static final String READONLY = "dos:readonly";
    public static final String HIDDEN = "dos:hidden";
    public static final String SYSTEM = "dos:system";
    public static final String ARCHIVE = "dos:archive";

    private static final Map<String,Class<?>> types;
    private static final Bijection<String,Class<? extends FileAttributeView>> nameView;
    private static final MapSet<String,String> impliesMap = new HashMapSet<>();
    private static final Map<String,Name> nameMap = new HashMap<>();
    
    static
    {
        addName(DEVICE);
        addName(INODE);
        addName(NLINK);
        addName(SETUID);
        addName(SETGID);
        addName(STICKY);
        // posix
        addName(PERMISSIONS);
        addName(GROUP);
        // basic
        addName(LAST_MODIFIED_TIME);
        addName(LAST_ACCESS_TIME);
        addName(CREATION_TIME);
        addName(SIZE);
        addName(IS_REGULAR);
        addName(IS_DIRECTORY);
        addName(IS_SYMBOLIC_LINK);
        addName(IS_OTHER);
        addName(FILE_KEY);

        addName(OWNER);

        addName(ACL);

        addName(READONLY);
        addName(HIDDEN);
        addName(SYSTEM);
        addName(ARCHIVE);

        addName(CONTENT);
        addName(CPIO_CHECKSUM);
        addName(CRC32);
        addName(MD5);
        addName(SHA1);
        addName(SHA256);
        
        types = new HashMap<>();
        types.put(DEVICE, Integer.class);
        types.put(INODE, Integer.class);
        types.put(NLINK, Integer.class);
        types.put(SETUID, Boolean.class);
        types.put(SETGID, Boolean.class);
        types.put(STICKY, Boolean.class);

        types.put(PERMISSIONS, Set.class);
        types.put(GROUP, GroupPrincipal.class);
        types.put(OWNER, UserPrincipal.class);

        types.put(LAST_MODIFIED_TIME, FileTime.class);
        types.put(LAST_ACCESS_TIME, FileTime.class);
        types.put(CREATION_TIME, FileTime.class);
        types.put(SIZE, Long.class);
        types.put(IS_REGULAR, Boolean.class);
        types.put(IS_DIRECTORY, Boolean.class);
        types.put(IS_SYMBOLIC_LINK, Boolean.class);
        types.put(IS_OTHER, Boolean.class);
        types.put(FILE_KEY, Boolean.class);
        
        nameView = new HashBijection<>();
        nameView.put(BASIC_VIEW, BasicFileAttributeView.class);
        nameView.put(ACL_VIEW, AclFileAttributeView.class);
        nameView.put(DOS_VIEW, DosFileAttributeView.class);
        nameView.put(OWNER_VIEW, FileOwnerAttributeView.class);
        nameView.put(POSIX_VIEW, PosixFileAttributeView.class);
        nameView.put(USER_VIEW, UserDefinedFileAttributeView.class);
        nameView.put(UNIX_VIEW, UnixFileAttributeView.class);
        nameView.put(DIGEST_VIEW, DigestFileAttributeView.class);
        
        initImplies(BASIC_VIEW);
        initImplies(ACL_VIEW);
        initImplies(DOS_VIEW);
        initImplies(OWNER_VIEW);
        initImplies(POSIX_VIEW);
        initImplies(USER_VIEW);
        initImplies(UNIX_VIEW);
        initImplies(DIGEST_VIEW);
    }
    private static void initImplies(String view)
    {
        Class<? extends FileAttributeView> viewClass = nameView.getSecond(view);
        initImplies(view, viewClass);
    }
    private static void initImplies(String view, Class<? extends FileAttributeView> viewClass)
    {
        String name = nameView.getFirst(viewClass);
        if (name != null)
        {
            impliesMap.add(view, name);
        }
        for (Class<?> itf : viewClass.getInterfaces())
        {
            if (FileAttributeView.class.isAssignableFrom(itf))
            {
                initImplies(view, (Class<? extends FileAttributeView>) itf);
            }
        }
    }
    /**
     * Returns a set that contains given views as well as all implied views.
     * @param views
     * @return 
     */
    public static final Set<String> impliedSet(String... views)
    {
        Set<String> set = new HashSet<>();
        for (String view : views)
        {
            set.addAll(impliesMap.get(view));
        }
        return set;
    }
    public static final Set<String> topViews(Set<String> views)
    {
        return topViews(Lists.toArray(views, String.class));
    }
    public static final Set<String> topViews(String... views)
    {
        Set<String> set = Arrays.stream(views).collect(Collectors.toSet());
        int len = views.length;
        for (int ii=0;ii<len;ii++)
        {
            String view = views[ii];
            for (int jj=0;jj<len;jj++)
            {
                if (ii != jj)
                {
                    Set<String> is = impliesMap.get(views[jj]);
                    if (is.contains(view))
                    {
                        set.remove(view);
                    }
                }
            }
        }
        return set;
    }
    public static final Class<?> type(Name name)
    {
        return types.get(name.toString());
    }
    public static final void check(Name name, Object value)
    {
        Objects.requireNonNull(value, "value can't be null");
        if (DIGEST_VIEW.equals(name.view))
        {
            if (!value.getClass().equals(byte[].class) && !(value instanceof ByteBuffer))
            {
                throw new ClassCastException(value+" not expected type byte[]/ByteBuffer");
            }
        }
        else
        {
            if (USER_VIEW.equals(name.view))
            {
                if (!value.getClass().equals(byte[].class) && !(value instanceof ByteBuffer))
                {
                    throw new ClassCastException(value+" not expected type byte[]/ByteBuffer");
                }
            }
            else
            {
                Class<?> type = FileAttributeName.type(name);
                if (type == null || !type.isAssignableFrom(value.getClass()))
                {
                    throw new ClassCastException(value+" not expected type "+type);
                }
            }
        }
    }

    private static void addName(String attr)
    {
        Name name = getInstance(attr);
        Name old = nameMap.put(name.name, name);
        if (old != null)
        {
            throw new IllegalArgumentException(name+" ambiguous");
        }
    }

    public static class FileAttributeNameMatcher
    {
        private Set<String> views;
        private String[] attributes;

        public FileAttributeNameMatcher(String expr)
        {
            String view = BASIC_VIEW;
            int idx = expr.indexOf(':');
            if (idx != -1)
            {
                view = expr.substring(0, idx);
                expr = expr.substring(idx+1);
            }
            if (expr.startsWith("*"))
            {
                if (expr.length() != 1)
                {
                    throw new IllegalArgumentException(expr+" invalid");
                }
            }
            else
            {
                attributes = expr.split(",");
            }
            views = impliesMap.get(view);
            if (views == null)
            {
                throw new UnsupportedOperationException(view+" not supported");
            }
        }
        public boolean any(String name)
        {
            return any(getInstance(name));
        }
        public boolean any(Name name)
        {
            if (!views.contains(name.view))
            {
                return false;
            }
            if (attributes == null)
            {
                return true;
            }
            else
            {
                for (String at : attributes)
                {
                    if (at.equals(name.name))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        
    }
    
    public static final Name getInstance(String attribute)
    {
        Name name = nameMap.get(attribute);
        if (name == null)
        {
            String view;
            String attr;
            int idx = attribute.indexOf(':');
            if (idx != -1)
            {
                view = attribute.substring(0, idx);
                attr = attribute.substring(idx+1);
                name = new Name(view, attr);
            }
            else
            {
                view = BASIC_VIEW;
                attr = attribute;
                name = new Name(view, attr);
                nameMap.put(attr, name);
            }
            nameMap.put(attribute, name);
        }
        return name;
    }
    public static class Name
    {
        private String view;
        private String name;
        private String string;

        public Name(String view, String name)
        {
            this.view = view;
            this.name = name;
            this.string = view+':'+name;
        }

        public String getView()
        {
            return view;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return string;
        }

    }
}
