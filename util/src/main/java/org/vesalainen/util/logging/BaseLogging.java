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

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import static java.util.logging.Level.*;

/**
 *
 * @author tkv
 */
public abstract class BaseLogging
{
    /**
     * VERBOSE log level is finer that finest. (200)
     */
    public static final Level VERBOSE = new Verbose();
    /**
     * DEBUG log level is finer that VERBOSE (100)
     */
    public static final Level DEBUG = new Debug();
    /**
     * Write to log SEVERE level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void severe(String format, Object... args)
    {
        if (isLoggable(SEVERE))
        {
            logIt(SEVERE, String.format(format, args));
        }
    }
    /**
     * Write to log at SEVERE level.
     * @param msgSupplier 
     */
    public void severe(Supplier<String> msgSupplier)
    {
        if (isLoggable(SEVERE))
        {
            logIt(SEVERE, msgSupplier);
        }
    }
    /**
     * Write to log at WARNING level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void warning(String format, Object... args)
    {
        if (isLoggable(WARNING))
        {
            logIt(WARNING, String.format(format, args));
        }
    }
    /**
     * Write to log at WARNING level.
     * @param msgSupplier 
     */
    public void warning(Supplier<String> msgSupplier)
    {
        if (isLoggable(WARNING))
        {
            logIt(WARNING, msgSupplier);
        }
    }
    /**
     * Write to log at INFO level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void info(String format, Object... args)
    {
        if (isLoggable(INFO))
        {
            logIt(INFO, String.format(format, args));
        }
    }
    /**
     * Write to log at INFO level.
     * @param msgSupplier 
     */
    public void info(Supplier<String> msgSupplier)
    {
        if (isLoggable(INFO))
        {
            logIt(INFO, msgSupplier);
        }
    }
    /**
     * Write to log at CONFIG level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void config(String format, Object... args)
    {
        if (isLoggable(CONFIG))
        {
            logIt(CONFIG, String.format(format, args));
        }
    }
    /**
     * Write to log at CONFIG level.
     * @param msgSupplier 
     */
    public void config(Supplier<String> msgSupplier)
    {
        if (isLoggable(CONFIG))
        {
            logIt(CONFIG, msgSupplier);
        }
    }
    /**
     * Write to log at FINE level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void fine(String format, Object... args)
    {
        if (isLoggable(FINE))
        {
            logIt(FINE, String.format(format, args));
        }
    }
    /**
     * Write to log at FINE level.
     * @param msgSupplier 
     */
    public void fine(Supplier<String> msgSupplier)
    {
        if (isLoggable(FINE))
        {
            logIt(FINE, msgSupplier);
        }
    }
    /**
     * Write to log at FINER level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void finer(String format, Object... args)
    {
        if (isLoggable(FINER))
        {
            logIt(FINER, String.format(format, args));
        }
    }
    /**
     * Write to log at FINER level.
     * @param msgSupplier 
     */
    public void finer(Supplier<String> msgSupplier)
    {
        if (isLoggable(FINER))
        {
            logIt(FINER, msgSupplier);
        }
    }
    /**
     * Write to log at FINEST level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void finest(String format, Object... args)
    {
        if (isLoggable(FINEST))
        {
            logIt(FINEST, String.format(format, args));
        }
    }
    /**
     * Write to log at FINEST level.
     * @param msgSupplier 
     */
    public void finest(Supplier<String> msgSupplier)
    {
        if (isLoggable(FINEST))
        {
            logIt(FINEST, msgSupplier);
        }
    }
    /**
     * Write to log at VERBOSE level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void verbose(String format, Object... args)
    {
        if (isLoggable(VERBOSE))
        {
            logIt(VERBOSE, String.format(format, args));
        }
    }
    /**
     * Write to log at VERBOSE level.
     * @param msgSupplier 
     */
    public void verbose(Supplier<String> msgSupplier)
    {
        if (isLoggable(VERBOSE))
        {
            logIt(VERBOSE, msgSupplier);
        }
    }
    /**
     * Write to log at DEBUG level.
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void debug(String format, Object... args)
    {
        if (isLoggable(DEBUG))
        {
            logIt(DEBUG, String.format(format, args));
        }
    }
    /**
     * Write to log at DEBUG level.
     * @param msgSupplier 
     */
    public void debug(Supplier<String> msgSupplier)
    {
        if (isLoggable(DEBUG))
        {
            logIt(DEBUG, msgSupplier);
        }
    }
    /**
     * Write to log
     * @param level
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void log(Level level, String format, Object... args)
    {
        if (isLoggable(level))
        {
            if (format != null)
            {
                logIt(level, String.format(format, args));
            }
            else
            {
                logIt(level, String.format("%s format == null", level));
            }
        }
    }
    /**
     * Write to log
     * @param level
     * @param thrown
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     * @see java.util.logging.Level
     */
    public void log(Level level, Throwable thrown, String format, Object... args)
    {
        if (isLoggable(level))
        {
            if (format != null)
            {
                logIt(level, String.format(format, args), thrown);
            }
            else
            {
                logIt(level, "", thrown);
            }
        }
    }
    public abstract List<String> getLoggerNames();
    public abstract boolean isLoggable(Level level);
    protected abstract void logIt(Level level, Supplier<String> msgSupplier);
    protected abstract void logIt(Level level, String msg);
    protected abstract void logIt(Level level, String msg, Throwable thrown);

    private static class Verbose extends Level
    {

        public Verbose()
        {
            super("VERBOSE", 200);
        }
        
    }
    private static class Debug extends Level
    {

        public Debug()
        {
            super("DEBUG", 100);
        }
        
    }
}
