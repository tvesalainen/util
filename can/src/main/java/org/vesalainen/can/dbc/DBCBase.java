/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCBase
{
    
    protected String comment = "";
    protected Map<String,Object> values = new HashMap<>();

    public DBCBase()
    {
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setValue(String name, Object value)
    {
        values.put(name, value);
    }
    public Object getValue(String name)
    {
        return values.get(name);
    }

    public Map<String, Object> getValues()
    {
        return values;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.comment);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final DBCBase other = (DBCBase) obj;
        if (!Objects.equals(this.comment, other.comment))
        {
            return false;
        }
        return true;
    }
}
