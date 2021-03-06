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
package org.vesalainen.code;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.function.Predicate;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class APS2 extends AnnotatedPropertyStore implements Transactional
{
    @Property byte b;
    @Property String string;

    public APS2()
    {
        super(MethodHandles.lookup());
    }

    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties, Predicate<String> isModified)
    {
        for (String p : updatedProperties)
        {
            System.err.println(p+" isModified "+isModified.test(p));
        }
    }

}
