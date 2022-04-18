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
 * @see <a href="https://cdn.vector.com/cms/content/know-how/_application-notes/AN-ION-1-3100_Introduction_to_J1939.pdf">Introduction to J1939</a>
 */
public class PGN
{
    /**
     * Returns PGN of canId
     * @param canId
     * @return 
     */
    public static final int pgn(int canId)
    {
        int pf = pduFormat(canId);
        int dp = dataPage(canId);
        if (pf < 0xf0)
        {
            return (dp<<16)|(pf<<8);
        }
        else
        {
            int ps = pduSpecific(canId);
            return (dp<<16)|(pf<<8)+ps;
        }
    }
    /**
     * Returns canId from pgn by using 0xfe as source address and priority=0
     * @param pgn
     * @return 
     */
    public static final int canId(int pgn)
    {
        return canId(0, pgn);
    }
    public static final int canId(int pri, int pgn)
    {
        return canId(pri, pgn, 0xfe);
    }
    public static final int canId(int pri, int pgn, int sa)
    {
        int pf = (pgn>>8) & 0xff;
        int dp = (pgn>>16) & 0x1;
        if (pf < 0xf0)
        {
            return (((pri&0xf)<<26)|(dp&0x1)<<24)|((pf&0xff)<<16)|(sa&0xff);
        }
        else
        {
            int ps = pgn & 0xff;
            return (((pri&0x7)<<26)|(dp&0x1)<<24)|((pf&0xff)<<16)|((ps&0xff)<<8)|(sa&0xff);
        }
    }
    public static final int canId(int pri, int pgn, byte da, byte sa)
    {
        int pf = (pgn>>8) & 0xff;
        int dp = (pgn>>16) & 0x1;
        if (pf < 0xf0)
        {
            return canId(pri, 0, dp, pf, da, sa);
        }
        else
        {
            throw new IllegalArgumentException("pgn is not peer-to-peer");
        }
    }
    public static final int canId(int pri, int edp, int dp, int pf, byte ps, byte sa)
    {
        if ((pri|7) != 7)
        {
            throw new IllegalArgumentException("priority out of bounds");
        }
        if ((edp|1) != 1)
        {
            throw new IllegalArgumentException("edp out of bounds");
        }
        if ((dp|1) != 1)
        {
            throw new IllegalArgumentException("dp out of bounds");
        }
        if ((pf|0xff) != 0xff)
        {
            throw new IllegalArgumentException("pf out of bounds");
        }
        return ((pri&0xf)<<26)|((edp&0x1)<<25)|((dp&0x1)<<24)|((pf&0xff)<<16)|((ps&0xff)<<8)|(sa&0xff);
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
    /**
     * SA
     * @param canId
     * @return 
     */
    public static final int sourceAddress(int canId)
    {
        return canId & 0xff;
    }
    /**
     * PS
     * @param canId
     * @return 
     */
    public static final int pduSpecific(int canId)
    {
        return (canId>>8) & 0xff;
    }
    /**
     * PF
     * @param canId
     * @return 
     */
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
        int pgn = pgn(canId);
        return "P="+hex(p)+
                " edp="+hex(edp)+
                " dp="+hex(dp)+
                " pf="+hex(pf)+
                " ps="+hex(ps)+
                " sa="+hex(sa)+
                " pgn="+pgn;
    }

    private static String hex(int p)
    {
        return Integer.toHexString(p);
    }
}
