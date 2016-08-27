/*
 * Copyright (C) 2016 tkv
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

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A convenient interface which can be implemented in a class without actually 
 * implementing it. Default methods handle standard logging where logger name
 * is the implementing classed name.
 * <p>This is less efficient than extending from JavaLoggin or using JavaLogging
 * instance.
 * @author tkv
 */
public interface AttachedLogger
{

    /**
     * Write to log at CONFIG level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void config(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).config(format, args);
    }

    /**
     * Write to log at CONFIG level.
     * @param msgSupplier
     */
    default void config(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).config(msgSupplier);
    }

    /**
     * Write to log at DEBUG level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void debug(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).debug(format, args);
    }

    /**
     * Write to log at DEBUG level.
     * @param msgSupplier
     */
    default void debug(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).debug(msgSupplier);
    }

    /**
     * Write to log at FINE level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void fine(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).fine(format, args);
    }

    /**
     * Write to log at FINE level.
     * @param msgSupplier
     */
    default void fine(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).fine(msgSupplier);
    }

    /**
     * Write to log at FINER level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void finer(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).finer(format, args);
    }

    /**
     * Write to log at FINER level.
     * @param msgSupplier
     */
    default void finer(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).finer(msgSupplier);
    }

    /**
     * Write to log at FINEST level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void finest(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).finest(format, args);
    }

    /**
     * Write to log at FINEST level.
     * @param msgSupplier
     */
    default void finest(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).finest(msgSupplier);
    }

    /**
     * Write to log at INFO level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void info(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).info(format, args);
    }

    /**
     * Write to log at INFO level.
     * @param msgSupplier
     */
    default void info(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).info(msgSupplier);
    }

    default boolean isLoggable(Level level)
    {
        return JavaLogging.getLogger(this.getClass()).isLoggable(level);
    }

    /**
     * Write to log
     * @param level
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void log(Level level, String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).log(level, format, args);
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
    default void log(Level level, Throwable thrown, String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).log(level, thrown, format, args);
    }

    /**
     * Write to log SEVERE level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void severe(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).severe(format, args);
    }

    /**
     * Write to log at SEVERE level.
     * @param msgSupplier
     */
    default void severe(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).severe(msgSupplier);
    }

    /**
     * Write to log at VERBOSE level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void verbose(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).verbose(format, args);
    }

    /**
     * Write to log at VERBOSE level.
     * @param msgSupplier
     */
    default void verbose(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).verbose(msgSupplier);
    }

    /**
     * Write to log at WARNING level.
     * @param format
     * @param args
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * @see java.util.logging.Level
     */
    default void warning(String format, Object... args)
    {
        JavaLogging.getLogger(this.getClass()).warning(format, args);
    }

    /**
     * Write to log at WARNING level.
     * @param msgSupplier
     */
    default void warning(Supplier<String> msgSupplier)
    {
        JavaLogging.getLogger(this.getClass()).warning(msgSupplier);
    }
    
}
