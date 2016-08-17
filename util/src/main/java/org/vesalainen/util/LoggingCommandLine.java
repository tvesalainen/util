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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import static java.util.logging.Level.*;
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
import org.vesalainen.util.jaxb.JavaLoggingConfig.LoggerType;
import org.vesalainen.util.logging.BaseLogging;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author tkv
 */
public class LoggingCommandLine extends CmdArgs
{
    private String logPattern = "%t/java%g.log";
    private int logLimit = 1048576;
    private int logCount = 16;
    private String hostname = "localhost";
    private int port;
    private Level logLevel = INFO;
    private Level pushLevel = SEVERE;
    private String rootLog = "org.vesalainen";
    private boolean useParentHandlers;
    private int bufferSize = 256;
    protected JavaLogging log;
    
    public LoggingCommandLine()
    {
        log = new JavaLogging(LoggingCommandLine.class);
    }
    /**
     * ([ lp &lt;logging config file&gt; ] ) |
     * (([ lp &lt;log pattern&gt; -l &lt;limit&gt; -c &lt;count&gt; ] |
     * [ -h &lt;host&gt; -p &lt;port&gt; ])
     * [ -ll &lt;log level&gt; ] [ -pl &lt;push level&gt; ]
     * [ -rl &lt;root log&gt; ] [ -ph &lt;use parent handlers&gt; ]
     * [ -bs &lt;buffer size&gt; ])
     * @param args 
     */
    @Override
    public void command(String... args)
    {
        addOption(File.class, "-lx", "logging config file", "xmlConfig", false);
        addOption("-lp", "log pattern", "filelog", logPattern);
        addOption("-l", "log limit", "filelog", logLimit);
        addOption("-c", "log count", "filelog", logCount);
        addOption("-h", "host", "netlog", hostname);
        addOption("-p", "port", "netlog", port);
        addOption("-ll", "log level", null, logLevel);
        addOption("-pl", "push level", null, pushLevel);
        addOption("-rl", "root log", null, rootLog);
        addOption("-ph", "use parent handlers", null, useParentHandlers);
        addOption("-bs", "buffer size", null, bufferSize);
        
        super.command(args);
        
        File configFile = getOption("-lx");
        if (configFile != null)
        {
            configureLog(configFile);
        }
        else
        {
            configureLog();
        }
    }

    private void configureLog(File configFile)
    {
        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("org.vesalainen.util.jaxb");
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            JavaLoggingConfig javaLoggingConfig = (JavaLoggingConfig) unmarshaller.unmarshal(configFile);
            for (LoggerType loggerType : javaLoggingConfig.getLoggerType())
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
            }
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private Handler createFileHandler(FileHandlerType fileHandlerType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Handler createConsoleHandler(ConsoleHandlerType consoleHandlerType)
    {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        populateHandler(consoleHandlerType, consoleHandler);
        return consoleHandler;
    }

    private void populateHandler(HandlerType handlerType, Handler handler)
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

    private <T> T getInstance(String classname)
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
    private void configureLog()
    {
        Logger log = Logger.getLogger(getOption("-rl"));
        log.setUseParentHandlers(getOption("-ph"));
        log.setLevel(getOption("-ll"));
        Handler handler = null;
        try
        {
            String effectiveGroup = getEffectiveGroup();
            if (effectiveGroup == null)
            {
                handler = new ConsoleHandler();
            }
            else
            {
                switch (effectiveGroup)
                {
                    case "filelog":
                        handler = new FileHandler(getOption("-lp"), getOption("-l"), getOption("-c"), true);
                        break;
                    case "netlog":
                        handler = new SocketHandler(getOption("-h"), getOption("-p"));
                        break;
                    default:
                        handler = new ConsoleHandler();
                }
            }
            handler.setLevel(getOption("-ll"));
        }
        catch (IOException | SecurityException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        MinimalFormatter minimalFormatter = new MinimalFormatter();
        handler.setFormatter(minimalFormatter);
        MemoryHandler memoryHandler = new MemoryHandler(handler, getOption("-bs"), getOption("-pl"));
        memoryHandler.setFormatter(minimalFormatter);
        log.addHandler(memoryHandler);
    }
    
    public JavaLogging getLog()
    {
        return log;
    }

    public String getLogPattern()
    {
        return logPattern;
    }
    /**
     * Set default for log filename pattern
     * @param logPattern 
     * @see java.util.logging.FileHandler
     */
    public void setLogPattern(String logPattern)
    {
        this.logPattern = logPattern;
    }

    public int getLogLimit()
    {
        return logLimit;
    }
    /**
     * Set default for maximum size of log file
     * @param logLimit 
     */
    public void setLogLimit(int logLimit)
    {
        this.logLimit = logLimit;
    }

    public int getLogCount()
    {
        return logCount;
    }
    /**
     * Set default for maximum number of log files
     * @param logCount 
     */
    public void setLogCount(int logCount)
    {
        this.logCount = logCount;
    }

    public String getHostname()
    {
        return hostname;
    }
    /**
     * Set default for log server hostname
     * @param hostname 
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public int getPort()
    {
        return port;
    }
    /**
     * Set default for log server port
     * @param port 
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    public Level getLogLevel()
    {
        return logLevel;
    }
    /**
     * Set default for log level
     * @param logLevel 
     */
    public void setLogLevel(Level logLevel)
    {
        this.logLevel = logLevel;
    }

    public Level getPushLevel()
    {
        return pushLevel;
    }
    /**
     * Set default for log push level
     * @param pushLevel 
     * @see java.util.logging.MemoryHandler
     */
    public void setPushLevel(Level pushLevel)
    {
        this.pushLevel = pushLevel;
    }

    public String getRootLog()
    {
        return rootLog;
    }
    /**
     * Set default for root log name.
     * @param rootLog 
     * @see java.util.logging.Logger#getLogger(java.lang.String) 
     */
    public void setRootLog(String rootLog)
    {
        this.rootLog = rootLog;
    }

    public boolean isUseParentHandlers()
    {
        return useParentHandlers;
    }
    /**
     * Set default for useParentHandlers
     * @param useParentHandlers 
     * @see java.util.logging.Logger#useParentHandlers
     */
    public void setUseParentHandlers(boolean useParentHandlers)
    {
        this.useParentHandlers = useParentHandlers;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }
    /**
     * Set default for maximum memory buffer size.
     * @param bufferSize 
     * @see java.util.logging.MemoryHandler
     */
    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

}
