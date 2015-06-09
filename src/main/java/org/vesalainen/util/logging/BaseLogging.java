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
package org.vesalainen.util.logging;

import java.util.logging.Level;
import static java.util.logging.Level.*;

/**
 *
 * @author tkv
 */
public abstract class BaseLogging
{
    public void severe(String format, Object... args)
    {
        if (isLoggable(SEVERE))
        {
            logIt(SEVERE, String.format(format, args));
        }
    }
    public void warning(String format, Object... args)
    {
        if (isLoggable(WARNING))
        {
            logIt(WARNING, String.format(format, args));
        }
    }
    public void info(String format, Object... args)
    {
        if (isLoggable(INFO))
        {
            logIt(INFO, String.format(format, args));
        }
    }
    public void config(String format, Object... args)
    {
        if (isLoggable(CONFIG))
        {
            logIt(CONFIG, String.format(format, args));
        }
    }
    public void fine(String format, Object... args)
    {
        if (isLoggable(FINE))
        {
            logIt(FINE, String.format(format, args));
        }
    }
    public void finer(String format, Object... args)
    {
        if (isLoggable(FINER))
        {
            logIt(FINER, String.format(format, args));
        }
    }
    public void finest(String format, Object... args)
    {
        if (isLoggable(FINEST))
        {
            logIt(FINEST, String.format(format, args));
        }
    }
    public void log(Level level, String format, Object... args)
    {
        if (isLoggable(level))
        {
            logIt(level, String.format(format, args));
        }
    }
    public void log(Level level, Throwable thrown, String format, Object... args)
    {
        if (isLoggable(level))
        {
            logIt(level, String.format(format, args), thrown);
        }
    }
    protected abstract boolean isLoggable(Level level);
    protected abstract void logIt(Level level, String msg);
    protected abstract void logIt(Level level, String msg, Throwable thrown);
}
