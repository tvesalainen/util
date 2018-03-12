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
package org.vesalainen.radiorecorder;

import java.io.IOException;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RadioRecorder extends LoggingCommandLine
{
    public static CachedScheduledThreadPool POOL = new CachedScheduledThreadPool();
    private static final String NMEA_GROUP = "-nmea-group";
    private static final String NMEA_PORT = "-nmea-port";
    private LocationService locationService;

    public RadioRecorder()
    {
        addOption(NMEA_GROUP, "NMEA Group Address", null, "224.0.0.3");
        addOption(NMEA_PORT, "NMEA port", null, 10110);
    }
    public void start() throws IOException
    {
        String group = getOption(NMEA_GROUP);
        int port = getOption(NMEA_PORT);
        locationService = new LocationService(group, port, POOL);
        locationService.start();
    }
    public void stop()
    {
        POOL.shutdownNow();
        locationService.stop();
    }

    public static void main(String... args)
    {
        try
        {
            RadioRecorder recorder = new RadioRecorder();
            recorder.command(args);
            recorder.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
