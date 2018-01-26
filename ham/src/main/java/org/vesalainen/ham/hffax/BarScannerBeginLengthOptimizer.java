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
public class BarScannerBeginLengthOptimizer implements BarPredicate
{
    private int barLength;
    private int expectedBegin;
    private int lastValue;

    public BarScannerBeginLengthOptimizer(int barLength, int expectedBegin)
    {
        this.barLength = barLength;
        this.expectedBegin = expectedBegin;
        reset();
    }

    public void setExpectedBegin(int expectedBegin)
    {
        this.expectedBegin = expectedBegin;
    }

    public int getExpectedBegin()
    {
        return expectedBegin;
    }
    
    @Override
    public void reset()
    {
        lastValue = Integer.MAX_VALUE;
    }
    @Override
    public boolean test(int nowBegin, int nowLength, int newBegin, int newLength, int negativeLength)
    {
        int newValue = evalue(newBegin, newLength, negativeLength);
        if (newValue < lastValue)
        {
            lastValue = newValue;
            return true;
        }
        return false;
    }
    private int evalue(int begin, int length, int negativeLength)
    {
        int negValue = negativeLength >= 40 ? 0 : 40 - negativeLength;
        return Math.abs(expectedBegin-begin)+10000*(Math.abs(barLength-length)+negValue);
    }
}
