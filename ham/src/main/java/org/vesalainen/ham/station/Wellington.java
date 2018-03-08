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
package org.vesalainen.ham.station;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.OffsetTimeRange;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Wellington extends DefaultCustomizer
{

    @Override
    public XMLGregorianCalendar[] convertStart(int start1, int start2)
    {
        XMLGregorianCalendar[] arr = new XMLGregorianCalendar[8];
        for (int ii=0;ii<4;ii++)
        {
            arr[ii] = dataTypeFactory.newXMLGregorianCalendarTime(start1/100, 15*ii, 0, 0);
            arr[ii+4] = dataTypeFactory.newXMLGregorianCalendarTime(start2/100, 15*ii, 0, 0);
        }
        return arr;
    }

    @Override
    public List<OffsetTimeRange> convertRanges(double frequency, List<OffsetTimeRange> ranges)
    {
        int offset = -1;
        if (Primitives.equals(3247.4, frequency))
        {
            offset = 45;
        }
        if (Primitives.equals(5807, frequency))
        {
            offset = 0;
        }
        if (Primitives.equals(9459, frequency))
        {
            offset = 15;
        }
        if (Primitives.equals(13550.5, frequency))
        {
            offset = 30;
        }
        if (Primitives.equals(16340.1, frequency))
        {
            offset = 45;
        }
        if (offset == -1)
        {
            throw new UnsupportedOperationException(frequency+ "kHz");
        }
        if (ranges != null)
        {
            List<OffsetTimeRange> list = new ArrayList<>();
            for (OffsetTimeRange r : ranges)
            {
                OffsetTime from = r.getFrom();
                while (r.isInRange(from))
                {
                    OffsetTime next = from.plusMinutes(15);
                    list.add(new OffsetTimeRange(from, next));
                    from = from.plusHours(1);
                }
            }
            return list;
        }
        else
        {
            return allBroadcastTimes(offset);
        }
    }
    public List<OffsetTimeRange> allBroadcastTimes(int offset)
    {
        List<OffsetTimeRange> list = new ArrayList<>();
        OffsetTime from = OffsetTime.of(0, offset, 0, 0, ZoneOffset.UTC);
        for (int ii=0;ii<24;ii++)
        {
            OffsetTime next = from.plusMinutes(15);
            list.add(new OffsetTimeRange(from, next));
            from = from.plusHours(1);
        }
        return list;
    }

    @Override
    public String mapLine(String line)
    {
        return super.mapLine(line)
                .replace("TNZ - TASMAN SEA - NEW ZEALAND", "TNZ 31S - 47S, 147E - 180E - 175W")
                .replace("SWP - SOUTHWEST PACIFIC", "SWP  0S - 55S, 170E - 180E - 128W")
                ;
    }

}
