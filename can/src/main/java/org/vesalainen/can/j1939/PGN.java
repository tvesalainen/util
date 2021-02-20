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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PGN
{
    /**
     * Returns PGN of canid
     * @param canId
     * @return 
     */
    public static final int pgn(int canId)
    {
        int pf = pduFormat(canId);
        int dp = dataPage(canId);
        if (pf < 0xf0)
        {
            return (dp<<16)+(pf<<8);
        }
        else
        {
            int ps = pduSpecific(canId);
            return (dp<<16)+(pf<<8)+ps;
        }
    }
    /**
     * Returns canId from pgn by using 0xfe as source address and priority=0
     * @param pgn
     * @return 
     */
    public static final int canId(int pgn)
    {
        int pf = (pgn>>8) & 0xff;
        int dp = (pgn>>16) & 0x1;
        if (pf < 0xf0)
        {
            return (dp<<24)+(pf<<16)+0xfe;
        }
        else
        {
            int ps = pgn & 0xff;
            return (dp<<24)+(pf<<16)+(ps<<8)+0xfe;
        }
    }
    /**
     * Returns true if both canid's have same PGN
     * @param canId1
     * @param canId2
     * @return 
     */
    public static final boolean samePGN(int canId1, int canId2)
    {
        return pgn(canId1) == pgn(canId2);
    }
    public static final int sourceAddress(int canId)
    {
        return canId & 0xff;
    }
    public static final int pduSpecific(int canId)
    {
        return (canId>>8) & 0xff;
    }
    public static final int pduFormat(int canId)
    {
        return (canId>>16) & 0xff;
    }
    public static final int dataPage(int canId)
    {
        return (canId>>24) & 0x1;
    }
    public static final int extendedDataPage(int canId)
    {
        return (canId>>25) & 0x1;
    }
    public static final int messagePriority(int canId)
    {
        return (canId>>26) & 0x3;
    }
    public static final String toString(int canId)
    {
        int p = messagePriority(canId);
        int edp = extendedDataPage(canId);
        int dp = dataPage(canId);
        int pf = pduFormat(canId);
        int ps = pduSpecific(canId);
        int sa = sourceAddress(canId);
        return "P="+hex(p)+
                " edp="+hex(edp)+
                " dp="+hex(dp)+
                " pf="+hex(pf)+
                " ps="+hex(ps)+
                " sa="+hex(sa);
    }

    private static String hex(int p)
    {
        return Integer.toHexString(p);
    }
}
