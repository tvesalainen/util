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
package org.vesalainen.management;

import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES_LIST;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class OpenTypeUtil
{
    private static final Class<?>[] types;
    static
    {
        int index = 0;
        types = new Class<?>[ALLOWED_CLASSNAMES_LIST.size()];
        for (String cn : ALLOWED_CLASSNAMES_LIST)
        {
            try
            {
                types[index++] = Class.forName(cn);
            }
            catch (ClassNotFoundException ex)
            {
                Logger.getLogger(OpenTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static boolean isOpenType(Class<?> type)
    {
        if (type.isPrimitive())
        {
            return true;
        }
        for (Class<?> t : types)
        {
            if (t.isAssignableFrom(type))
            {
                return true;
            }
        }
        return false;
    }
}
