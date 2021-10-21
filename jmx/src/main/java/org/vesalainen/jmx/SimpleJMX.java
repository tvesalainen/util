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

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleJMX
{

    /**
     * Sets system property:
     * <p>org.vesalainen.jmxremote.port to port
     * @param port 
     */
    public static final void setPort(int port)
    {
        if (port < 0 || port > 65535)
        {
            throw new IllegalArgumentException(port+" port out of range 0 - 65535");
        }
        System.getProperty("org.vesalainen.jmxremote.port");
        System.setProperty("org.vesalainen.jmxremote.port", Integer.toString(port));
    }
    /**
     * Sets system property:
     * <p>org.vesalainen.jmxremote.timeout to timeout
     * <p>Note! Actual System property is in millis!
     * @param port 
     */
    public static final void setTimeout(long timeout, TimeUnit unit)
    {
        System.setProperty("org.vesalainen.jmxremote.timeout", Long.toString(TimeUnit.MILLISECONDS.convert(timeout, unit)));
    }
    /**
     * Starts SimpleJMX by setting system properties: 
     * <p>javax.management.builder.initial to org.vesalainen.jmx.SimpleMBeanServerBuilder
     * <p>
     * This is meant for testing. It is recommended to set these properties
     * the normal way. 
     */
    public static final void start()
    {
        System.setProperty("javax.management.builder.initial", "org.vesalainen.jmx.SimpleMBeanServerBuilder");
    }
}
