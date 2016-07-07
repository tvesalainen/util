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

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

/**
 *
 * @author tkv
 */
public class JavaLogging extends BaseLogging
{
    private static final Map<String,JavaLogging> map = new WeakHashMap();
    private static Clock clock = Clock.systemDefaultZone();
    
    private static Logger logger;

    public JavaLogging()
    {
        setLogger("");
    }

    public JavaLogging(Class<?> cls)
    {
        setLogger(cls);
    }

    public JavaLogging(Class<?> cls, String ext)
    {
        setLogger(cls, ext);
    }

    public JavaLogging(String name)
    {
        setLogger(name);
    }

    public JavaLogging(String name, String ext)
    {
        setLogger(name, ext);
    }

    public JavaLogging(Logger logger)
    {
        setLogger(logger);
    }

    public static JavaLogging getLogger(Class<?> cls)
    {
        return getLogger(cls.getName().replace('$', '.'));
    }
    public static JavaLogging getLogger(String name)
    {
        JavaLogging log = map.get(name);
        if (log == null)
        {
            log = new JavaLogging(name);
            map.put(name, log);
        }
        return log;
    }
    public final void setLogger(Logger logger)
    {
        this.logger = logger;
    }
    
    public final void setLogger(Class<?> cls)
    {
        setLogger(cls.getName().replace('$', '.'));
    }
    
    public final void setLogger(Class<?> cls, String ext)
    {
        setLogger(cls.getName().replace('$', '.'), ext);
    }
    
    public final void setLogger(String name)
    {
        logger = Logger.getLogger(name);
    }
    
    public final void setLogger(String name, String ext)
    {
        logger = Logger.getLogger(name+'.'+ext);
    }
    
    public Logger getLogger()
    {
        return logger;
    }
    
    @Override
    public boolean isLoggable(Level level)
    {
        return logger.isLoggable(level);
    }

    @Override
    protected void logIt(Level level, String msg)
    {
        logger.log(level, msg);
    }

    @Override
    protected void logIt(Level level, String msg, Throwable thrown)
    {
        logger.log(level, msg, thrown);
    }

    public static Clock getClock()
    {
        return clock;
    }

    public static void setClock(Clock clock)
    {
        JavaLogging.clock = clock;
    }

    @Override
    public List<String> getLoggerNames()
    {
        List<String> list = new ArrayList<>();
        Enumeration cl = LogManager.getLogManager().getLoggerNames();
        while (cl.hasMoreElements())
        {
            list.add(cl.nextElement().toString());
        }
        return list;
    }
    
    public static final void setConsoleHandler(String name, Level level)
    {
        setHandler(name, false, level, new MinimalFormatter(JavaLogging::getClock), null, new ConsoleHandler());
    }
    public static final void setConsoleHandler(String name, boolean useParentHandlers, Level level, Formatter formatter, Filter filter)
    {
        setHandler(name, useParentHandlers, level, formatter, filter, new ConsoleHandler());
    }
    public static final void setFileHandler(String name, boolean useParentHandlers, Level level, Formatter formatter, Filter filter, String pattern, int limit, int count, boolean append) throws IOException
    {
        FileHandler fileHandler = new FileHandler(pattern, limit, count, append);
        setHandler(name, useParentHandlers, level, formatter, filter, fileHandler);
    }
    public static final void setMemoryHandler(String name, boolean useParentHandlers, Level level, Formatter formatter, Filter filter, Handler handler, int size, Level pushLevel)
    {
        MemoryHandler memoryHandler = new MemoryHandler(handler, size, pushLevel);
        setHandler(name, useParentHandlers, level, formatter, filter, memoryHandler);
    }
    public static final void setHandler(String name, boolean useParentHandlers, Level level, Formatter formatter, Filter filter, Handler handler)
    {
        Logger log = Logger.getLogger(name);
        log.setUseParentHandlers(useParentHandlers);
        log.setLevel(level);
        handler.setFormatter(formatter);
        handler.setFilter(filter);
        handler.setLevel(level);
        log.addHandler(handler);
    }
}
