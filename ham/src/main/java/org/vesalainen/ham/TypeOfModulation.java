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
public enum TypeOfModulation
{
    N("Unmodulated carrier"),
    A("Double-sideband amplitude modulation (e.g. AM broadcast radio)"),
    H("Single-sideband with full carrier (e.g. as used by CHU)"),
    R("Single-sideband with reduced or variable carrier"),
    J("Single-sideband with suppressed carrier (e.g. Shortwave utility and amateur stations)"),
    B("Independent sideband (two sidebands containing different signals)"),
    C("Vestigial sideband (e.g. NTSC)"),
    F("Frequency modulation (e.g. FM broadcast radio)"),
    G("Phase modulation"),
    D("Combination of AM and FM or PM"),
    P("Sequence of pulses without modulation"),
    K("Pulse amplitude modulation"),
    L("Pulse width modulation (e.g. as used by WWVB)"),
    M("Pulse position modulation"),
    Q("Sequence of pulses, phase or frequency modulation within each pulse"),
    V("Combination of pulse modulation methods"),
    W("Combination of any of the above"),
    X("None of the above")
    ;
    private String description;

    private TypeOfModulation(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

}
