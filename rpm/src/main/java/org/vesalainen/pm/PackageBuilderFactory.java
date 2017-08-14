/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import org.vesalainen.pm.rpm.RPMBuilder;

/**
 *
 * @author tkv
 */
public class PackageBuilderFactory
{
    /**
     * Returns PackageBuilder for name. It also sets Mapper key to that name.
     * <p>
     * Currently supported are "rpm" and "deb". It is possible to create new
     * builders by providing implementing class name in file 
     * META-INF/services/org.vesalainen.pm.PackageBuilder
     * @param name
     * @return 
     * @see java.util.ServiceLoader
     */
    public static final PackageBuilder findPackageBuilder(String name)
    {
        Objects.requireNonNull(name, "name cannot be null");
        ServiceLoader<PackageBuilder> loader = ServiceLoader.load(PackageBuilder.class);
        Iterator<PackageBuilder> iterator = loader.iterator();
        while (iterator.hasNext())
        {
            PackageBuilder pb = iterator.next();
            if (name.equals(pb.getPackageBuilderName()))
            {
                Mapper.setKey(name);
                return pb;
            }
        }
        throw new UnsupportedOperationException("package builder "+name+" not found");
    }
}
