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
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.junit.Test;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
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
        AbstractCanService canSvc = AbstractCanService.openSocketCand("can1", new TCompiler());
        canSvc.addDBCFile(Paths.get("src", "test", "resources", "Orion_CANBUS.dbc"));
        canSvc.addDBCFile(Paths.get("src", "main", "resources", "n2k.dbc"));
        //canSvc.compilePgn(33162494);
        canSvc.startAndWait();
    }
    
    private static class TCompiler implements SignalCompiler
    {
        
    }
    private static class TestCompiler implements SignalCompiler
    {

        @Override
        public Runnable compileBegin(MessageClass mc, LongSupplier millisSupplier)
        {
            return ()->System.err.print(mc.getName()+":");
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier)
        {
            return ()->System.err.print(" "+sc.getName()+" = "+supplier.getAsInt()+" "+sc.getUnit());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier)
        {
            return ()->System.err.print(" "+sc.getName()+" = "+supplier.getAsLong()+" "+sc.getUnit());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier)
        {
            return ()->System.err.print(" "+sc.getName()+" = "+supplier.getAsDouble()+" "+sc.getUnit());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map)
        {
            return ()->
                {
                    int ii = supplier.getAsInt();
                    String ss = map.apply(ii);
                    ss = ss == null ? ii+"???" : ss;
                    System.err.print(" "+sc.getName()+" = "+ss );
                };
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> supplier)
        {
            return ()->
                {
                    System.err.print(" "+sc.getName()+" = '"+supplier.get()+"'");
                };
        }

        @Override
        public Consumer<Throwable> compileEnd(MessageClass mc)
        {
            return (ex)->System.err.println();
        }

        @Override
        public Runnable compileBeginRepeat(MessageClass mc)
        {
            return ()->System.err.print("\n(");
        }

        @Override
        public Runnable compileEndRepeat(MessageClass mc)
        {
            return ()->System.err.print(")");
        }

    }
}
