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
package org.vesalainen.can.dbc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.j1939.PGN;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBC
{
    protected static final Map<Integer,MessageClass> canIdMap = new HashMap<>();
    protected static final Map<Integer,MessageClass> pgnMap = new HashMap<>();
    private static boolean hasJ1939;
    private static boolean hasN2K;
    
    public static void addJ1939()
    {
        addDBCFile(AbstractCanService.class.getResourceAsStream("/j1939.dbc"));
        DBC.hasJ1939 = true;
    }
    public static void addN2K()
    {
        if (!hasJ1939)
        {
            addJ1939();
        }
        addDBCFile(AbstractCanService.class.getResourceAsStream("/n2k.dbc"));
        DBC.hasN2K = true;
    }

    public static boolean hasJ1939()
    {
        return hasJ1939;
    }

    public static boolean hasN2K()
    {
        return hasN2K;
    }
    
    public static MessageClass getMessage(int canId)
    {
        return canIdMap.get(canId);
    }
    public static MessageClass getPgnMessage(int pgn)
    {
        return pgnMap.get(pgn);
    }
    public static <T> void addDBCFile(T path)
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        parser.parse(path, dbcFile);
        String protocolType = (String)dbcFile.getAttributeValue("ProtocolType");
        if (protocolType == null)
        {
            protocolType = "";
        }
        switch (protocolType)
        {
            case "":
            case "StandardDBC":
                dbcFile.forEach((mc)->
                {
                    canIdMap.put(mc.getId(), mc);
                });
                break;
            case "J1939":
            case "N2K":
                dbcFile.forEach((mc)->
                {
                    pgnMap.put(PGN.pgn(mc.getId()), mc);
                });
                break;
            default:
                throw new UnsupportedOperationException(protocolType+" not supported");
        }
    }
    public static void forEachFastMessage(IntConsumer act)
    {
        pgnMap.forEach((pgn, mc)->
        {
            String type = (String) mc.getAttributeValue("MessageType");
            if ("Fast".equals(type))
            {
                act.accept(pgn);
            }
        });
    }
}
