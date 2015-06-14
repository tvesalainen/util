/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.util;

import java.io.File;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class CmdArgsTest
{
    
    public CmdArgsTest()
    {
    }

    @Test
    public void test1()
    {
        CmdArgs cmdArgs = new CmdArgs();
        cmdArgs.addOption('s', "size", "The size of ...", 16);
        cmdArgs.addOption(File.class, 'f', "file", "The file ...");
        cmdArgs.addOption('l', "level", "Log level", Level.INFO);
        cmdArgs.setArgs("-s", "4096", "-f", "text.txt", "-l", "FINE", "rest1", "rest2");
        assertEquals(4096, cmdArgs.getOption('s'));
        assertEquals(new File("text.txt"), cmdArgs.getOption('f'));
        assertEquals(Level.FINE, cmdArgs.getOption('l'));
        String[] rest = cmdArgs.getRest();
        Assert.assertArrayEquals(new String[] {"rest1", "rest2"}, rest);
    }
    
}
