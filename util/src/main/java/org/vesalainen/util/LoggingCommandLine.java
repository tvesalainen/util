/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SocketHandler;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LoggingCommandLine extends CmdArgs
{
    private String logPattern = "%t/java%g.log";
    private int logLimit = 1048576;
    private int logCount = 16;
    private String hostname = "localhost";
    private int radioPort;
    private Level logLevel = INFO;
    private Level pushLevel = SEVERE;
    private String rootLog = "org.vesalainen";
    private boolean useParentHandlers;
    private int bufferSize = 256;
    
    /**
     * ([ lp &lt;logging config file&gt; ] ) |
     * (([ lp &lt;log pattern&gt; -l &lt;limit&gt; -c &lt;count&gt; ] |
     * [ -h &lt;host&gt; -p &lt;radioPort&gt; ])
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
        addOption("-p", "port", "netlog", radioPort);
        addOption("-ll", "log level", null, logLevel);
        addOption("-pl", "push level", null, pushLevel);
        addOption("-rl", "root log", null, rootLog);
        addOption("-ph", "use parent handlers", null, useParentHandlers);
        addOption("-bs", "buffer size", null, bufferSize);
        
        super.command(args);
        
        File configFile = getOption("-lx");
        if (configFile != null)
        {
            try
            {
                JavaLogging.xmlConfig(configFile);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            configureLog();
        }
        Thread.setDefaultUncaughtExceptionHandler(LoggingCommandLine::exceptionHandler);
    }

    private static void exceptionHandler(Thread thread, Throwable thr)
    {
        JavaLogging.getLogger(LoggingCommandLine.class).log(SEVERE, thr, "%s: %s", thread.getName(), thr.getMessage());
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
    
    public final String getLogPattern()
    {
        return logPattern;
    }
    /**
     * Set default for log filename pattern
     * @param logPattern 
     * @see java.util.logging.FileHandler
     */
    public final void setLogPattern(String logPattern)
    {
        this.logPattern = logPattern;
    }

    public final int getLogLimit()
    {
        return logLimit;
    }
    /**
     * Set default for maximum size of log file
     * @param logLimit 
     */
    public final void setLogLimit(int logLimit)
    {
        this.logLimit = logLimit;
    }

    public final int getLogCount()
    {
        return logCount;
    }
    /**
     * Set default for maximum number of log files
     * @param logCount 
     */
    public final void setLogCount(int logCount)
    {
        this.logCount = logCount;
    }

    public final String getHostname()
    {
        return hostname;
    }
    /**
     * Set default for log server hostname
     * @param hostname 
     */
    public final void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public final int getPort()
    {
        return radioPort;
    }
    /**
     * Set default for log server radioPort
     * @param port 
     */
    public final void setPort(int port)
    {
        this.radioPort = port;
    }

    public final Level getLogLevel()
    {
        return logLevel;
    }
    /**
     * Set default for log level
     * @param logLevel 
     */
    public final void setLogLevel(Level logLevel)
    {
        this.logLevel = logLevel;
    }

    public final Level getPushLevel()
    {
        return pushLevel;
    }
    /**
     * Set default for log push level
     * @param pushLevel 
     * @see java.util.logging.MemoryHandler
     */
    public final void setPushLevel(Level pushLevel)
    {
        this.pushLevel = pushLevel;
    }

    public final String getRootLog()
    {
        return rootLog;
    }
    /**
     * Set default for root log name.
     * @param rootLog 
     * @see java.util.logging.Logger#getLogger(java.lang.String) 
     */
    public final void setRootLog(String rootLog)
    {
        this.rootLog = rootLog;
    }

    public final boolean isUseParentHandlers()
    {
        return useParentHandlers;
    }
    /**
     * Set default for useParentHandlers
     * @param useParentHandlers 
     * @see java.util.logging.Logger#useParentHandlers
     */
    public final void setUseParentHandlers(boolean useParentHandlers)
    {
        this.useParentHandlers = useParentHandlers;
    }

    public final int getBufferSize()
    {
        return bufferSize;
    }
    /**
     * Set default for maximum memory buffer size.
     * @param bufferSize 
     * @see java.util.logging.MemoryHandler
     */
    public final void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

}
