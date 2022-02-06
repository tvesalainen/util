/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.j1939;

import java.lang.invoke.MethodHandles;
import org.vesalainen.can.AbstractMessageData;
import org.vesalainen.code.Property;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ProductInformation extends AbstractMessageData
{
    @Property(aliases={"Nmea_2000_Database_Version"}) int nmea2000DatabaseVersion;
    @Property(aliases={"Nmea_Manufacturer_S_Product_Code"}) int nmeaManufacturerSProductCode;
    @Property(aliases={"Manufacturer_S_Model_Id"}) String manufacturerSModelId;
    @Property(aliases={"Manufacturer_S_Software_Version_Code"}) String manufacturerSSoftwareVersionCode;
    @Property(aliases={"Manufacturer_S_Model_Version"}) String manufacturerSModelVersion;
    @Property(aliases={"Manufacturer_S_Model_Serial_Code"}) String manufacturerSModelSerialCode;
    @Property(aliases={"Nmea_2000_Certification_Level"}) int nmea2000CertificationLevel;
    @Property(aliases={"Load_Equivalency"}) int loadEquivalency;
    
    public ProductInformation()
    {
        super(MethodHandles.lookup(), 126996);
    }

    public int getNmea2000DatabaseVersion()
    {
        return nmea2000DatabaseVersion;
    }

    public void setNmea2000DatabaseVersion(int nmea2000DatabaseVersion)
    {
        this.nmea2000DatabaseVersion = nmea2000DatabaseVersion;
    }

    public int getNmeaManufacturerSProductCode()
    {
        return nmeaManufacturerSProductCode;
    }

    public void setNmeaManufacturerSProductCode(int nmeaManufacturerSProductCode)
    {
        this.nmeaManufacturerSProductCode = nmeaManufacturerSProductCode;
    }

    public String getManufacturerSModelId()
    {
        return manufacturerSModelId;
    }

    public void setManufacturerSModelId(String manufacturerSModelId)
    {
        this.manufacturerSModelId = manufacturerSModelId;
    }

    public String getManufacturerSSoftwareVersionCode()
    {
        return manufacturerSSoftwareVersionCode;
    }

    public void setManufacturerSSoftwareVersionCode(String manufacturerSSoftwareVersionCode)
    {
        this.manufacturerSSoftwareVersionCode = manufacturerSSoftwareVersionCode;
    }

    public String getManufacturerSModelVersion()
    {
        return manufacturerSModelVersion;
    }

    public void setManufacturerSModelVersion(String manufacturerSModelVersion)
    {
        this.manufacturerSModelVersion = manufacturerSModelVersion;
    }

    public String getManufacturerSModelSerialCode()
    {
        return manufacturerSModelSerialCode;
    }

    public void setManufacturerSModelSerialCode(String manufacturerSModelSerialCode)
    {
        this.manufacturerSModelSerialCode = manufacturerSModelSerialCode;
    }

    public int getNmea2000CertificationLevel()
    {
        return nmea2000CertificationLevel;
    }

    public void setNmea2000CertificationLevel(int nmea2000CertificationLevel)
    {
        this.nmea2000CertificationLevel = nmea2000CertificationLevel;
    }

    public int getLoadEquivalency()
    {
        return loadEquivalency;
    }

    public void setLoadEquivalency(int loadEquivalency)
    {
        this.loadEquivalency = loadEquivalency;
    }
    
}
