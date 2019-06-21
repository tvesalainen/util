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
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import org.vesalainen.util.Range;
import org.vesalainen.util.RangeDB;
import org.vesalainen.util.RangeDB.Entry;
import org.vesalainen.util.navi.AbstractLocationSupport.CoordinateSupplier;

/**
 *
 * AbstractLocationRangeMap is a base class for location range mappings.
 * 
 * <p>Note! Same value can be mapped with different BoundingBoxes however when searched
 * with overlapping method only distinct values are returned.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractLocationRangeMap<L,T> extends RangeDB<L,T> implements Serializable
{
    protected static final long serialVersionUID = 1L;

    private CoordinateSupplier<L> latitudeSupplier;

    protected AbstractLocationRangeMap(AbstractLocationSupport support)
    {
        super(support, support.longitudeOrder());
        this.latitudeSupplier = support.latitudeSupplier;
    }

    public void put(BoundingBox<L> bb, T value)
    {
        if (bb.isWestToEast())
        {
            super.put(bb.getSouthWest(), bb.getNorthEast(), value);
        }
        else
        {
            BoundingBox<L>[] arr = bb.splitAntiMeridian();
            super.put(arr[0].getSouthWest(), arr[0].getNorthEast(), value);
            super.put(arr[1].getSouthWest(), arr[1].getNorthEast(), value);
        }
    }

    public Stream<T> overlapping(BoundingBox<L> bb)
    {
        if (bb.isWestToEast())
        {
            return super.overlapping(bb.getSouthWest(), bb.getNorthEast());
        }
        else
        {
            BoundingBox<L>[] arr = bb.splitAntiMeridian();
            Stream<T> s1 = super.overlapping(arr[0].getSouthWest(), arr[0].getNorthEast());
            Stream<T> s2 = super.overlapping(arr[1].getSouthWest(), arr[1].getNorthEast());
            return Stream.concat(s1, s2).distinct();
        }
    }

    @Override
    protected Stream<Entry> overlappingEntries(Range<L> range)
    {
        double south = latitudeSupplier.applyAsDouble(range.getFrom());
        double north = latitudeSupplier.applyAsDouble(range.getTo());
        return super.overlappingEntries(range).filter((Entry e)->
        {
            Range<L> r = e.getRange();
            double s = latitudeSupplier.applyAsDouble(r.getFrom());
            double n = latitudeSupplier.applyAsDouble(r.getTo());
            return 
                    (s >= south && s <= north) ||
                    (n >= south && n <= north) ||
                    (north >= s && north <= n) ||
                    (south >= s && south <= n)
                    ;
        });
    }

    @Override
    public Stream<T> overlapping(Range<L> range)
    {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public Stream<T> overlapping(L from, L to)
    {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public void put(L from, L to, T value)
    {
        throw new UnsupportedOperationException("not supported");
    }

}
