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
package org.vesalainen.pm.deb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.vesalainen.nio.file.PathHelper;
import org.vesalainen.pm.ComponentBuilder;
import org.vesalainen.pm.FileUse;
import org.vesalainen.pm.PackageBuilder;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class MultiFileBuilder implements ComponentBuilder
{
    private PackageBuilder packageBuilder;
    private Path sourceDirectory;
    private Stream<Path> stream;
    private Path relativeTarget;
    private FileUse[] use;
    private String copyright;
    private String license;
    private List<FileAttribute> attrs = new ArrayList<>();
    private String groupname;
    private String perms;
    private Instant lastModifiedTime;
    private Instant lastAccessTime;
    private Instant creationTime;
    private String owner;

    public MultiFileBuilder(PackageBuilder packageBuilder, Path sourceDirectory, Stream<Path> stream, Path relativeTarget)
    {
        this.packageBuilder = packageBuilder;
        this.sourceDirectory = sourceDirectory;
        this.stream = stream;
        this.relativeTarget = relativeTarget;
    }

    @Override
    public ComponentBuilder setUsage(FileUse... use)
    {
        this.use = use;
        return this;
    }

    @Override
    public ComponentBuilder addFileAttributes(FileAttribute<?>... attrs)
    {
        CollectionHelp.addAll(this.attrs, attrs);
        return this;
    }

    @Override
    public ComponentBuilder setCopyright(String copyright)
    {
        this.copyright = copyright;
        return this;
    }

    @Override
    public ComponentBuilder setLicense(String license)
    {
        this.license = license;
        return this;
    }

    @Override
    public ComponentBuilder setOwner(String name) throws IOException
    {
        this.owner = name;
        return this;
    }

    @Override
    public ComponentBuilder setCreationTime(Instant time)
    {
        this.creationTime = time;
        return this;
    }

    @Override
    public ComponentBuilder setLastAccessTime(Instant time)
    {
        this.lastAccessTime = time;
        return this;
    }

    @Override
    public ComponentBuilder setLastModifiedTime(Instant time)
    {
        this.lastModifiedTime = time;
        return this;
    }

    @Override
    public ComponentBuilder setPermissions(String perms)
    {
        this.perms = perms;
        return this;
    }

    @Override
    public ComponentBuilder setGroup(String groupname) throws IOException
    {
        this.groupname = groupname;
        return this;
    }

    @Override
    public void build() throws IOException
    {
        stream.forEach((p)->
        {
            Path res = PathHelper.transform(sourceDirectory, relativeTarget, p);
            ComponentBuilder builder = null;
            try
            {
                if (Files.isRegularFile(p))
                {
                        builder = packageBuilder.addFile(p, res);
                }
                else
                {
                    if (Files.isDirectory(p))
                    {
                        builder = packageBuilder.addDirectory(res);
                    }
                    else
                    {
                        if (Files.isSymbolicLink(p))
                        {
                            Path linkTarget = PathHelper.transform(sourceDirectory, relativeTarget, Files.readSymbolicLink(p));
                            builder = packageBuilder.addSymbolicLink(p, linkTarget);
                        }
                        else
                        {
                            throw new IllegalArgumentException(p+" unknown file type");
                        }
                    }
                }
                if (creationTime != null)
                {
                    builder.setCreationTime(creationTime);
                }
                if (lastAccessTime != null)
                {
                    builder.setLastAccessTime(lastAccessTime);
                }
                if (lastModifiedTime != null)
                {
                    builder.setLastModifiedTime(lastModifiedTime);
                }
                if (owner != null)
                {
                    builder.setOwner(owner);
                }
                if (groupname != null)
                {
                    builder.setGroup(groupname);
                }
                if (copyright != null)
                {
                    builder.setCopyright(copyright);
                }
                if (license != null)
                {
                    builder.setLicense(license);
                }
                if (perms != null)
                {
                    builder.setPermissions(perms);
                }
                if (use != null)
                {
                    builder.setUsage(use);
                }
                if (!attrs.isEmpty())
                {
                    builder.addFileAttributes(CollectionHelp.toArray(attrs, FileAttribute.class));
                }
                builder.build();
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    
}
