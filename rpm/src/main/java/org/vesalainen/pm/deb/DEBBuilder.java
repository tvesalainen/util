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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.channels.FileChannel.MapMode.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.pm.ComponentBuilder;
import org.vesalainen.pm.Condition;
import org.vesalainen.pm.FileUse;
import static org.vesalainen.pm.FileUse.*;
import org.vesalainen.pm.PackageBuilder;
import org.vesalainen.pm.deb.Copyright.FileCopyright;
import org.vesalainen.util.ArrayHelp;
import org.vesalainen.util.OSProcess;

/**
 * DEBBuilder class supports building structure for building debian packet.
 * Use dpkg-buildpackage -us -uc for building of actual packet.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://www.debian.org/doc/manuals/maint-guide/index.en.html">Debian New Maintainers' Guide</a>
 * @see <a href="https://www.debian.org/doc/debian-policy/index.html">Debian Policy Manual</a>
 */
public class DEBBuilder implements PackageBuilder
{
    public static final String STANDARDS_VERSION = "4.0.1.0";
    public static final String SOURCE_FORMAT = "1.0\n";
    protected static final String INTERPRETER = "/bin/sh";
    protected Path dir;
    protected String name;
    protected String version;
    protected String release;
    protected String maintainer;
    protected Path debian;
    protected Copyright copyright;
    protected Control control;
    protected int compatibility = 9;
    protected Set<MaintainerScript> maintainerScripts = new HashSet<>();
    protected ChangeLog changeLog;
    protected Conffiles conffiles;
    protected Docs docs;
    protected FileTime now = FileTime.from(Instant.now());
    protected List<FileBuilder> fileBuilders = new ArrayList<>();

    public DEBBuilder()
    {
        control = new Control();
        copyright = new Copyright();
        changeLog = new ChangeLog();
        conffiles = new Conffiles();
        docs = new Docs();
    }

    /**
     * Returns "deb"
     * @return 
     */
    @Override
    public String getPackageBuilderName()
    {
        return "deb";
    }

