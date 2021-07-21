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

import static java.lang.Integer.min;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import static java.util.logging.Level.*;
import static org.vesalainen.can.SignalType.*;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SingleMessage extends AbstractMessage
{
    protected final int pgn;
    protected final int source;
    protected final int priority;
    protected byte[] buf;
    protected String comment;
    protected List<Runnable> signals = new ArrayList<>();

    public SingleMessage(int canId, int len, String comment)
    {
        this.source = PGN.sourceAddress(canId);
        this.priority = PGN.messagePriority(canId);
        this.pgn = PGN.pgn(canId);
        this.buf = new byte[len];
        this.comment = comment;
    }
    
    public void addBegin(MessageClass mc, SignalCompiler compiler)
    {
        Runnable rn = compiler.compileBegin(mc);
        if (rn != null)
        {
            signals.add(rn);
        }
    }
    public void addSignal(MessageClass mc, SignalClass sc, SignalCompiler compiler)
    {
        IntSupplier is;
        LongSupplier ls;
        DoubleSupplier ds;
        Supplier<String> ss;
        Runnable rn;
        switch (sc.getSignalType())
        {
            case INT:
                is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                rn = compiler.compile(mc, sc, is);
                break;
            case LONG:
                ls = ArrayFuncs.getLongSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                rn = compiler.compile(mc, sc, ls);
                break;
            case DOUBLE:
                ls = ArrayFuncs.getLongSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                double factor = sc.getFactor();
                double offset = sc.getOffset();
                ds = ()->factor*ls.getAsLong()+offset;
                rn = compiler.compile(mc, sc, ds);
                break;
            case LOOKUP:
                is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                rn = compiler.compile(mc, sc, is, sc.getMapper());
                break;
            case BINARY:
                rn = compiler.compileBinary(mc, sc);
                break;
            case ASCIIZ:
                ss = ArrayFuncs.getZeroTerminatingStringSupplier(sc.getStartBit()/8, sc.getSize()/8, buf);
                rn = compiler.compileASCII(mc, sc, ss);
                break;
            default:
                throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
        }
        if (rn != null)
        {
            signals.add(rn);
        }
    }
    public void addEnd(MessageClass mc, SignalCompiler compiler)
    {
        Runnable rn = compiler.compileEnd(mc);
        if (rn != null)
        {
            signals.add(rn);
        }
    }
    @Override
    protected boolean update(AbstractCanService service)
    {
        try
        {
            ByteBuffer frame = service.getFrame();
            frame.position(8);
            frame.get(buf, 0, min(buf.length, frame.remaining()));
            return true;
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "execute %s", comment);
        }
        return false;
    }

    @Override
    protected void execute(CachedScheduledThreadPool executor)
    {
        info("execute pgn=%d src=%d %s\n%s", pgn, source, comment, HexUtil.toString(buf));
        signals.forEach((r)->
        {
            try
            {
                r.run();
            }
            catch (Exception ex)
            {
                log(WARNING, ex, "execute %s", comment);
            }
        });
    }

    
}
