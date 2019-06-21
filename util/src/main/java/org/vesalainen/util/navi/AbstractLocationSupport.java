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

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <L> Location type
 */
public class AbstractLocationSupport<L> implements Serializable
{
    protected static final long serialVersionUID = 1L;

    protected CoordinateSupplier<L> longitudeSupplier;
    protected CoordinateSupplier<L> latitudeSupplier;
    protected LocationFactory<L> locationFactory;
    protected BoundingBoxFactory<L> boundingBoxFactory;

    public AbstractLocationSupport()
    {
    }

    public AbstractLocationSupport(AbstractLocationSupport support)
    {
        this(support.longitudeSupplier, support.latitudeSupplier, support.locationFactory, support.boundingBoxFactory);
    }

    public AbstractLocationSupport(
            CoordinateSupplier<L> longitudeSupplier, 
            CoordinateSupplier<L> latitudeSupplier, 
            LocationFactory<L> locationFactory,
            BoundingBoxFactory<L> boundingBoxFactory
    )
    {
        this.longitudeSupplier = longitudeSupplier;
        this.latitudeSupplier = latitudeSupplier;
        this.locationFactory = locationFactory;
        this.boundingBoxFactory = boundingBoxFactory;
    }

    public Comparator<L> comparator()
    {
        return new LongitudeFirstComparator();
    }
    
    public Comparator<L> longitudeOrder()
    {
        return new LongitudeOrderComparator();
    }
    
    public class LongitudeFirstComparator implements Comparator<L>, Serializable
    {

        @Override
        public int compare(L o1, L o2)
        {
            int c = Double.compare(longitudeSupplier.applyAsDouble(o1), longitudeSupplier.applyAsDouble(o2));
            if (c == 0)
            {
                return Double.compare(latitudeSupplier.applyAsDouble(o1), latitudeSupplier.applyAsDouble(o2));
            }
            else
            {
                return c;
            }
        }
        
    }
    public class LongitudeOrderComparator implements Comparator<L>, Serializable
    {

        @Override
        public int compare(L o1, L o2)
        {
            return Double.compare(longitudeSupplier.applyAsDouble(o1), longitudeSupplier.applyAsDouble(o2));
        }
        
    }
    @FunctionalInterface
    public interface CoordinateSupplier<T> extends Serializable
    {
        double applyAsDouble(T item);
    }
    @FunctionalInterface
    public interface LocationFactory<T> extends Serializable
    {
        T create(double latitude, double longitude);
    }
    @FunctionalInterface
    public interface BoundingBoxFactory<T> extends Serializable
    {
        BoundingBox<T> apply(T sw, T ne);
    }
}
