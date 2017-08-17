/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Helper for calling OS processes.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OSProcess
{
    /**
     * Call os process and waits it's execution.
     * @param args Either command and arguments in single string or command and
     * arguments as separate strings. 
     * <p>
     * call("netstat -an") is same as call("netstat", "-an")
     * @return
     * @throws IOException
     * @throws InterruptedException 
     * @see java.lang.Runtime#exec(java.lang.String[]) 
     */
    public static final int call(String... args) throws IOException, InterruptedException
    {
        return call(null, null, args);
    }
    /**
     * Call os process and waits it's execution.
     * @param args Either command and arguments in single string or command and
     * arguments as separate strings. 
     * <p>
     * call("netstat -an") is same as call("netstat", "-an")
     * @param cwd Current Working Directory
     * @param env Environment
     * @param args
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public static final int call(Path cwd, Map<String,String> env, String... args) throws IOException, InterruptedException
    {
        if (args.length == 1)
        {
            args = args[0].split("[ ]+");
        }
        String cmd = Arrays.stream(args).collect(Collectors.joining(" "));
        JavaLogging.getLogger(OSProcess.class).info("call: %s", cmd);
        ProcessBuilder builder = new ProcessBuilder(args);
        if (cwd != null)
        {
            builder.directory(cwd.toFile());
        }
        if (env != null)
        {
            Map<String, String> environment = builder.environment();
            environment.clear();
            environment.putAll(env);
        }
        Process process = builder.start();
        Thread stdout = new Thread(new ProcessLogger(cmd, process.getInputStream(), Level.INFO));
        stdout.start();
        Thread stderr = new Thread(new ProcessLogger(cmd, process.getErrorStream(), Level.WARNING));
        stderr.start();
        return process.waitFor();
    }
    private static class ProcessLogger extends JavaLogging implements Runnable
    {
        private InputStream is;
        private Level level;
        private final String cmd;

        public ProcessLogger(String cmd, InputStream is, Level level)
        {
            super(ProcessLogger.class);
            this.cmd = cmd;
            this.is = is;
            this.level = level;
        }
        
        @Override
        public void run()
        {
            try (InputStream s = is)
            {
                FileUtil.lines(s).forEach((l)->log(level, "%s: %s", cmd, l));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
}
