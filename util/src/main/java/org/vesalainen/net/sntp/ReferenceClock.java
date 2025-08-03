/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.net.sntp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum ReferenceClock
{
    /**
     * Geosynchronous Orbit Environment Satellite
     */
     GOES,
     /**
      * Global Position System
      */
     GPS,
     /**
      * Galileo Positioning System
      */
     GAL,
     /**
      * Generic pulse-per-second
      */
     PPS,
     /**
      * Inter-Range Instrumentation Group
      */
     IRIG,
     /**
      * LF Radio WWVB Ft. Collins, CO 60 kHz
      */
     WWVB,
     /**
      * LF Radio DCF77 Mainflingen, DE 77.5 kHz
      */
     DCF,
     /**
      * LF Radio HBG Prangins, HB 75 kHz
      */
     HBG,
     /**
      * LF Radio MSF Anthorn, UK 60 kHz
      */
     MSF,
     /**
      * LF Radio JJY Fukushima, JP 40 kHz, Saga, JP 60 kHz
      */
     JJY,
     /**
      * MF Radio LORAN C station, 100 kHz
      */
     LORC,
     /**
      * MF Radio Allouis, FR 162 kHz
      */
     TDF,
     /**
      * HF Radio CHU Ottawa, Ontario
      */
     CHU,
     /**
      * HF Radio WWV Ft. Collins, CO
      */
     WWV,
     /**
      * HF Radio WWVH Kauai, HI
      */
     WWVH,
     /**
      * NIST telephone modem
      */
     NIST,
     /**
      * NIST telephone modem
      */
     ACTS,
     /**
      * USNO telephone modem
      */
     USNO,
     /**
      * European telephone modem
      */
     PTB,      
}
