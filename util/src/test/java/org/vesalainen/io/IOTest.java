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
package org.vesalainen.io;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.PointList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IOTest
{
    
    public IOTest()
    {
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException
    {
        Path path = Paths.get("serialize.ser");
        PointList p1 = new PointList();
        p1.add(1, 2);
        IO.serialize(p1, path);
        PointList p2 = IO.deserialize(path);
        assertEquals(p1, p2);
    }
    
}
