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
package org.vesalainen.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.vesalainen.bean.BeanHelper;

/**
 * JAXBCommandLine handles applications logging parameters and configuration
 * with single xml-file as argument.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JAXBCommandLine extends LoggingCommandLine implements Runnable
{
    private Map<String,Object> configMap = new HashMap<>();
    private String packageName;
    private final JAXBContext jaxbCtx;
    private final Object factory;
    private File configFile;
    private Path relative;
    private boolean watch;
    /**
     * Creates JAXBCommandLine.
     * @param packageName JAXB package name
     * @param watch Do we watch config file and configure automatically after modification.
     */
    public JAXBCommandLine(String packageName, boolean watch)
    {
        try
        {
            this.packageName = packageName;
            this.watch = watch;
            jaxbCtx = JAXBContext.newInstance(packageName);
            Class<?> cls = Class.forName(packageName+".ObjectFactory");
            factory = cls.newInstance();
        }
        catch (JAXBException | ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Resolves command line arguments and starts watch thread if watch was true
     * in constructor.
     * @param args 
     */
    @Override
    public void command(String... args)
    {
        addArgument(File.class, "xml-config-file-path");
        super.command(args);
        configFile = getArgument("xml-config-file-path");
        readConfig();
        if (watch)
        {
            Thread thread = new Thread(this, configFile+" watcher");
            thread.start();
        }
    }

    private void readConfig()
    {
        try
        {
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            Object config = unmarshaller.unmarshal(configFile);
            BeanHelper.stream(config)
                    .forEach((String pattern)->
            {
                Object value = BeanHelper.getValue(config, pattern);
                configMap.put(pattern, value);
                config("Config setValue(%s, %s)", pattern, value);
            });
        }
        catch (JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Overrides getValue so that also config file values are returned.
     * @param name
     * @return 
     */
    @Override
    public Object getValue(String name)
    {
        Object value = super.getValue(name);
        if (value != null)
        {
            return value;
        }
        else
        {
            return configMap.get(name);
        }
    }

    @Override
    public void run()
    {
        try
        {
            Path path = configFile.getParentFile().toPath();
            relative = path.relativize(configFile.toPath());
            FileSystem fileSystem = path.getFileSystem();
            WatchService watchService = fileSystem.newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true)
            {
                WatchKey wk = watchService.take();
                for (WatchEvent e : wk.pollEvents())
                {
                    Kind kind = e.kind();
                    if (kind.equals(ENTRY_MODIFY))
                    {
                        if (relative.equals(e.context()))
                        {
                            try
                            {
                                fine("modified %s", e);
                                readConfig();
                            }
                            catch (Exception ex)
                            {
                                log(Level.SEVERE, ex, "%s", ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        catch (IOException | InterruptedException ex)
        {
            log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
}