    private void addDocumentationFile(String filepath)
    {
        docs.addFile(filepath);
    }
    private void addConfigurationFile(String filepath)
    {
        conffiles.addFile(filepath);
    }
    /**
     * Returns "/bin/sh"
     * @return 
     */
    @Override
    public String getDefaultInterpreter()
    {
        return INTERPRETER;
    }
    /**
     * Set debian/postinst
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public DEBBuilder setPostInstallation(String script, String interpreter)
    {
        setMaintainerScript("postinst", script, interpreter);
        return this;
    }
    /**
     * Set debian/preinst
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public DEBBuilder setPreInstallation(String script, String interpreter)
    {
        setMaintainerScript("preinst", script, interpreter);
        return this;
    }
    /**
     * Set debian/postrm
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public DEBBuilder setPostUnInstallation(String script, String interpreter)
    {
        setMaintainerScript("postrm", script, interpreter);
        return this;
    }
    /**
     * Set debian/prerm
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public DEBBuilder setPreUnInstallation(String script, String interpreter)
    {
        setMaintainerScript("prerm", script, interpreter);
        return this;
    }
    private void setMaintainerScript(String name, String script, String interpreter)
    {
        maintainerScripts.add(new MaintainerScript(debian, name, script, interpreter));
    }
    /**
     * Add debian/control Conflicts field.
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public DEBBuilder addConflict(String name, String version, Condition... dependency)
    {
        control.addConflict(release, version, dependency);
        return this;
    }
    /**
     * Add debian/control Depends field.
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public DEBBuilder addRequire(String name, String version, Condition... dependency)
    {
        control.addDepends(release, version, dependency);
        return this;
    }
    /**
     * Add debian/control Provides field.
     * @param name
     * @param version Not used
     * @param dependency Not used
     * @return 
     */
    @Override
    public DEBBuilder addProvide(String name, String version, Condition... dependency)
    {
        control.addProvides(name);
        return this;
    }
    /**
     * Add debian/control Architecture field.
     * @param architecture
     * @return 
     */
    @Override
    public DEBBuilder setArchitecture(String architecture)
    {
        control.setArchitecture(architecture);
        return this;
    }
    /**
     * Add debian/control Description field.
     * @param description
     * @return 
     */
    @Override
    public DEBBuilder setDescription(String description)
    {
        control.setDescription(description);
        return this;
    }
    /**
     * Add debian/copyright Copyright field.
     * @param cr
     * @return 
     */
    @Override
    public DEBBuilder setCopyright(String cr)
    {
        copyright.setCopyright(cr);
        return this;
    }
    /**
     * Add debian/copyright License field.
     * @param license
     * @return 
     */
    @Override
    public DEBBuilder setLicense(String license)
    {
        copyright.setLicense(license);
        return this;
    }
    /**
     * Add debian/control Maintainer field. 
     * @param maintainer
     * @return 
     */
    @Override
    public PackageBuilder setMaintainer(String maintainer)
    {
        this.maintainer = maintainer;
        control.setMaintainer(maintainer);
        return this;
    }
    /**
     * Add debian/control Package field.
     * @param name
     * @return 
     */
    @Override
    public DEBBuilder setPackageName(String name)
    {
        this.name = name;
        control.setPackage(name);
        return this;
    }
    /**
     * Does nothing
     * @param os
     * @return 
     */
    @Override
    public DEBBuilder setOperatingSystem(String os)
    {
        return this;
    }
    /**
     * Set debian/changelog release
     * @param release
     * @return 
     */
    @Override
    public DEBBuilder setRelease(String release)
    {
        this.release = release;
        return this;
    }
    /**
     * Does nothing
     * @param summary
     * @return 
     */
    @Override
    public DEBBuilder setSummary(String summary)
    {
        return this;
    }
    /**
     * Set debian/changelog version
     * @param version
     * @return 
     */
    @Override
    public DEBBuilder setVersion(String version)
    {
        this.version = version;
        return this;
    }
    /**
     * Set debian/control Section field.
     * @param area
     * @return 
     */
    @Override
    public DEBBuilder setApplicationArea(String area)
    {
        control.setSection(area);
        return this;
    }
    /**
     * Set debian/control Priority field.
     * @param priority
     * @return 
     */
    @Override
    public PackageBuilder setPriority(String priority)
    {
        control.setPriority(priority);
        return this;
    }
    /**
     * Add directory to package.
     * @param target
     * @return
     * @throws IOException 
     */
    @Override
    public ComponentBuilder addDirectory(String target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb = new FileBuilder(target);
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add symbolic link to package.
     * @param target
     * @param linkTarget
     * @return
     * @throws IOException 
     */
    @Override
    public ComponentBuilder addSymbolicLink(String target, String linkTarget) throws IOException
    {
        checkTarget(target);
        checkTarget(linkTarget);
        FileBuilder fb = new FileBuilder(target, linkTarget);
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add regular file to package.
     * @param source
     * @param target
     * @return
     * @throws IOException 
     */
    @Override
    public FileBuilder addFile(Path source, String target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb = new FileBuilder(target, source);
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add regular file to package.
     * @param content
     * @param target
     * @return
     * @throws IOException 
     */
    @Override
    public ComponentBuilder addFile(ByteBuffer content, String target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb = new FileBuilder(target, content);
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Creates name-version directory creates debian source files and runs 
     * dpkg-buildpackage -us -uc
     * @param base
     * @return
     * @throws IOException 
     */
    @Override
    public Path build(Path base) throws IOException
    {
        this.dir = base.resolve(name+"-"+version);
        this.debian = dir.resolve("debian");
        control.setStandardsVersion(STANDARDS_VERSION);
        changeLog.set(name, version, release, maintainer);
        control.setSource(name);
        for (FileBuilder fb : fileBuilders)
        {
            fb.build();
        }
        copyright.save(debian);
        control.save(debian);

        changeLog.save(debian);
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
        conffiles.save(debian);
        docs.save(debian);
        try
        {
            OSProcess.call(dir, null, "dpkg-buildpackage -us -uc");
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex);
        }
        return dir;
    }
    
    public class FileBuilder implements ComponentBuilder
    {
        private String target;
        private String linkTarget;
        private ByteBuffer content; 
        private FileUse[] usage = new FileUse[]{};
        private Map<String,Object> attributeMap = new HashMap<>();
        private FileCopyright fileCopyright;
        /**
         * Creates directory
         * @param target 
         */
        public FileBuilder(String target)
        {
            this.target = target;
        }
        /**
         * Creates symbolic link.
         * @param target
         * @param linkTarget 
         */
        public FileBuilder(String target, String linkTarget)
        {
            this.target = target;
            this.linkTarget = linkTarget;
        }
        /**
         * Creates regular file from file contents
         * @param target
         * @param file
         * @throws IOException 
         */
        public FileBuilder(String target, Path file) throws IOException
        {
            this.target = target.startsWith("/") ? target.substring(1) : target;
            try (FileChannel fc = FileChannel.open(file, READ))
            {
                this.content = fc.map(READ_ONLY, 0, Files.size(file));
            }
            attributeMap.putAll(Files.readAttributes(file, "*"));
        }
        /**
         * Creates regular file from given content.
         * @param target
         * @param content 
         */
        public FileBuilder(String target, ByteBuffer content)
        {
            this.target = target.startsWith("/") ? target.substring(1) : target;
            this.content = content;
        }
        /**
         * Gathers FileAttributes from default methods.
         * @param attrs
         * @return 
         */
        @Override
        public FileBuilder addFileAttributes(FileAttribute<?>... attrs)
        {
            for (FileAttribute<?> fa : attrs)
            {
                attributeMap.put(fa.name(), fa.value());
            }
            return this;
        }
        /**
         * Set file usage in package.
         * @param use
         * @return 
         */
        @Override
        public FileBuilder setUsage(FileUse... use)
        {
            this.usage = use;
            return this;
        }
        /**
         * Set files copyright.
         * @param cr
         * @return 
         */
        @Override
        public ComponentBuilder setCopyright(String cr)
        {
            if (fileCopyright == null)
            {
                fileCopyright = copyright.addFile(target);
            }
            fileCopyright.addCopyright(cr);
            return this;
        }
        /**
         * Set files license.
         * @param license
         * @return 
         */
        @Override
        public ComponentBuilder setLicense(String license)
        {
            if (fileCopyright == null)
            {
                fileCopyright = copyright.addFile(target);
            }
            fileCopyright.addLicense(license);
            return this;
        }

        private void build() throws IOException
        {
            if (ArrayHelp.contains(usage, CONFIGURATION))
            {
                addConfigurationFile(target);
            }
            if (ArrayHelp.contains(usage, DOCUMENTATION))
            {
                addDocumentationFile(target);
            }
            Path path = dir.resolve(target);
            if (content != null)
            {
                Files.createDirectories(path.getParent());
                try (FileChannel fc = FileChannel.open(path, WRITE, CREATE))
                {
                    fc.write(content);
                }
            }
            else
            {
                if (linkTarget == null)
                {
                    Files.createDirectories(path);
                }
                else
                {
                    Path link = dir.resolve(linkTarget);
                    Files.createSymbolicLink(path, link);
                }
            }
            attributeMap.forEach((a,v)->
            {
                try
                {
                    Files.setAttribute(path, a, v);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
}
