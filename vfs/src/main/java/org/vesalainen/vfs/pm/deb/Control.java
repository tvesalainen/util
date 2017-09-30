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
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.vfs.pm.Condition;
import org.vesalainen.vfs.pm.Dependency;
import static org.vesalainen.vfs.pm.deb.Field.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Control extends ControlBase
{
    private Paragraph binary;
    private Map<String, Dependency> conflicts = new HashMap<>();
    private Map<String, Dependency> provides = new HashMap<>();
    private Map<String, Dependency> dependencies = new HashMap<>();
    private String summary = "";
    private String description = "";
    private String upstreamVersion;
    private String debianRevision;

    public Control(Path debian) throws IOException
    {
        super(debian, "control");
        binary = paragraphs.get(0);
        String ver = binary.get(VERSION);
        int lidx = ver.lastIndexOf('-');
        if (lidx != -1)
        {
            upstreamVersion = ver.substring(0, lidx);
            debianRevision = ver.substring(lidx+1);
        }
        else
        {
            upstreamVersion = ver;
            debianRevision = "0";
        }
        String desc = binary.get(DESCRIPTION);
        int idx = desc.indexOf('\n');
        if (idx != -1)
        {
            summary = desc.substring(0, idx);
            description = desc.substring(idx+1);
        }
        else
        {
            summary = desc;
            description = "";
        }
        List<String> conflictList = binary.getList(CONFLICTS);
        if (conflictList != null)
        {
            for (String conflict : conflictList)
            {
                DEPDependency dep = new DEPDependency(conflict);
                conflicts.put(dep.getName(), dep);
            }
        }
        List<String> providesList = binary.getList(PROVIDES);
        if (providesList != null)
        {
            for (String provide : providesList)
            {
                DEPDependency dep = new DEPDependency(provide);
                provides.put(dep.getName(), dep);
            }
        }
        List<String> dependsList = binary.getList(DEPENDS);
        if (dependsList != null)
        {
            for (String require : dependsList)
            {
                DEPDependency dep = new DEPDependency(require);
                dependencies.put(dep.getName(), dep);
            }
        }
    }

    Control()
    {
        super("control", new Paragraph());
        binary = paragraphs.get(0);
    }

    @Override
    void save(Path debian) throws IOException
    {
        binary.set(VERSION, upstreamVersion+'-'+debianRevision);
        binary.set(DESCRIPTION, summary+'\n'+description);
        super.save(debian);
    }

    public String getUpstreamVersion()
    {
        return upstreamVersion;
    }

    public void setUpstreamVersion(String upstreamVersion)
    {
        this.upstreamVersion = upstreamVersion;
    }

    public String getDebianRevision()
    {
        return debianRevision;
    }

    public void setDebianRevision(String debianRevision)
    {
        this.debianRevision = debianRevision;
    }
    
    public Control setPackage(String v)
    {
        binary.add(PACKAGE, v);
        return this;
    }
    public String getPackage()
    {
        return binary.get(PACKAGE);
    }
    public Control setMaintainer(String v)
    {
        binary.add(MAINTAINER, v);
        return this;
    }
    public String getMaintainer()
    {
        return binary.get(MAINTAINER);
    }
    public Control setSection(String v)
    {
        binary.add(SECTION, v);
        return this;
    }
    public String getSection()
    {
        return binary.get(SECTION);
    }
    public Control setPriority(String v)
    {
        binary.add(PRIORITY, v);
        return this;
    }
    public String getPriority()
    {
        return binary.get(PRIORITY);
    }
    public Control setHomePage(String v)
    {
        binary.add(HOMEPAGE, v);
        return this;
    }
    public String getHomePage()
    {
        return binary.get(HOMEPAGE);
    }
    public Control setArchitecture(String v)
    {
        binary.add(ARCHITECTURE, "noarch".equalsIgnoreCase(v) ? "all" : v);
        return this;
    }
    public String getArchitecture()
    {
        return binary.get(ARCHITECTURE);
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setInstalledSize(int kb)
    {
        binary.set(INSTALLED_SIZE, String.valueOf(kb));
    }
    public int getInstalledSize()
    {
        String size = binary.get(INSTALLED_SIZE);
        if (size != null)
        {
            return Integer.parseInt(size);
        }
        else
        {
            return -1;
        }
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public Control addDepends(String depends)
    {
        return addDepends(depends, "");
    }
    public Control addDepends(String depends, String version, Condition... deps)
    {
        if (!depends.startsWith("/"))
        {
            DEPDependency dep = new DEPDependency(depends, version, deps);
            binary.add(DEPENDS, dep.toString());
            dependencies.put(dep.getName(), dep);
        }
        return this;
    }
    public Control addConflict(String depends)
    {
        return addConflict(depends, "");
    }
    public Control addConflict(String depends, String version, Condition... dependencies)
    {
        if (!depends.startsWith("/"))
        {
            DEPDependency dep = new DEPDependency(depends, version, dependencies);
            binary.add(CONFLICTS, dep.toString());
            conflicts.put(dep.getName(), dep);
        }
        return this;
    }
    public Control addProvides(String provide)
    {
        binary.add(PROVIDES, provide);
        DEPDependency dep = new DEPDependency(provide, null);
        provides.put(dep.getName(), dep);
        return this;
    }
    public Control addBinary(Field field, String... values)
    {
        binary.add(field, values);
        return this;
    }

    public Collection<String> getConflicts()
    {
        return conflicts.keySet();
    }

    public Dependency getConflict(String name)
    {
        return conflicts.get(name);
    }

    public Collection<String> getProvides()
    {
        return provides.keySet();
    }

    public Dependency getProvide(String name)
    {
        return provides.get(name);
    }

    public Collection<String> getRequires()
    {
        return dependencies.keySet();
    }

    public Dependency getRequire(String name)
    {
        return dependencies.get(name);
    }

}
