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
package org.vesalainen.ham.itshfbc;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CircuitTest
{
    
    public CircuitTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        GeoDB db = new GeoDB();
        GeoLocation norl = db.search(100, of("CITY", "NEW ORLEANS"));
        GeoLocation colon = db.search(100, of("CITY", "COLON"), of("NATION", "PANAMA"));
        Circuit circuit = new Circuit(4.3179, 8.5039, 12.7899, 17.1464)
                .setReceiverLabel("COLON")
                .setReceiverLocation(colon.getLocation())
                .setTransmitterLabel("NEW ORLEANS")
                .setTransmitterLocation(norl.getLocation())
                .setTransmitterPower(4)
                .setNoise(Noise.RESIDENTIAL)
                .setRsn(RSN.SSB)
                .setSunSpotNumbers(16)
                ;
        circuit.predict();
        List<CircuitFrequency> frequenciesFor = circuit.frequenciesFor(1);
        frequenciesFor.sort(null);
    }
    
}
