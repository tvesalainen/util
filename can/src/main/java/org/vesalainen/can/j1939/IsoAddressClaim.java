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
public class IsoAddressClaim extends AbstractMessageData
{
    @Property(aliases={"Unique_Number"}) int uniqueNumber;
    @Property(aliases={"Manufacturer_Code"}) int manufacturerCode;
    @Property(aliases={"Device_Instance_Lower"}) int deviceInstanceLower;
    @Property(aliases={"Device_Instance_Upper"}) int deviceInstanceUpper;
    @Property(aliases={"Device_Function"}) int deviceFunction;
    @Property(aliases={"Reserved"}) int reserved;
    @Property(aliases={"Device_Class"}) int deviceClass;
    @Property(aliases={"System_Instance"}) int systemInstance;
    @Property(aliases={"Industry_Group"}) int industryGroup;
    @Property(aliases={"Reserved_Iso_Self_Configurable"}) int reservedIsoSelfConfigurable;
    
    public IsoAddressClaim()
    {
        super(MethodHandles.lookup(), 60928);
    }

    public int getUniqueNumber()
    {
        return uniqueNumber;
    }

    public void setUniqueNumber(int uniqueNumber)
    {
        this.uniqueNumber = uniqueNumber;
    }

    public int getManufacturerCode()
    {
        return manufacturerCode;
    }

    public void setManufacturerCode(int manufacturerCode)
    {
        this.manufacturerCode = manufacturerCode;
    }

    public int getDeviceInstanceLower()
    {
        return deviceInstanceLower;
    }

    public void setDeviceInstanceLower(int deviceInstanceLower)
    {
        this.deviceInstanceLower = deviceInstanceLower;
    }

    public int getDeviceInstanceUpper()
    {
        return deviceInstanceUpper;
    }

    public void setDeviceInstanceUpper(int deviceInstanceUpper)
    {
        this.deviceInstanceUpper = deviceInstanceUpper;
    }

    public int getDeviceFunction()
    {
        return deviceFunction;
    }

    public void setDeviceFunction(int deviceFunction)
    {
        this.deviceFunction = deviceFunction;
    }

    public int getReserved()
    {
        return reserved;
    }

    public void setReserved(int reserved)
    {
        this.reserved = reserved;
    }

    public int getDeviceClass()
    {
        return deviceClass;
    }

    public void setDeviceClass(int deviceClass)
    {
        this.deviceClass = deviceClass;
    }

    public int getSystemInstance()
    {
        return systemInstance;
    }

    public void setSystemInstance(int systemInstance)
    {
        this.systemInstance = systemInstance;
    }

    public int getIndustryGroup()
    {
        return industryGroup;
    }

    public void setIndustryGroup(int industryGroup)
    {
        this.industryGroup = industryGroup;
    }

    public int getReservedIsoSelfConfigurable()
    {
        return reservedIsoSelfConfigurable;
    }

    public void setReservedIsoSelfConfigurable(int reservedIsoSelfConfigurable)
    {
        this.reservedIsoSelfConfigurable = reservedIsoSelfConfigurable;
    }
    
}
