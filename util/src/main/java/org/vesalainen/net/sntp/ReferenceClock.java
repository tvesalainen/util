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
     * uncalibrated local clock
     */
      LOCL,
      /**
       * calibrated Cesium clock
       */
      CESM,
      /**
       * calibrated Rubidium clock
       */
      RBDM,
      /**
       * calibrated quartz clock or other pulse-per-second source
       */
      PPS,
      /**
       * Inter-Range Instrumentation Group
       */
      IRIG,
      /**
       * NIST telephone modem service
       */
      ACTS,
      /**
       * USNO telephone modem service
       */
      USNO,
      /**
       * PTB (Germany) telephone modem service
       */
      PTB,
      /**
       * Allouis (France) Radio 164 kHz
       */
      TDF,
      /**
       * Mainflingen (Germany) Radio 77.5 kHz
       */
      DCF,
      /**
       * Rugby (UK) Radio 60 kHz
       */
      MSF,
      /**
       * Ft. Collins (US) Radio 2.5, 5, 10, 15, 20 MHz
       */
      WWV,
      /**
       * Boulder (US) Radio 60 kHz
       */
      WWVB,
      /**
       * Kauai Hawaii (US) Radio 2.5, 5, 10, 15 MHz
       */
      WWVH,
      /**
       * Ottawa (Canada) Radio 3330, 7335, 14670 kHz
       */
      CHU,
      /**
       * LORAN-C radionavigation system
       */
      LORC,
      /**
       * OMEGA radionavigation system
       */
      OMEG,
      /**
       * Global Positioning Service
       */
      GPS,
      /**
       * Galileo Positioning System
       */
      GAL
}
