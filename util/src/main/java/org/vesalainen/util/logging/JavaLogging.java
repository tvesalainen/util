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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SocketHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.vesalainen.util.jaxb.ConsoleHandlerType;
import org.vesalainen.util.jaxb.FileHandlerType;
import org.vesalainen.util.jaxb.HandlerType;
import org.vesalainen.util.jaxb.JavaLoggingConfig;
import org.vesalainen.util.jaxb.LoggerType;
import org.vesalainen.util.jaxb.MemoryHandlerType;
import org.vesalainen.util.jaxb.SocketHandlerType;

/**
 *
 * @author tkv
 */
public class JavaLogging extends BaseLogging
{
    private static final Map<String,JavaLogging> map = new WeakHashMap();
    private static Supplier<Clock> clockSupplier = () -> {return Clock.systemDefaultZone();};
    
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
    protected void logIt(Level level, Supplier<String> msgSupplier)
    {
        logger.log(level, msgSupplier);
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

    public static void setClockSupplier(Supplier<Clock> clockSupplier)
    {
        JavaLogging.clockSupplier = clockSupplier;
    }

    public static Clock getClock()
    {
        return clockSupplier.get();
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
    /**
     * Configures logging from xml file.
     * @param configFile
     * @throws IOException 
     */
    public static final void xmlConfig(File configFile) throws IOException
    {
        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("org.vesalainen.util.jaxb");
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            JavaLoggingConfig javaLoggingConfig = (JavaLoggingConfig) unmarshaller.unmarshal(configFile);
            for (LoggerType loggerType : javaLoggingConfig.getLogger())
            {
                String name = loggerType.getName();
                Logger logger = Logger.getLogger(name);
                String level = loggerType.getLevel();
                if (level != null)
                {
                    logger.setLevel(BaseLogging.parseLevel(level));
                }
                logger.setUseParentHandlers(loggerType.isUseParentHandlers());
                String resourceBundleString = loggerType.getResourceBundle();
                if (resourceBundleString != null)
                {
                    Locale locale;
                    String languageTag = loggerType.getLocale();
                    if (languageTag != null)
                    {
                        locale = Locale.forLanguageTag(languageTag);
                    }
                    else
                    {
                        locale = Locale.getDefault();
                    }
                    ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleString, locale);
                    logger.setResourceBundle(resourceBundle);
                }
                logger.setFilter(getInstance(loggerType.getFilter()));
                for (ConsoleHandlerType consoleHandlerType : loggerType.getConsoleHandler())
                {
                    logger.addHandler(createConsoleHandler(consoleHandlerType));
                }
                for (FileHandlerType fileHandlerType : loggerType.getFileHandler())
                {
                    logger.addHandler(createFileHandler(fileHandlerType));
                }
                for (MemoryHandlerType memoryHandlerType : loggerType.getMemoryHandler())
                {
                    logger.addHandler(createMemoryHandler(memoryHandlerType));
                }
                for (SocketHandlerType socketHandlerType : loggerType.getSocketHandler())
                {
                    logger.addHandler(createSocketHandler(socketHandlerType));
                }
            }
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private static Handler createMemoryHandler(MemoryHandlerType memoryHandlerType) throws IOException
    {
        Handler handler = null;
        MemoryHandlerType.Target target = memoryHandlerType.getTarget();
        if (target != null)
        {
            ConsoleHandlerType consoleHandlerType = target.getConsoleHandler();
            if (consoleHandlerType != null)
            {
                handler = createConsoleHandler(consoleHandlerType);
            }
            else
            {
                FileHandlerType fileHandlerType = target.getFileHandler();
                if (fileHandlerType != null)
                {
                    handler = createFileHandler(fileHandlerType);
                }
                else
                {
                    MemoryHandlerType memHandlerType = target.getMemoryHandler();
                    if (memHandlerType != null)
                    {
                        handler = createMemoryHandler(memoryHandlerType);
                    }
                    else
                    {
                        SocketHandlerType socketHandlerType = target.getSocketHandler();
                        if (socketHandlerType != null)
                        {
                            handler = createSocketHandler(socketHandlerType);
                        }
                    }
                }
            }
        }
        MemoryHandler memoryHandler = new MemoryHandler(handler, (int)memoryHandlerType.getSize(), BaseLogging.parseLevel(memoryHandlerType.getPushLevel()));
        populateHandler(memoryHandlerType, memoryHandler);
        return memoryHandler;
    }
    private static Handler createSocketHandler(SocketHandlerType socketHandlerType) throws IOException
    {
        SocketHandler socketHandler = new SocketHandler(socketHandlerType.getHost(), socketHandlerType.getPort());
        populateHandler(socketHandlerType, socketHandler);
        return socketHandler;
    }
    private static Handler createFileHandler(FileHandlerType fileHandlerType) throws IOException
    {
        FileHandler fileHandler = new FileHandler(fileHandlerType.getPattern(), (int)fileHandlerType.getLimit(), (int)fileHandlerType.getCount(), fileHandlerType.isAppend());
        populateHandler(fileHandlerType, fileHandler);
        return fileHandler;
    }

    private static Handler createConsoleHandler(ConsoleHandlerType consoleHandlerType)
    {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        populateHandler(consoleHandlerType, consoleHandler);
        return consoleHandler;
    }

    private static void populateHandler(HandlerType handlerType, Handler handler)
    {
        try
        {
            handler.setEncoding(handlerType.getEncoding());
            handler.setErrorManager(getInstance(handlerType.getErrorManager()));
            handler.setFilter(getInstance(handlerType.getFilter()));
            handler.setFormatter(getInstance(handlerType.getFormatter()));
            String level = handlerType.getLevel();
            if (level != null)
            {
                handler.setLevel(BaseLogging.parseLevel(level));
            }
        }
        catch (SecurityException | UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static <T> T getInstance(String classname)
    {
        if (classname == null)
        {
            return null;
        }
        try
        {
            Class<T> cls = (Class<T>) Class.forName(classname);
            return cls.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
