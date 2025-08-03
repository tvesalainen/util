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
package org.vesalainen.util;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum OperatingSystem
{
    Windows,
    Linux
    ;
    
    private static final Map<String,Path> map = new HashMap<>();
    private static OperatingSystem os;
    /**
     * Returns true if current operation system is given
     * @param os
     * @return 
     */
    public static boolean is(OperatingSystem os)
    {
        return getOperatingSystem().equals(os);
    }
    /**
     * Return current operation system.
     * @return 
     */
    public static OperatingSystem getOperatingSystem()
    {
        if (os == null)
        {
            String osName = System.getProperty("os.name");
            if (osName.contains("inux"))
            {
                os = Linux;
            }
            else
            {
                if (osName.contains("indows"))
                {
                    os = Windows;
                }
                else
                {
                    throw new UnsupportedOperationException(osName+" not supported");
                }
            }
        }
        return os;
    }
}
