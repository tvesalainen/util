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
package org.vesalainen.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SecuredFile extends JavaLogging
{
    private static final String Suffix = ".safe";
    private Path path;
    private Path safePath;

    public SecuredFile(Path path)
    {
        this(path, Suffix);
    }
    public SecuredFile(Path path, String suffix)
    {
        this(path.toFile(), suffix);
    }
    public SecuredFile(File file)
    {
        this(file, Suffix);
    }
    public SecuredFile(File file, String suffix)
    {
        super(SecuredFile.class);
        this.path = file.toPath();
        this.safePath = new File(file.getPath()+suffix).toPath();
    }

    public Path getPath()
    {
        return path;
    }

    public Path getSafePath()
    {
        return safePath;
    }
    
    public boolean exists()
    {
        return Files.exists(path);
    }
    public void load(Loader loader) throws IOException
    {
        try
        {
            try (InputStream is = Files.newInputStream(path))
            {
                loader.load(is);
                fine("secure loaded from %s", path);
            }
            Files.deleteIfExists(safePath);
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, ex, "secure loading %s %s", path, ex.getMessage());
            try (InputStream is = Files.newInputStream(safePath))
            {
                try
                {
                    loader.load(is);
                    fine("secure loaded from %s", safePath);
                }
                catch (Exception ex1)
                {
                    log(Level.SEVERE, ex, "secure loading %s %s", safePath, ex.getMessage());
                    throw new IOException(ex);
                }
            }
            Files.move(safePath, path, ATOMIC_MOVE, REPLACE_EXISTING);
        }
    }
    
    public void save(Saver saver) throws IOException
    {
        if (Files.exists(path))
        {
            Files.move(path, safePath, ATOMIC_MOVE, REPLACE_EXISTING);
        }
        try (OutputStream os = Files.newOutputStream(path))
        {
            saver.save(os);
            fine("secure saved to %s", path);
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, ex, "secure saving %s %s", path, ex.getMessage());
            throw new IOException(ex);
        }
    }
    
    @FunctionalInterface
    public interface Loader
    {
        void load(InputStream is) throws Exception;
    }
    @FunctionalInterface
    public interface Saver
    {
        void save(OutputStream os) throws Exception;
    }
}
