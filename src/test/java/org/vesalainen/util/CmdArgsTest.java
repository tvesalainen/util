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
import java.util.logging.Logger;
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
        try
        {
            CmdArgs cmdArgs = new CmdArgs();
            cmdArgs.addArgument("arg1");
            cmdArgs.addArgument(Long.class, "arg2");
            cmdArgs.addOption("-s", "size", null, 16);
            cmdArgs.addOption(File.class, "-f", "file");
            cmdArgs.addOption("-l", "level", null, Level.INFO);
            cmdArgs.setArgs("-s", "4096", "-f", "text.txt", "-l", "FINE", "rest1", "1234");
            assertEquals(4096, cmdArgs.getOption("-s"));
            assertEquals(new File("text.txt"), cmdArgs.getOption("-f"));
            assertEquals(Level.FINE, cmdArgs.getOption("-l"));
            assertEquals("rest1", cmdArgs.getArgument("arg1"));
            assertEquals(1234L, cmdArgs.getArgument("arg2"));
            assertEquals("usage:  -s <size> -f <file> -l <level> <arg1> <arg2>", cmdArgs.getUsage());
        }
        catch (CmdArgsException ex)
        {
            Logger.getLogger(CmdArgsTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void test2()
    {
        try
        {
            CmdArgs cmdArgs = new CmdArgs();
            cmdArgs.addArgument("arg");
            cmdArgs.addOption("-h", "host", "net", "localhost");
            cmdArgs.addOption("-p", "port", "net", 23);
            cmdArgs.addOption("-f", "file", "local", "log.txt");
            cmdArgs.setArgs("-h", "www.host.com", "-p", "1234", "hello");
            assertEquals("www.host.com", cmdArgs.getOption("-h"));
            assertEquals(1234, cmdArgs.getOption("-p"));
            assertEquals("usage: [-h <host> -p <port>]|[-f <file>] <arg>", cmdArgs.getUsage());
        }
        catch (CmdArgsException ex)
        {
            Logger.getLogger(CmdArgsTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
}
