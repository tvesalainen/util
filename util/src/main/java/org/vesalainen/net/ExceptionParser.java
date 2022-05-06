/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;

/**
 * Helper class to handle Exceptions
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ExceptionParser
{
    /**
     * Tries to detect if Throwable is caused by broken connection. If detected
     * returns level, else return SEVERE.
     * @param level
     * @param thr
     * @return 
     */
    public static final Level brokenConnection(Level level, Throwable thr)
    {
        if (thr instanceof EOFException)
        {
            return level;
        }
        if (thr instanceof ClosedChannelException)
        {
            return level;
        }
        if ((thr instanceof IOException) && (
                "Connection timed out".equals(thr.getMessage()) ||
                "Connection refused".equals(thr.getMessage()) ||
                "No route to host".equals(thr.getMessage()) ||
                "Broken pipe".equals(thr.getMessage()) ||
                "Connection reset by peer".equals(thr.getMessage())
                )
            )
        {
            return level;
        }
        Throwable cause = thr.getCause();
        if (cause != null)
        {
            return brokenConnection(level, cause);
        }
        return Level.SEVERE;
    }
}
