/*
 * Copyright (C) 2011 Timo Vesalainen
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

import org.vesalainen.util.navi.TimeSpan;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Period implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final Period INFINITE = new Period(new Date(0), new Date(Long.MAX_VALUE));
    private Date from;
    private Date to;

    protected Period()
    {
    }

    public Period(Date from, Date to)
    {
        this.from = from;
        this.to = to;
    }

    public boolean inside(Date date)
    {
        return
                (from.equals(date) || from.before(date)) &&
                (to.equals(date) || to.after(date));
    }
    public boolean intersect(Period other)
    {
        return other.inside(from) || other.inside(to) || inside(other.from) || inside(other.to);
    }

    public Date getFrom()
    {
        return from;
    }

    public Date getTo()
    {
        return to;
    }



    public TimeSpan getTimeSpan()
    {
        return new TimeSpan(to, from);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Period other = (Period) obj;
        if (this.from != other.from && (this.from == null || !this.from.equals(other.from)))
        {
            return false;
        }
        if (this.to != other.to && (this.to == null || !this.to.equals(other.to)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + (this.from != null ? this.from.hashCode() : 0);
        hash = 89 * hash + (this.to != null ? this.to.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return from.toString()+"-"+to.toString();
    }

}
