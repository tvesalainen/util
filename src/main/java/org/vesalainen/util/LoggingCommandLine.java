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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SocketHandler;
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

    @Override
    public void command(String... args)
    {
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
                        handler = new SocketHandler(getOption("-h"), getOption("p"));
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
