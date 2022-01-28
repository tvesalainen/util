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
package org.vesalainen.can.j1939;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PGNTest
{
    
    public PGNTest()
    {
    }

    @Test
    public void testPGN()
    {
        assertEquals(129025, PGN.pgn(0x09F80110));
        System.err.println(PGN.toString(0x09F80110));
        assertEquals(129026, PGN.pgn(0x09F80210));
        System.err.println(PGN.toString(0x09F80210));
        assertEquals(129029, PGN.pgn(0x0DF80510));
        System.err.println(PGN.toString(0x0DF80510));
        assertEquals(61444, PGN.pgn((int) 2364540158L));
        System.err.println(PGN.toString((int) 2364540158L));
        assertEquals(65265, PGN.pgn((int) 2566844926L));
        System.err.println(PGN.toString((int) 2566844926L));
    }
    @Test
    public void testCanId()
    {
        int pgn = PGN.pgn(0x09F80110);
        int canId = PGN.canId(pgn);
        assertTrue(PGN.samePGN(pgn, PGN.pgn(canId)));
    }
    @Test
    public void testCanId2()
    {
        int canId = PGN.canId(2, 1, 1, 238, 5, 4);
        assertEquals(2, PGN.messagePriority(canId));
        assertEquals(1, PGN.extendedDataPage(canId));
        assertEquals(1, PGN.dataPage(canId));
        assertEquals(238, PGN.pduFormat(canId));
        assertEquals(5, PGN.pduSpecific(canId));
        assertEquals(4, PGN.sourceAddress(canId));
    }
    @Test
    public void testCanId3()
    {
        int canId = PGN.canId(2, 60928, 5, 4);
        assertEquals(2, PGN.messagePriority(canId));
        assertEquals(60928, PGN.pgn(canId));
        assertEquals(5, PGN.pduSpecific(canId));
        assertEquals(4, PGN.sourceAddress(canId));
    }
}
