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
package org.vesalainen.can.dbc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.IntFunction;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.can.ArrayFuncs;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PgnPropertyDump
{
    
    public PgnPropertyDump()
    {
    }

    @Test
    public void test() throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream("/n2k.dbc"))
        {
            parser.parse(is, dbcFile);
            MessageClass msg = dbcFile.getMessageForPgn(126996);
            print(msg);
        }
    }

    private void print(MessageClass msg)
    {
        msg.forEach((sc)->
        {
            String name = sc.getName();
            String property = CamelCase.property(name);
            System.err.print("@Property(aliases={\""+name+"\"} ");
            switch (sc.getSignalType())
            {
                case LOOKUP:
                case BINARY:
                case INT:
                    System.err.print("int ");
                    break;
                case LONG:
                    System.err.print("long ");
                    break;
                case DOUBLE:
                    System.err.print("double ");
                    break;
                case ASCIIZ:
                case AISSTRING:
                case AISSTRING2:
                    System.err.print("String ");
                    break;
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
            System.err.println(property+";");
        });
    }
}
