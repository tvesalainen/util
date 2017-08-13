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
import java.util.ArrayList;
import java.util.List;
import static org.vesalainen.pm.rpm.deb.DEBBuilder.SOURCE_FORMAT;

/**
 *
 * @author tkv
 */
public class Conffiles extends FilesBase
{

    public Conffiles(Path debian)
    {
        super(debian, "conffiles");
    }
    
    @Override
    public void addFile(String filepath)
    {
        if (!filepath.startsWith("/etc"))
        {
            super.addFile(filepath);
        }
    }
    
}
