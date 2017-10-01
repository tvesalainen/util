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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.stream.Collectors;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MaintainerScript
{
    private Path path;
    private String interpreter;
    private String script;

    public MaintainerScript(Path controlRoot, String name) throws IOException
    {
        path = controlRoot.resolve(name);
        if (Files.exists(path))
        {
            try (BufferedReader r = Files.newBufferedReader(path, US_ASCII))
            {
                String first = r.readLine();
                if (!first.startsWith("#!"))
                {
                    throw new IllegalArgumentException(first+" missing interpreter");
                }
                script = first.substring(2).trim();
                script = r.lines().collect(Collectors.joining("\n", "", "\n"));
            }
        }
    }

    public void save() throws IOException
    {
        if (interpreter != null && script != null)
        {
            try (BufferedWriter w = Files.newBufferedWriter(path, US_ASCII))
            {
                w.append("#!").append(interpreter).append('\n');
                w.append(script);
            }
        }
    }

    public String getInterpreter()
    {
        return interpreter;
    }

    public String getScript()
    {
        return script;
    }

    public void set(String interpreter, String script)
    {
        this.interpreter = interpreter;
        this.script = script;
    }
    
}
