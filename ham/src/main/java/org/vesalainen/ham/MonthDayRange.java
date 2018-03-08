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
package org.vesalainen.ham;

import org.vesalainen.util.Range;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static org.vesalainen.ham.BroadcastStationsFile.DATA_TYPE_FACTORY;
import static org.vesalainen.ham.BroadcastStationsFile.OBJECT_FACTORY;
import org.vesalainen.ham.jaxb.DateRangeType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MonthDayRange extends Range<MonthDay> implements TimeRange
{
    public MonthDayRange(DateRangeType range)
    {
        this(
                MonthDay.of(range.getFrom().getMonth(), range.getFrom().getDay()),
                MonthDay.of(range.getTo().getMonth(), range.getTo().getDay())
        );
    }

    public MonthDayRange(MonthDay from, MonthDay to)
    {
        super(from, to);
    }

    public boolean isInRange(OffsetDateTime instant)
    {
        return isInRange(MonthDay.from(instant));
    }
    public DateRangeType toDateRangeType()
    {
        DateRangeType range = OBJECT_FACTORY.createDateRangeType();
        range.setFrom(DATA_TYPE_FACTORY.newXMLGregorianCalendarDate(FIELD_UNDEFINED, from.getMonthValue(), from.getDayOfMonth(), 0));
        range.setTo(DATA_TYPE_FACTORY.newXMLGregorianCalendarDate(FIELD_UNDEFINED, to.getMonthValue(), to.getDayOfMonth(), 0));
        return range;
    }

    @Override
    public String toString()
    {
        return "MonthDayRange{" + "from=" + from + ", to=" + to + '}';
    }

}
