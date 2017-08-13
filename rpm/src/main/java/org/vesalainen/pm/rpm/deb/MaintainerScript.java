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

import java.io.BufferedWriter;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author tkv
 */
public class MaintainerScript
{
    private Path path;
    private String script;
    private String interpreter;

    public MaintainerScript(Path dir, String name, String script, String interpreter)
    {
        this.path = dir.resolve(name);
        this.script = script;
        this.interpreter = interpreter;
    }

    public void save() throws IOException
    {
        try (BufferedWriter bf = Files.newBufferedWriter(path, UTF_8))
        {
            bf.append(String.format("#!%s\n", interpreter));
            bf.append(script);
        }
    }
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MaintainerScript other = (MaintainerScript) obj;
        if (!Objects.equals(this.path, other.path))
        {
            return false;
        }
        return true;
    }
    
}
