/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.navi;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatchTest
{
    
    public AnchorWatchTest()
    {
    }

    /**
     * Test of update method, of class AnchorWatch.
     */
    @Test
    public void testUpdate()
    {
        AnchorWatch aw = new AnchorWatch();
        URL url = AnchorWatchTest.class.getClassLoader().getResource("atAnchor.nmea");
        NMEAParser parser = NMEAParser.newInstance();
        NO no = new NO(aw);
        try
        {
            parser.parse(url, no, null);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    private class NO extends AbstractNMEAObserver
    {
        AnchorWatch anchorWatch;
        private float longitude;
        private float latitude;

        public NO(AnchorWatch anchorWatch)
        {
            this.anchorWatch = anchorWatch;
        }

        @Override
        public void setLongitude(float longitude)
        {
            this.longitude = longitude;
        }

        @Override
        public void setLatitude(float latitude)
        {
            this.latitude = latitude;
        }

        @Override
        public void commit(String reason)
        {
            anchorWatch.update(longitude, latitude);
        }
        
    }
}
