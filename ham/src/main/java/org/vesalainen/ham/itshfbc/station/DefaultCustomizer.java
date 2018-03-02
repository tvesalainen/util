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
package org.vesalainen.ham.itshfbc.station;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.jaxb.StationType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DefaultCustomizer
{
    protected DatatypeFactory dataTypeFactory;

    public DefaultCustomizer()
    {
        try
        {
            this.dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public static DefaultCustomizer getInstance(String filename)
    {
        String classname = "org.vesalainen.ham.itshfbc.station."+filename.substring(0, filename.length()-4);
        try
        {
            Class<?> cls = Class.forName(classname);
            return (DefaultCustomizer) cls.newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            return new DefaultCustomizer();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public XMLGregorianCalendar[] convertStart(int start1, int start2)
    {
        XMLGregorianCalendar time1 = dataTypeFactory.newXMLGregorianCalendarTime(start1/100, start1%100, 0, 0);
        XMLGregorianCalendar time2 = dataTypeFactory.newXMLGregorianCalendarTime(start2/100, start2%100, 0, 0);
        return new XMLGregorianCalendar[]{time1, time2};
    }
    public String convertRanges(double frequency, String range)
    {
        return range;
    }
    public void afterProcess(StationType station)
    {
        
    }
    public String mapLine(String line)
    {
        return line.trim().replace("EQ", "00S");
    }
    public String scheduleLine(String line)
    {
        String scheduleLine = line
                .trim()
                .replace("LATEST", "")
                .trim()
                ;
        if (!scheduleLine.contains("/576"))
        {
            scheduleLine = scheduleLine+" 120/576";
        }
        return scheduleLine;
    }
    
    public String transmitterLine(String line)
    {
        return line.trim();
    }
}
