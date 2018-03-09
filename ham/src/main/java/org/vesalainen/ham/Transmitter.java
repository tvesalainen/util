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
package org.vesalainen.ham;

import java.time.OffsetDateTime;
import org.vesalainen.ham.jaxb.TransmitterType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Transmitter implements TimeRange
{
    
    private Station station;
    private TransmitterType transmitter;
    private EmissionClass emissionClass;
    private TimeRange timeRanges;

    public Transmitter(Station station, TransmitterType transmitter)
    {
        this.station = station;
        this.transmitter = transmitter;
        this.emissionClass = new EmissionClass(transmitter.getEmission());
        this.timeRanges = TimeRanges.orTimeRanges(transmitter.getTime());
    }

    @Override
    public boolean isInRange(OffsetDateTime time)
    {
        return timeRanges.isInRange(time);
    }

    public EmissionClass getEmissionClass()
    {
        return emissionClass;
    }

    public Station getStation()
    {
        return station;
    }

    public String getCallSign()
    {
        return transmitter.getCallSign();
    }

    public double getFrequency()
    {
        return transmitter.getFrequency();
    }

    public String getEmission()
    {
        return transmitter.getEmission();
    }

    public Double getPower()
    {
        return transmitter.getPower();
    }
    
}
