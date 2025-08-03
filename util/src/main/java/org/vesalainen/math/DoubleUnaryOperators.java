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
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class DoubleUnaryOperators
{
    public static DoubleUnaryOperator sign(int sign, DoubleUnaryOperator f)
    {
        if (sign < 0)
        {
            return (x)->-f.applyAsDouble(x);
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
        protected  List<DoubleUnaryOperator> list = new ArrayList<>();
        
        public void add(DoubleUnaryOperator op)
        {
            list.add(op);
        }
        
        public abstract DoubleUnaryOperator build();
    }
    public static class SumBuilder extends Builder
    {

        @Override
        public DoubleUnaryOperator build()
        {
            DoubleUnaryOperator[] array = CollectionHelp.toArray(list, DoubleUnaryOperator.class);
            return (x)->
            {
                double s = 0;
                int l = array.length;
                for (int ii=0;ii<l;ii++)
                {
                    s += array[ii].applyAsDouble(x);
                }
                return s;
            };
        }
        
    }
    public static class MultiplyBuilder extends Builder
    {

        @Override
        public DoubleUnaryOperator build()
        {
            DoubleUnaryOperator[] array = CollectionHelp.toArray(list, DoubleUnaryOperator.class);
            return (x)->
            {
                double s = 1;
                int l = array.length;
                for (int ii=0;ii<l;ii++)
                {
                    s *= array[ii].applyAsDouble(x);
                }
                return s;
            };
        }
        
    }
}
