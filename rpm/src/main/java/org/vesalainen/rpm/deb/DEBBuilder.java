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
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.vesalainen.nio.FileUtil;

/**
 * DEBBuilder class supports building structure for building debian packet.
 * Use dpkg-buildpackage -us -uc for building of actual packet.
 * @author tkv
 * @see <a href="https://www.debian.org/doc/manuals/maint-guide/index.en.html">Debian New Maintainers' Guide</a>
 * @see <a href="https://www.debian.org/doc/debian-policy/index.html">Debian Policy Manual</a>
 */
public class DEBBuilder
{
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

    public DEBBuilder(Path dir, String name, String version, String release, String maintainer)
    {
        this.dir = dir;
        this.name = name;
        this.version = version;
        this.release = release;
        this.maintainer = maintainer;
        this.debian = dir.resolve("debian");
        control = new Control(debian, name);
        copyright = new Copyright(debian);
        changeLog = new ChangeLog(debian, name, version, release, maintainer);
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
        
        Path compat = debian.resolve("compat");
        try (BufferedWriter bf = Files.newBufferedWriter(compat, UTF_8))
        {
            bf.append(String.format("%d\n", compatibility));
        }
        for (MaintainerScript ms : maintainerScripts)
        {
            ms.save();
        }
        
        FileUtil.copyResource("/rules", debian.resolve("rules"), DEBBuilder.class);
    }
}
