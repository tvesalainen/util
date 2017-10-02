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

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.vesalainen.util.logging.AttachedLogger;
import static org.vesalainen.vfs.VirtualFile.Type.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystem extends FileSystem implements AttachedLogger
{
    private VirtualFileSystemProvider provider;
    final Map<String,Path> pathCache = new HashMap<>();
    // roots in reverse order so that longest fits first
    private ConcurrentNavigableMap<Root,VirtualFileStore> stores = new ConcurrentSkipListMap<>(Collections.reverseOrder()); 
    private Set<Path> rootSet = Collections.unmodifiableSet(stores.keySet());
    private Collection<FileStore> storeSet = Collections.unmodifiableCollection(stores.values());
    private Root defaultRoot;
    private Set<String> supportedFileAttributeViews = new HashSet<>();
    private Set<String> supportedFileAttributeViewsSecured = Collections.unmodifiableSet(supportedFileAttributeViews);
    private Glob glob;
    private FileAttribute<?>[] defaultRegularAttributes;
    private FileAttribute<?>[] defaultDirectoryAttributes;
    private FileAttribute<?>[] defaultSymbolicLinkAttributes;

    public VirtualFileSystem(VirtualFileSystemProvider provider, Map<String, ?> env)
    {
        this.provider = provider;
        this.defaultRegularAttributes = Env.getDefaultRegularFileAttributes(env);
        this.defaultDirectoryAttributes = Env.getDefaultDirectoryFileAttributes(env);
        this.defaultSymbolicLinkAttributes = Env.getDefaultSymbolicLinkFileAttributes(env);
    }

    public final Root addFileStore(String root, VirtualFileStore store, boolean defaultStore) throws IOException
    {
        fine("addFileStore(%s, %s, %s)", root, store, defaultStore);
        Root r = new Root(this, root);
        stores.put(r, store);
        supportedFileAttributeViews.addAll(store.supportedFileAttributeViews());
        store.create(r, DIRECTORY);
        if (defaultStore)
        {
            if (this.defaultRoot != null)
            {
                throw new IllegalArgumentException("defaultRoot already set");
            }
            this.defaultRoot = r;
        }
        return r;
    }
    public VirtualFileStore getFileStore(Path path)
    {
        if (path.isAbsolute())
        {
            return stores.get(path.getRoot());
        }
        return stores.get(defaultRoot);
    }

    public Root getDefaultRoot()
    {
        return defaultRoot;
    }
    
    @Override
    public FileSystemProvider provider()
    {
        return provider;
    }

    @Override
    public void close() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isReadOnly()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSeparator()
    {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories()
    {
        return rootSet;
    }

    @Override
    public Iterable<FileStore> getFileStores()
    {
        return storeSet;
    }

    @Override
    public Set<String> supportedFileAttributeViews()
    {
        return supportedFileAttributeViewsSecured;
    }

    @Override
    public Path getPath(String first, String... more)
    {
        if (more.length == 0)
        {
            Path p = pathCache.get(first);
            if (p != null)
            {
                return p;
            }
        }
        Root root = null;
        for (Root r : stores.keySet())
        {
            String rest = r.matches(first);
            if (rest != null)
            {
                if (rest.isEmpty())
                {
                    return r;   // it's root
                }
                root = r;
                first = rest;
                break;
            }
        }
        if (first.startsWith(getSeparator()) && root == null)
        {
            throw new InvalidPathException(first, "doesn't start with root like "+stores.keySet(), 0);
        }
        return MultiPath.getInstance(this, root, first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        fine("getPathMatcher(%s)", syntaxAndPattern);
        String[] split = syntaxAndPattern.split(":");
        if (split.length != 2)
        {
            throw new IllegalArgumentException(syntaxAndPattern);
        }
        switch (split[0])
        {
            case "glob":
                if (glob == null)
                {
                    glob = Glob.newInstance();
                }
                return glob.globMatcher(split[1]);
            case "regex":
                return new RegexPathMatcher(split[1]);
            default:
                throw new UnsupportedOperationException(split[0]+" not supported");
        }
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WatchService newWatchService() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Assigns pseudo device, inode and counts nlink attributes
     */
    private boolean enumerated;
    public void enumerateInodes()
    {
        if (!enumerated)
        {
            enumerated = true;
            int dev = 0;
            for (VirtualFileStore store : stores.values())
            {
                store.enumerateInodes(dev++);
            }
        }
    }

    FileAttribute<?>[] getDefaultRegularAttributes()
    {
        return defaultRegularAttributes;
    }

    FileAttribute<?>[] getDefaultDirectoryAttributes()
    {
        return defaultDirectoryAttributes;
    }

    FileAttribute<?>[] getDefaultSymbolicLinkAttributes()
    {
        return defaultSymbolicLinkAttributes;
    }
    
}
