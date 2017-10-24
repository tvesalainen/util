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

import java.util.Arrays;
import org.vesalainen.vfs.pm.Condition;
import static org.vesalainen.vfs.pm.Condition.*;
import org.vesalainen.vfs.pm.Dependency;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEBDependency implements Dependency
{
    private String name;
    private String version;
    private Condition[] conditions;

    public DEBDependency(String name, String version, Condition... condition)
    {
        this.name = name;
        this.version = version;
        this.conditions = condition;
    }

    public DEBDependency(String str)
    {
        if (str.indexOf('|') != -1)
        {
            throw new UnsupportedOperationException("alternative packages not supported yet.");
        }
        if (str.indexOf('(') == -1)
        {
            this.name = str.trim();
        }
        else
        {
            String[] split = str.split("[ \\(\\)]+");
            if (split.length != 3 || split[1].length() > 2)
            {
                throw new IllegalArgumentException(str);
            }
            this.name = split[0];
            this.version = split[2];
            switch (split[1])
            {
                case "<<":
                    this.conditions = new Condition[]{LESS};
                    break;
                case ">>":
                    this.conditions = new Condition[]{GREATER};
                    break;
                case "=":
                    this.conditions = new Condition[]{EQUAL};
                    break;
                case ">=":
                    this.conditions = new Condition[]{GREATER, EQUAL};
                    break;
                case "<=":
                    this.conditions = new Condition[]{LESS, EQUAL};
                    break;
            }
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public Condition[] getConditions()
    {
        return conditions;
    }
    
    @Override
    public String toString()
    {
        if (version == null || version.isEmpty())
        {
            return name;
        }
        if (conditions.length < 1 || conditions.length > 2)
        {
            throw new IllegalArgumentException(Arrays.toString(conditions)+" illegal combination");
        }
        if (conditions.length == 1)
        {
            switch (conditions[0])
            {
                case LESS:
                    return name+" (<< "+version+")";
                case GREATER:
                    return name+" (>> "+version+")";
                case EQUAL:
                    return name+" (= "+version+")";
                default:
                    throw new UnsupportedOperationException(conditions[0]+" not supported");
            }
        }
        else
        {
            Arrays.sort(conditions);
            switch (conditions[1])
            {
                case LESS:
                    return name+" (<= "+version+")";
                case GREATER:
                    return name+" (>= "+version+")";
                default:
                    throw new UnsupportedOperationException(Arrays.toString(conditions)+" dependency not supported");
            }

        }
    }

}
