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
package org.vesalainen.can;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.Test;
import org.vesalainen.jmx.SimpleJMX;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CanServiceT
{
    
    public CanServiceT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINER);
        SimpleJMX.start();
    }

    @Test
    public void test() throws IOException, InterruptedException, ExecutionException
    {
        //AbstractCanService canSvc = AbstractCanService.openSocketCan2Udp("224.0.0.3", 10111, new TestCompiler());
        AbstractCanService canSvc = AbstractCanService.openSocketCand("can1", new PrintCompiler());
        canSvc.addDBCFile(Paths.get("src", "test", "resources", "Orion_CANBUS.dbc"));
        canSvc.addDBCFile(Paths.get("src", "main", "resources", "n2k.dbc"));
        //canSvc.compilePgn(33162494);
        canSvc.startAndWait();
    }
    
    private static class TCompiler implements SignalCompiler
    {
        
    }
}
