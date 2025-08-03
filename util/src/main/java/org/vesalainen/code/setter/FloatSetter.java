/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.code.setter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface FloatSetter extends Setter<FloatSetter>
{

    void set(float v);

    public default void set(double d)
    {
        set((float) d);
    }
    
    @Override
    public default void setObject(Object v)
    {
        set((float) v);
    }

    @Override
    public default FloatSetter andThen(FloatSetter then)
    {
        return (v) ->
        {
            set(v);
            then.set(v);
        };
    }

    @Override
    public default FloatSetter andThen(Runnable then)
    {
        return (v) ->
        {
            set(v);
            then.run();
        };
    }

}
