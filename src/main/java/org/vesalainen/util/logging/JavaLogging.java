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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author tkv
 */
public class JavaLogging extends BaseLogging
{
    private static final Map<String,JavaLogging> map = new WeakHashMap();
    
    private Logger logger;

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
    
}
