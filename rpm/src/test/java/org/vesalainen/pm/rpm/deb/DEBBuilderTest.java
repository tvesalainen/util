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

import org.vesalainen.pm.deb.DEBBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.vesalainen.pm.rpm.FileFlag.DOC;

/**
 *
 * @author tkv
 */
public class DEBBuilderTest
{
    static final Path DIR = new File("z:\\").toPath();
    
    public DEBBuilderTest()
    {
    }

    @Test
    public void testBuild() throws IOException
    {
        DEBBuilder builder = new DEBBuilder(DIR, "deb-test", "0.1", "r0", "Timo <timo@mail.net>");
        builder.control()
                .setMaintainer("timo")
                .setSection("java")
                .setPriority("optional")
                .setPackage("test")
                .setArchitecture("all")
                .addDepends("lsb")
                .setDescription("blaa blaa");
        builder.copyright()
                .setCopyright("2017 Timo Vesalainen")
                .setLicense("GPL-3");
        builder.addFile("/opt/app/rules", Paths.get("src", "main", "resources", "rules"))
                .setFlags(DOC)
                ;
        builder.build();
    }
    
}
