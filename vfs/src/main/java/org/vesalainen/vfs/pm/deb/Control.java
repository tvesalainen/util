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
import java.util.Arrays;
import org.vesalainen.vfs.pm.Condition;
import static org.vesalainen.vfs.pm.deb.Field.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Control extends ControlBase
{
    private Paragraph binary;

    public Control(Path debian) throws IOException
    {
        super(debian, "control");
    }

    Control()
    {
        super("control", new Paragraph());
        binary = paragraphs.get(0);
    }
    public Control setPackage(String v)
    {
        binary.add(PACKAGE, v);
        return this;
    }
    public Control setVersion(String v)
    {
        binary.add(VERSION, v);
        return this;
    }
    public Control setMaintainer(String v)
    {
        binary.add(MAINTAINER, v);
        return this;
    }
    public Control setSection(String v)
    {
        binary.add(SECTION, v);
        return this;
    }
    public Control setPriority(String v)
    {
        binary.add(PRIORITY, v);
        return this;
    }
    public Control setHomePage(String v)
    {
        binary.add(HOMEPAGE, v);
        return this;
    }
    public Control setArchitecture(String v)
    {
        binary.add(ARCHITECTURE, "noarch".equalsIgnoreCase(v) ? "all" : v);
        return this;
    }
    public Control setDescription(String v)
    {
        binary.add(DESCRIPTION, v);
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
            addRelationship(DEPENDS, depends, version, dependencies);
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
            addRelationship(CONFLICTS, depends, version, dependencies);
        }
        return this;
    }
    public Control addProvides(String provides)
    {
        binary.add(PROVIDES, provides);
        return this;
    }
    private void addRelationship(Field field, String depends, String version, Condition... dependencies)
    {
        binary.add(field, toString(depends, version, dependencies));
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
