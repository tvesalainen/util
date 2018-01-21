/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.hffax;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Fax
{
    
    protected static final long LINE_LEN = 500000;
    protected static final long START_BLACK_LEN = 500000 * 2182 / 2300;
    protected static final long STOP_LEN = 500000 / 225;
    protected static final long STOP_BLACK_LEN = 7 * STOP_LEN / 20;
    protected static final int STOP_ERROR_LIMIT = 300;

    public Fax()
    {
    }
    /**
     * Returns true if value differs only 1 percent.
     * @param expected
     * @param value
     * @return 
     */
    public static boolean isAbout(double expected, double value)
    {
        return isAbout(expected, value, 1);
    }
    /**
     * Returns true if value differs only tolerance percent.
     * @param expected
     * @param value
     * @param tolerance
     * @return 
     */
    public static boolean isAbout(double expected, double value, double tolerance)
    {
        double delta = expected*tolerance/100.0;
        return value > expected-delta && value < expected+delta;
    }
    public static final int topBlackInPixels(int width)
    {
        return inPixels(width, 2182);
    }
    public static final int topWhiteInPixels(int width)
    {
        return inPixels(width, 2300-2182);
    }
    /**
     * Converts len from 2300 width.
     * @param width
     * @param len
     * @return 
     */
    public static final int inPixels(int width, int len)
    {
        return width*len / 2300;
    }
}
