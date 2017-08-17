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
package org.vesalainen.util.jar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Helper for creating jar files
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JarBuilder implements Closeable
{
    private JarOutputStream jar;

    public JarBuilder(File file) throws IOException
    {
        this(new FileOutputStream(file));
    }

    public JarBuilder(OutputStream os) throws IOException
    {
        this(new BufferedOutputStream(os));
    }

    public JarBuilder(BufferedOutputStream out) throws IOException
    {
        this.jar = new JarOutputStream(out);
    }
    
    public void addEntry(String name, byte[] data) throws IOException
    {
        jar.putNextEntry(new JarEntry(name));
        jar.write(data);
    }
    
    public void addEntry(String name, File file) throws IOException
    {
        addEntry(name, new FileInputStream(file));
    }
    
    public void addEntry(String name, InputStream is) throws IOException
    {
        addEntry(name, new BufferedInputStream(is));
    }
    
    public void addEntry(String name, BufferedInputStream is) throws IOException
    {
        jar.putNextEntry(new JarEntry(name));
        byte[] buf = new byte[4096];
        int rc = is.read(buf);
        while (rc != -1)
        {
            jar.write(buf, 0, rc);
            rc = is.read(buf);
        }
    }
    
    @Override
    public void close() throws IOException
    {
        jar.close();
    }
    
}
