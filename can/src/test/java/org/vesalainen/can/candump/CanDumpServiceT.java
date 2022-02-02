/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.candump;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.can.PrintCompiler;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CanDumpServiceT
{
    
    public CanDumpServiceT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.INFO);
    }

    //@Test
    public void test1() throws IOException, InterruptedException, ExecutionException
    {
        Path path = Paths.get("C:\\Users\\tkv\\share", "candump.txt");
        CanDumpService svc = new CanDumpService("can1", path, new CachedScheduledThreadPool(), new PrintCompiler());
        DBC.addN2K();
        svc.startAndWait();
    }
    @Test
    public void test2() throws IOException, InterruptedException, ExecutionException
    {
        Path path = Paths.get("C:\\Users\\tkv\\Documents\\NetBeansProjects\\canboat\\samples", "candumpSample2.txt");
        CanDumpService svc = new CanDumpService("can0", path, new CachedScheduledThreadPool(), new PrintCompiler());
        DBC.addN2K();
        svc.startAndWait();
    }
    private class Compiler implements SignalCompiler
    {
        
    }
}
