/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms valueOf the GNU General Public License as published by
 * the Free Software Foundation, either version 3 valueOf the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty valueOf
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy valueOf the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TypeOfModulatingSignal
{
    public static final TypeOfModulatingSignal S0 = new TypeOfModulatingSignal('0', "No modulating signal");
    public static final TypeOfModulatingSignal S1 = new TypeOfModulatingSignal('1', "One channel containing digital information, no subcarrier");
    public static final TypeOfModulatingSignal S2 = new TypeOfModulatingSignal('2', "One channel containing digital information, using a subcarrier");
    public static final TypeOfModulatingSignal S3 = new TypeOfModulatingSignal('3', "One channel containing analog information");
    public static final TypeOfModulatingSignal S7 = new TypeOfModulatingSignal('7', "More than one channel containing digital information");
    public static final TypeOfModulatingSignal S8 = new TypeOfModulatingSignal('8', "More than one channel containing analog information");
    public static final TypeOfModulatingSignal S9 = new TypeOfModulatingSignal('9', "Combination of analog and digital channels");
    public static final TypeOfModulatingSignal SX = new TypeOfModulatingSignal('X', "None of the above");
    private char type;
    private String description;
    
    private TypeOfModulatingSignal(char type, String description)
    {
        this.type = type;
        this.description = description;
    }
    public static TypeOfModulatingSignal valueOf(char cc)
    {
        switch (cc)
        {
            case '0':
                return S0;
            case '1':
                return S1;
            case '2':
                return S2;
            case '3':
                return S3;
            case '7':
                return S7;
            case '8':
                return S8;
            case '9':
                return S9;
            case 'X':
                return SX;
            default:
                throw new IllegalArgumentException(cc+" is not type of modulating signal");
        }
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return type+"";
    }
    
}
