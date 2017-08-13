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
package org.vesalainen.pm.rpm.deb;

import java.nio.file.Path;
import static org.vesalainen.pm.rpm.Dependency.*;
import static org.vesalainen.pm.rpm.deb.Field.*;

/**
 *
 * @author tkv
 */
public class Control extends ControlBase
{
    private Paragraph general;
    private Paragraph binary;

    Control(Path dir, String source)
    {
        super(dir, "control", new Paragraph(), new Paragraph());
        general = paragraphs.get(0);
        binary = paragraphs.get(1);
        general.add(Source, source);
    }
    public Control setMaintainer(String v)
    {
        general.add(Maintainer, v);
        return this;
    }
    public Control setSection(String v)
    {
        general.add(Section, v);
        return this;
    }
    public Control setPriority(String v)
    {
        general.add(Priority, v);
        return this;
    }
    Control setStandardsVersion(String v)
    {
        general.add(Standards_Version, v);
        return this;
    }
    public Control setHomePage(String v)
    {
        general.add(Standards_Version, v);
        return this;
    }
    public Control setPackage(String v)
    {
        binary.add(Package, v);
        return this;
    }
    public Control setArchitecture(String v)
    {
        binary.add(Architecture, "noarch".equalsIgnoreCase(v) ? "all" : v);
        return this;
    }
    public Control setDescription(String v)
    {
        binary.add(Description, v);
        return this;
    }
    public Control addDepends(String depends)
    {
        return addDepends(depends, "", 0);
    }
    public Control addDepends(String depends, String version, int dependencies)
    {
        if (compatible(dependencies) && !depends.startsWith("/"))
        {
            addRelationship(Depends, depends, version, dependencies);
        }
        return this;
    }
    public Control addConflict(String depends)
    {
        return addConflict(depends, "", 0);
    }
    public Control addConflict(String depends, String version, int dependencies)
    {
        if (compatible(dependencies) && !depends.startsWith("/"))
        {
            addRelationship(Conflicts, depends, version, dependencies);
        }
        return this;
    }
    private void addRelationship(Field field, String depends, String version, int dependencies)
    {
        binary.add(field, toString(depends, version, dependencies));
    }
    public Control addGeneral(Field field, String... values)
    {
        general.add(field, values);
        return this;
    }
    public Control addBinary(Field field, String... values)
    {
        binary.add(field, values);
        return this;
    }

    String toString(String depends, String version, int dependencies)
    {
        if (version == null || version.isEmpty())
        {
            return depends;
        }
        switch (dependencies)
        {
            case LESS:
                return depends+" (<< "+version+")";
            case GREATER:
                return depends+" (>> "+version+")";
            case EQUAL:
                return depends+" (= "+version+")";
            case LESS | EQUAL:
                return depends+" (<= "+version+")";
            case GREATER | EQUAL:
                return depends+" (>= "+version+")";
            default:
                throw new UnsupportedOperationException(dependencies+" dependency not supported");
                
        }
    }

    private boolean compatible(int dependencies)
    {
        return dependencies == 0 || (dependencies >= 0x02 && dependencies <= (0x08 | 0x04 | 0x02));
    }

}