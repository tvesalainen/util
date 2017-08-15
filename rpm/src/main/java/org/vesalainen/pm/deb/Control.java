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
package org.vesalainen.pm.deb;

import java.nio.file.Path;
import java.util.Arrays;
import org.vesalainen.pm.Condition;
import static org.vesalainen.pm.Condition.*;
import static org.vesalainen.pm.deb.Field.*;

/**
 *
 * @author tkv
 */
public class Control extends ControlBase
{
    private Paragraph general;
    private Paragraph binary;

    Control()
    {
        super("control", new Paragraph(), new Paragraph());
        general = paragraphs.get(0);
        binary = paragraphs.get(1);
    }
    public Control setSource(String source)
    {
        general.add(Source, source);
        return this;
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
        return addDepends(depends, "");
    }
    public Control addDepends(String depends, String version, Condition... dependencies)
    {
        if (!depends.startsWith("/"))
        {
            addRelationship(Depends, depends, version, dependencies);
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
            addRelationship(Conflicts, depends, version, dependencies);
        }
        return this;
    }
    public Control addProvides(String provides)
    {
        binary.add(Provides, provides);
        return this;
    }
    private void addRelationship(Field field, String depends, String version, Condition... dependencies)
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

    String toString(String depends, String version, Condition... deps)
    {
        if (version == null || version.isEmpty())
        {
            return depends;
        }
        if (deps.length < 1 || deps.length > 2)
        {
            throw new IllegalArgumentException(Arrays.toString(deps)+" illegal combination");
        }
        if (deps.length == 1)
        {
            switch (deps[0])
            {
                case LESS:
                    return depends+" (<< "+version+")";
                case GREATER:
                    return depends+" (>> "+version+")";
                case EQUAL:
                    return depends+" (= "+version+")";
                default:
                    throw new UnsupportedOperationException(deps[0]+" not supported");
            }
        }
        else
        {
            Arrays.sort(deps);
            switch (deps[1])
            {
                case LESS:
                    return depends+" (<= "+version+")";
                case GREATER:
                    return depends+" (>= "+version+")";
                default:
                    throw new UnsupportedOperationException(Arrays.toString(deps)+" dependency not supported");
            }

        }
    }

}
