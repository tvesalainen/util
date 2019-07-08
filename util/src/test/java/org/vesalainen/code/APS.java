/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.code;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class APS extends AnnotatedPropertyStore
{
    enum E {A1, B2, C3 }; 
    @Property(value = "enum", ordinal = 0)
    E e;
    @Property(value = "string", ordinal = 1)
    String s;
    @Property(value = "boolean", ordinal = 2)
    boolean b;
    @Property(value = "byte", ordinal = 3)
    byte by;
    @Property(value = "char", ordinal = 4)
    char cc;
    @Property(value = "short", ordinal = 5)
    short sh;
    @Property(value = "long", ordinal = 6)
    long ll;
    @Property(value = "double", ordinal = 7)
    double db;
    @Property(ordinal = 8)
    float foo;
    @Property(value = "bar", ordinal = 9)
    float ba;
    int i;

    public APS(AnnotatedPropertyStore aps)
    {
        super(aps);
    }

    public APS(Path path) throws IOException
    {
        super(path);
    }

    public APS()
    {
    }

    @Property(ordinal = 10)
    public void setGoo(int i)
    {
        this.i = i;
    }

    @Property(ordinal = 10)
    public int getGoo()
    {
        return i;
    }

}
