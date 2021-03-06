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
import java.nio.file.Paths;
import java.time.Month;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PredictorTest
{
    
    public PredictorTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINEST);
    }

    @Test
    public void test1() throws IOException
    {
        GeoDB db = new GeoDB();
        GeoLocation norl = db.search(100, of("CITY", "NEW ORLEANS"));
        GeoLocation colon = db.search(100, of("CITY", "COLON"), of("NATION", "PANAMA"));
        Predictor p = new Predictor();
        p.comment("Any VOACAP default cards may be placed in the file: VOACAP.DEF")
                .lineMax(55)
                .coeffs(Coeffs.CCIR)
                .time(1, 24, 1, true)
                .month(2018, Month.FEBRUARY)
                .sunspot(12.0)
                .label("NEW ORLEANS", "COLON")
                .circuit(norl.getLocation(), colon.getLocation(), true)
                .system(4, Noise.RURAL, RSN.SSB)
                .antenna(true, 1, 2, 30, 1, 45, 4, Paths.get("default\\Isotrope"))
                .antenna(false, 2, 2, 30, 0, 0, 4, Paths.get("default\\SWWhip.VOA"))
                .frequency(4, 7, 12, 14, 28)
                .method(30, 0)
                .execute()
                .quit();
        System.err.println(p);
        Prediction prediction = p.predict();
        HourPrediction hourPrediction = prediction.getHourPrediction(12);
        assertEquals(56.0, hourPrediction.getValue("SNRxx", 14.0), 1e-8);
        assertEquals(68.0, hourPrediction.getValue("SNR", 14.0), 1e-8);
    }
    @Test
    public void testAntenna()
    {
        Predictor p = new Predictor();
        p.antenna(true, 1, 2, 30, 1, 45, 4, Paths.get("default\\Isotrope"));
        String exp = "ANTENNA       1    1    2   30     1.000[default\\Isotrope     ] 45.0    4.0000\r\n";
        System.err.println(exp);
        System.err.println(p.toString());
        assertEquals(exp, p.toString());
    }
    
}
