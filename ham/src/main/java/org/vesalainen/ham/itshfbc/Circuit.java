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
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.ham.maidenhead.MaidenheadLocator;
import org.vesalainen.ham.ssn.SunSpotNumber;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Circuit
{
    private static SunSpotNumber SSN;
    private String receiverLabel;
    private Location receiverLocation;
    private String transmitterLabel;
    private Location transmitterLocation;
    private double transmitterPower;
    private Noise noise;
    private RSN rsn;
    private double[] frequences;
    private ZonedDateTime date = ZonedDateTime.now();
    
    static
    {
        try
        {
            SSN = new SunSpotNumber(Paths.get("prediML.txt"));
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(Circuit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private Prediction prediction;

    public Circuit(double... frequences)
    {
        this.frequences = frequences;
    }

    public void predict() throws IOException
    {
        check();
        Predictor predictor = new Predictor();
        predictor.comment("Any VOACAP default cards may be placed in the file: VOACAP.DEF")
                .lineMax(55)
                .coeffs(Coeffs.CCIR)
                .time(1, 24, 1, true)
                .month(date.getYear(), date.getMonth())
                .sunspot(SSN.getSunSpotNumber(date))
                .label(transmitterLabel, receiverLabel)
                .circuit(transmitterLocation, receiverLocation, true)
                .system(4, noise, rsn)
                .antenna(true, 1, 2, 30, 1, 45, transmitterPower, Paths.get("default\\Isotrope"))
                .antenna(false, 2, 2, 30, 0, 0, 0, Paths.get("default\\SWWhip.VOA"))
                .frequency(frequences)
                .method(30, 0)
                .execute()
                .quit();
        prediction = predictor.predict();
    }
    public List<CircuitFrequency> frequenciesFor(int hour)
    {
        List<CircuitFrequency> list = new ArrayList<>();
        HourPrediction hourPrediction = prediction.getHourPrediction(hour);
        for (Double freq : hourPrediction.getFrequences())
        {
            CircuitFrequency cf = new CircuitFrequency(this, freq, hourPrediction);
            list.add(cf);
        }
        return list;
    }
    private void check()
    {
        Objects.requireNonNull(receiverLocation);
        Objects.requireNonNull(transmitterLocation);
        Objects.requireNonNull(noise);
        Objects.requireNonNull(rsn);
        Objects.requireNonNull(date);
        if (receiverLabel == null)
        {
            MaidenheadLocator ml = new MaidenheadLocator(receiverLocation);
            receiverLabel = ml.getSubsquare();
        }
        if (transmitterLabel == null)
        {
            MaidenheadLocator ml = new MaidenheadLocator(transmitterLocation);
            transmitterLabel = ml.getSubsquare();
        }
        if (transmitterPower <= 0)
        {
            throw new IllegalArgumentException("transmitterPower not set");
        }
    }
    public String getReceiverLabel()
    {
        return receiverLabel;
    }

    public Circuit setReceiverLabel(String receiverLabel)
    {
        this.receiverLabel = receiverLabel;
        return this;
    }

    public Location getReceiverLocation()
    {
        return receiverLocation;
    }

    public Circuit setReceiverLocation(Location receiverLocation)
    {
        this.receiverLocation = receiverLocation;
        return this;
    }

    public String getTransmitterLabel()
    {
        return transmitterLabel;
    }

    public Circuit setTransmitterLabel(String transmitterLabel)
    {
        this.transmitterLabel = transmitterLabel;
        return this;
    }

    public Location getTransmitterLocation()
    {
        return transmitterLocation;
    }

    public Circuit setTransmitterLocation(Location transmitterLocation)
    {
        this.transmitterLocation = transmitterLocation;
        return this;
    }

    public double getTransmitterPower()
    {
        return transmitterPower;
    }

    public Circuit setTransmitterPower(double transmitterPower)
    {
        this.transmitterPower = transmitterPower;
        return this;
    }

    public Noise getNoise()
    {
        return noise;
    }

    public Circuit setNoise(Noise noise)
    {
        this.noise = noise;
        return this;
    }

    public RSN getRsn()
    {
        return rsn;
    }

    public Circuit setRsn(RSN rsn)
    {
        this.rsn = rsn;
        return this;
    }

    public ZonedDateTime getDate()
    {
        return date;
    }

    public Circuit setDate(ZonedDateTime date)
    {
        this.date = date;
        return this;
    }
    
}
