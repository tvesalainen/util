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
package org.vesalainen.vfs.pm;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.vesalainen.vfs.Glob;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PackageFilenameFactory
{
    
    public static Path getPath(Path dir, String type, String packageName, String version, String architecture) throws IOException
    {
        List<PackageFilename> filenames = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, getFilename(type, packageName, version, "*", architecture).toString()))
        {
            ds.forEach((p)->filenames.add(getInstance(p)));
        }
        Collections.sort(filenames);
        if (filenames.isEmpty())
        {
            return getPath(dir, type, packageName, version, 1, architecture);
        }
        else
        {
            PackageFilename last = filenames.get(0);
            int iv = last.getRelease();
            return getPath(dir, type, packageName, version, iv+1, architecture);
        }
    }
    public static Path getPath(Path dir, String type, String packageName, String version, int release, String architecture)
    {
        return dir.resolve(getFilename(type, packageName, version, String.valueOf(release), architecture));
    }
    public static String getFilename(String type, String packageName, String version, String release, String architecture)
    {
        switch (type)
        {
            case "deb":
                return String.format("%s_%s-%s_%s.deb", packageName, version, release, architecture != null ? architecture : "all");
            case "rpm":
                return String.format("%s-%s-%s.%s.rpm", packageName, version, release, architecture != null ? architecture : "noarch");
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    public static PackageFilename getInstance(Path path)
    {
        String pathStr = path.toString();
        if (pathStr.endsWith(".deb"))
        {
            return new DebFilename(path);
        }
        else
        {
            if (pathStr.endsWith(".rpm"))
            {
                return new RpmFilename(path);
            }
            else
            {
                throw new UnsupportedOperationException(path+" not supported");
            }
        }
    }
    public static class RpmFilename extends PackageFilenameImpl
    {
        
        public RpmFilename(Path path)
        {
            this.path = path;
            String[] split = path.getFileName().toString().split("-");
            if (split.length != 3)
            {
                valid = false;
                return;
            }
            String[] split1 = split[2].split("\\.");
            if (split1.length != 3)
            {
                valid = false;
                return;
            }
            packageName = split[0];
            version = split[1];
            try
            {
                release = Integer.parseInt(split1[0]);
            }
            catch (NumberFormatException ex)
            {
                return;
            }
            architecture = split1[1];
            valid = true;
        }

    }
    public static class DebFilename extends PackageFilenameImpl
    {
        
        public DebFilename(Path path)
        {
            this.path = path;
            String[] split = path.getFileName().toString().split("_");
            if (split.length == 3)
            {
                packageName = split[0];
                int idx = split[1].lastIndexOf('-');
                if (idx != -1)
                {
                    version = split[1].substring(0, idx);
                    try
                    {
                        release = Integer.parseInt(split[1].substring(idx+1));
                    }
                    catch (NumberFormatException ex)
                    {
                        return;
                    }
                }
                else
                {
                    version = split[1];
                    release = 0;
                }
                idx = split[2].indexOf(".deb");
                architecture = split[2].substring(0, idx);
                valid = true;
            }
        }

    }
    public static class PackageFilenameImpl implements PackageFilename
    {
        protected Path path;
        protected boolean valid;
        protected String packageName;
        protected String version;
        protected int release;
        protected String architecture;

        @Override
        public Path getPath()
        {
            return path;
        }
        
        @Override
        public boolean isValid()
        {
            return valid;
        }

        @Override
        public String getPackage()
        {
            return packageName;
        }

        @Override
        public String getVersion()
        {
            return version;
        }

        @Override
        public int getRelease()
        {
            return release;
        }

        @Override
        public String getArchitecture()
        {
            return architecture;
        }
        
    }
}
