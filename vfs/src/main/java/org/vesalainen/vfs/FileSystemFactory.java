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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.RegexMatcher;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class FileSystemFactory implements AttachedLogger
{
    private static RegexMatcher<Class<? extends VirtualFileSystem>> matcher;
    private static final Glob GLOB = Glob.newInstance();
    
    protected VirtualFileSystemProvider provider;
    protected Path path;
    protected Map<String, ?> env;

    public FileSystemFactory(VirtualFileSystemProvider provider, Path path, Map<String, ?> env)
    {
        this.provider = provider;
        this.path = path;
        this.env = env;
    }

    public abstract FileSystem create() throws IOException;
    
    public static final void register(String expression, Class<? extends VirtualFileSystem> fileSystem)
    {
        if (matcher == null)
        {
            matcher = new RegexMatcher(expression, fileSystem, Regex.Option.CASE_INSENSITIVE);
        }
        else
        {
            if (matcher.isCompiled())
            {
                throw new IllegalStateException("cannot register after getInstance call");
            }
            matcher.addExpression(expression, fileSystem, Regex.Option.CASE_INSENSITIVE);
        }
    }
    
    public static final FileSystem getInstance(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        if (!matcher.isCompiled())
        {
            matcher.compile();
        }
        JavaLogging logger = JavaLogging.getLogger(FileSystemFactory.class);
        try
        {
            logger.fine("getInstance(%s)", path);
            Class<? extends VirtualFileSystem> cls = matcher.match(path.getFileName().toString());
            if (cls == null)
            {
                throw new UnsupportedOperationException(path+" not supported");
            }
            Constructor<? extends VirtualFileSystem> constructor = cls.getConstructor(VirtualFileSystemProvider.class, Path.class, Map.class);
            return constructor.newInstance(provider, path, env);
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            logger.log(Level.SEVERE, ex, "%s", ex.getMessage());
            throw new IOException(ex);
        }
    }
}
