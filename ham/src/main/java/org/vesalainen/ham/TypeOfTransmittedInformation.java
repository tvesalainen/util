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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum TypeOfTransmittedInformation
{
    N("No transmitted information"),
    A("Aural telegraphy, intended to be decoded by ear, such as Morse code"),
    B("Electronic telegraphy, intended to be decoded by machine (radioteletype and digital modes)"),
    C("Facsimile (still images)"),
    D("Data transmission, telemetry or telecommand (remote control)"),
    E("Telephony (voice or music intended to be listened to by a human)"),
    F("Video (television signals)"),
    W("Combination of any of the above"),
    X("None of the above")
    ;
    private String description;

    private TypeOfTransmittedInformation(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

}
