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
package org.vesalainen.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.vesalainen.bean.BeanHelper;

/**
 * JAXBCommandLine handles applications logging parameters and configuration
 * with single xml-file as argument.
 * @author tkv
 */
public class JAXBCommandLine extends LoggingCommandLine implements Runnable
{
    private String packageName;
    private final JAXBContext jaxbCtx;
    private final Object factory;
    private List<String> patterns = new ArrayList<>();
    private File configFile;

    public JAXBCommandLine(String packageName)
    {
        try
        {
            this.packageName = packageName;
            jaxbCtx = JAXBContext.newInstance(packageName);
            Class<?> cls = Class.forName(packageName+".ObjectFactory");
            factory = cls.newInstance();
        }
        catch (JAXBException | ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void command(String... args)
    {
        addArgument(File.class, "xml-config-file-path");
        super.command(args);
        configFile = getArgument("xml-config-file-path");
        readConfig();
        Thread thread = new Thread(this, configFile+" watcher");
        thread.start();
    }

    private void readConfig()
    {
        try
        {
            patterns.clear();
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            Object config = unmarshaller.unmarshal(configFile);
            BeanHelper.stream(config)
                    .forEach((String pattern)->
            {
                Object value = BeanHelper.getValue(config, pattern);
                setValue(pattern, value);
                patterns.add(pattern);
            });
        }
        catch (JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public List<String> getPatterns()
    {
        return patterns;
    }

    @Override
    public void run()
    {
        try
        {
            Path path = configFile.getParentFile().toPath();
            FileSystem fileSystem = path.getFileSystem();
            WatchService watchService = fileSystem.newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true)
            {
                WatchKey wk = watchService.take();
            }
        }
        catch (IOException | InterruptedException ex)
        {
            log.log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
}
