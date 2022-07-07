/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.vesalainen.can.ArrayAction;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.candump.CanDumpService;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DeltaPositionT
{
    @Test
    public void test2() throws IOException, InterruptedException, ExecutionException
    {
        int canId = PGN.canId(129028);
        AbstractCanService svc = AbstractCanService.openCan2Udp("224.0.0.3", 11111, new DeltaCompiler());
        DBC.addN2K();
        svc.startAndWait();
    }
    private class DeltaCompiler implements SignalCompiler
    {
        private double lat;
        private double lon;
        private double rlat;
        private double rlon;
        private long time;
        private long deltatime;
        private double dlat;
        private double dlon;
        private double difr;
        private double div;
        private double sum;
        private double olat;
        private double ld;

        @Override
        public boolean needCompilation(int canId)
        {
            switch (canId)
            {
                case 0x0DF8054F:
                case 0x09F8034F:
                case 0x09F8014F:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public ArrayAction compile(MessageClass mc, SignalClass sc, ToLongFunction toLongFunction)
        {
            switch (sc.getName())
            {
                case "Position_Time":
                    return (ctx,buf) ->
                    {
                        long t = toLongFunction.applyAsLong(buf);
                        //System.err.println(t);
                        time = t;
                        deltatime = t;
                    };
                case "Delta_Position_Time":
                    return (ctx,buf) ->
                    {
                        long t = toLongFunction.applyAsLong(buf);
                        deltatime = time+t;
                        //System.err.println(deltatime);
                    };
                default:
                    return null;
            }
        }
        
        @Override
        public ArrayAction compile(MessageClass mc, SignalClass sc, ToDoubleFunction toDoubleFunction)
        {
            int pgn = PGN.pgn(mc.getId());
            switch (sc.getName())
            {
                case "Latitude":
                    if (pgn == 129029)
                    {
                        return (ctx,buf) ->
                        {
                            olat = lat;
                            lat = toDoubleFunction.applyAsDouble(buf);
                            difr = lat-olat;
                            div = difr/ld;
                            System.err.println(lat+"= "+difr+" D "+div);
                            sum = 0;
                        };
                    }
                    else
                    {
                        return (ctx,buf) ->
                        {
                            rlat = toDoubleFunction.applyAsDouble(buf);
                            //System.err.println(rlat+"R");
                        };
                    }
                case "Delta_Latitude":
                    return (ctx,buf) ->
                    {
                        ld = toDoubleFunction.applyAsDouble(buf);
                        dlat = lat+ld;
                        sum += ld;
                        System.err.println(ld+"D");
                    };
                case "Delta_Longitude":
                    return (ctx,buf) ->
                    {
                        double d = toDoubleFunction.applyAsDouble(buf);
                        //System.err.println(d+"D");
                    };
                default:
                    return null;
            }
        }

        
    }
}
