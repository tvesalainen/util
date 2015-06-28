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

/**
 *
 * @author tkv
 */
public class CmdArgsException extends Exception
{
    private final CmdArgs cmdArgs;
    
    public CmdArgsException(String message, CmdArgs cmdArgs)
    {
        super(message);
        this.cmdArgs = cmdArgs;
    }

    public CmdArgsException(Throwable cause, CmdArgs cmdArgs)
    {
        super(cause);
        this.cmdArgs = cmdArgs;
    }

    public String usage()
    {
        return cmdArgs.getUsage();
    }
    
}
