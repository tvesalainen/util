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
 * @see <a href="https://en.wikipedia.org/wiki/Types_of_radio_emissions">Types of radio emissions</a>
 */
public class EmissionClass
{
    private TypeOfModulation typeOfModulation;
    private TypeOfModulatingSignal typeOfModulatingSignal;
    private TypeOfTransmittedInformation typeOfTransmittedInformation;

    public EmissionClass(String emissionClass)
    {
        if (emissionClass.length() != 3)
        {
            throw new UnsupportedOperationException(emissionClass);
        }
        this.typeOfModulation = TypeOfModulation.valueOf(emissionClass.substring(0, 1));
        this.typeOfModulatingSignal = TypeOfModulatingSignal.valueOf(emissionClass.charAt(1));
        this.typeOfTransmittedInformation = TypeOfTransmittedInformation.valueOf(emissionClass.substring(2, 3));
    }

    public TypeOfModulation getTypeOfModulation()
    {
        return typeOfModulation;
    }

    public TypeOfModulatingSignal getTypeOfModulatingSignal()
    {
        return typeOfModulatingSignal;
    }

    public TypeOfTransmittedInformation getTypeOfTransmittedInformation()
    {
        return typeOfTransmittedInformation;
    }

    public String getDescription()
    {
        return typeOfModulation.getDescription() +" / "+ typeOfModulatingSignal.getDescription() +" / "+ typeOfTransmittedInformation.getDescription();
    }
    @Override
    public String toString()
    {
        return typeOfModulation.toString() + typeOfModulatingSignal.toString() + typeOfTransmittedInformation.toString();
    }
    
}
