/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.rpm.deb;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.channels.FileChannel.MapMode.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.rpm.FileFlag;
import static org.vesalainen.rpm.FileFlag.*;

/**
 * DEBBuilder class supports building structure for building debian packet.
 * Use dpkg-buildpackage -us -uc for building of actual packet.
 * @author tkv
 * @see <a href="https://www.debian.org/doc/manuals/maint-guide/index.en.html">Debian New Maintainers' Guide</a>
 * @see <a href="https://www.debian.org/doc/debian-policy/index.html">Debian Policy Manual</a>
 */
public class DEBBuilder
{
    public static final String STANDARDS_VERSION = "4.0.1.0";
    public static final String SOURCE_FORMAT = "1.0\n";
    private static final String INTERPRETER = "/bin/sh";
    private Path dir;
    private String name;
    private String version;
    private String release;
    private String maintainer;
    private Path debian;
    private Copyright copyright;
    private Control control;
    private int compatibility = 9;
    private Set<MaintainerScript> maintainerScripts = new HashSet<>();
    private ChangeLog changeLog;
    private Conffiles conffiles;
    private Docs docs;
    private FileTime now = FileTime.from(Instant.now());

    public DEBBuilder(Path base, String name, String version, String release, String maintainer)
    {
        this.dir = base.resolve(name+"-"+version);
        this.name = name;
        this.version = version;
        this.release = release;
        this.maintainer = maintainer;
        this.debian = dir.resolve("debian");
        control = new Control(debian, name);
        control.setStandardsVersion(STANDARDS_VERSION);
        copyright = new Copyright(debian);
        changeLog = new ChangeLog(debian, name, version, release, maintainer);
        conffiles = new Conffiles(debian);
        docs = new Docs(debian);
    }
    public void addDocumentationFile(String filepath)
    {
        docs.addFile(filepath);
    }
    public void addConfigurationFile(String filepath)
    {
        conffiles.addFile(filepath);
    }
    public void setPostInst(String script)
    {
        setPostInst(script, INTERPRETER);
    }
    public void setPostInst(String script, String interpreter)
    {
        setMaintainerScript("postinst", script, interpreter);
    }
    public void setPreInst(String script)
    {
        setPreInst(script, INTERPRETER);
    }
    public void setPreInst(String script, String interpreter)
    {
        setMaintainerScript("preinst", script, interpreter);
    }
    public void setPostRm(String script)
    {
        setPostRm(script, INTERPRETER);
    }
    public void setPostRm(String script, String interpreter)
    {
        setMaintainerScript("postrm", script, interpreter);
    }
    public void setPreRm(String script)
    {
        setPreRm(script, INTERPRETER);
    }
    public void setPreRm(String script, String interpreter)
    {
        setMaintainerScript("prerm", script, interpreter);
    }
    private void setMaintainerScript(String name, String script, String interpreter)
    {
        maintainerScripts.add(new MaintainerScript(dir, name, script, interpreter));
    }
    
    public Control control()
    {
        return control;
    }

    public Copyright copyright()
    {
        return copyright;
    }
    
    public void build() throws IOException
    {
        copyright.save();
        control.save();

        changeLog.save();
        // compat
        Path compat = debian.resolve("compat");
        try (BufferedWriter bf = Files.newBufferedWriter(compat, UTF_8))
        {
            bf.append(String.format("%d\n", compatibility));
        }
        for (MaintainerScript ms : maintainerScripts)
        {
            ms.save();
        }
        // rules
        Path rules = debian.resolve("rules");
        FileUtil.copyResource("/rules", rules, DEBBuilder.class);
        PosixHelp.setPermission(rules, "-rwxr-xr-x");
        // source/format
        Path source = debian.resolve("source");
        Files.createDirectories(source);
        Path format = source.resolve("format");
        try (BufferedWriter bf = Files.newBufferedWriter(format, UTF_8))
        {
            bf.append(SOURCE_FORMAT);
        }
        conffiles.save();
        docs.save();
    }
    
    public FileBuilder addFile(String target, Path file) throws IOException
    {
        return new FileBuilder(target, file);
    }
    public FileBuilder addFile(String target, ByteBuffer content) throws IOException
    {
        return new FileBuilder(target, content);
    }
    public class FileBuilder
    {
        private String target;
        private ByteBuffer content; 
        private String user = "root";
        private String group = "root";
        private short mode;
        private FileTime creationTime = now;
        private FileTime lastAccessTime = now;
        private FileTime lastModifiedTime = now;
        private FileFlag[] flags = new FileFlag[]{};

        public FileBuilder(String target, Path file) throws IOException
        {
            this.target = target.startsWith("/") ? target.substring(1) : target;
            try (FileChannel fc = FileChannel.open(file, READ))
            {
                this.content = fc.map(READ_ONLY, 0, Files.size(file));
            }
            this.user = PosixHelp.getOwner(file);
            this.group = PosixHelp.getGroup(file);
            this.mode = PosixHelp.getMode(PosixHelp.getModeString(file));
            this.creationTime = FileUtil.getCreationTime(file);
            this.lastAccessTime = FileUtil.getLastAccessTime(file);
            this.lastModifiedTime = FileUtil.getLastModifiedTime(file);
        }

        public FileBuilder(String target, ByteBuffer content)
        {
            this.target = target.startsWith("/") ? target.substring(1) : target;
            this.content = content;
        }

        public FileBuilder setUser(String user)
        {
            this.user = user;
            return this;
        }

        public FileBuilder setGroup(String group)
        {
            this.group = group;
            return this;
        }

        public FileBuilder setMode(String mode)
        {
            this.mode = PosixHelp.getMode(mode);    // to make early check
            return this;
        }

        public FileBuilder setCreationTime(FileTime creationTime)
        {
            this.creationTime = creationTime;
            return this;
        }

        public FileBuilder setLastAccessTime(FileTime lastAccessTime)
        {
            this.lastAccessTime = lastAccessTime;
            return this;
        }

        public FileBuilder setLastModifiedTime(FileTime lastModifiedTime)
        {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public FileBuilder setFlags(FileFlag... flags)
        {
            this.flags = flags;
            return this;
        }
        
        public void build() throws IOException
        {
            if (FileFlag.isSet(CONFIG, flags))
            {
                addConfigurationFile(target);
            }
            if (FileFlag.isSet(DOC, flags))
            {
                addDocumentationFile(target);
            }
            Path path = dir.resolve(target);
            Files.createDirectories(path.getParent());
            try (FileChannel fc = FileChannel.open(path, WRITE, CREATE))
            {
                fc.write(content);
            }
            PosixHelp.setOwner(user, path);
            PosixHelp.setGroup(group, path);
            PosixHelp.setPermission(path, PosixHelp.toString(mode));
            FileUtil.setTimes(lastModifiedTime, lastAccessTime, creationTime, path);
        }
    }
}
