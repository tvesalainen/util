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
package org.vesalainen.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleJMX
{

    private static String safe;
    private static Server server;
    public static final void start()
    {
        safe = System.getProperty("javax.management.builder.initial");
        System.setProperty("javax.management.builder.initial", "org.vesalainen.jmx.SimpleMBeanServerBuilder");
    }
    public static final void stop()
    {
        System.setProperty("javax.management.builder.initial", safe);
    }
}
