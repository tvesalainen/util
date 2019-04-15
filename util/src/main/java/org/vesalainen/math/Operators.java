/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Operators
{
    public static DoubleBinaryOperator sign(int sign, DoubleBinaryOperator f)
    {
        if (sign < 0)
        {
            return (x,y)->-f.applyAsDouble(x,y);
        }
        else
        {
            return f;
        }
    }
    public static SumBuilder sumBuilder()
    {
        return new SumBuilder();
    }
    public static MultiplyBuilder multiplyBuilder()
    {
        return new MultiplyBuilder();
    }
    private static abstract class Builder
    {
        protected  List<DoubleBinaryOperator> list = new ArrayList<>();
        
        public void add(DoubleBinaryOperator op)
        {
            list.add(op);
        }
        
        public abstract DoubleBinaryOperator build();
    }
    public static class SumBuilder extends Builder
    {

        @Override
        public DoubleBinaryOperator build()
        {
            DoubleBinaryOperator[] array = CollectionHelp.toArray(list, DoubleBinaryOperator.class);
            return (x,y)->
            {
                double s = 0;
                int l = array.length;
                for (int ii=0;ii<l;ii++)
                {
                    s += array[ii].applyAsDouble(x,y);
                }
                return s;
            };
        }
        
    }
    public static class MultiplyBuilder extends Builder
    {

        @Override
        public DoubleBinaryOperator build()
        {
            DoubleBinaryOperator[] array = CollectionHelp.toArray(list, DoubleBinaryOperator.class);
            return (x,y)->
            {
                double s = 1;
                int l = array.length;
                for (int ii=0;ii<l;ii++)
                {
                    s *= array[ii].applyAsDouble(x,y);
                }
                return s;
            };
        }
        
    }
}
