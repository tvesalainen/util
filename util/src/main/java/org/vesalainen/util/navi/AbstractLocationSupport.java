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
package org.vesalainen.util.navi;

import java.util.function.ToDoubleFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <L> Location type
 */
public class AbstractLocationSupport<L>
{
    protected ToDoubleFunction<L> longitudeSupplier;
    protected ToDoubleFunction<L> latitudeSupplier;
    protected LocationFactory<L> locationFactory;

    public AbstractLocationSupport()
    {
    }

    public AbstractLocationSupport(AbstractLocationSupport support)
    {
        this(support.longitudeSupplier, support.latitudeSupplier, support.locationFactory);
    }

    public AbstractLocationSupport(ToDoubleFunction<L> longitudeSupplier, ToDoubleFunction<L> latitudeSupplier, LocationFactory<L> locationFactory)
    {
        this.longitudeSupplier = longitudeSupplier;
        this.latitudeSupplier = latitudeSupplier;
        this.locationFactory = locationFactory;
    }
    
    @FunctionalInterface
    public interface LocationFactory<T>
    {
        T create(double latitude, double longitude);
    }
}
