/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.modbus;

import static org.vesalainen.modbus.DataType.*;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Register
{
    private String serviceName;
    private String description;
    private int address;
    private DataType type;
    private int words;
    private float scaleFactor;
    private String range;
    private String path;
    private boolean writable;
    private String unit;
    private String remarks;
    
    public Register(String... f)
    {
        this.serviceName = f[0];
        this.description = f[1];
        this.address = Integer.parseInt(f[2]);
        type(f[3]);
        this.scaleFactor = Float.parseFloat(f[4].isEmpty()?"1":f[4]);
        this.range = f[5];
        this.path = f[6];
        this.writable = "yes".equals(f[7]);
        if (f.length > 8)
        {
            this.unit = f[8];
            if (f.length > 9)
            {
                this.remarks = f[9];
            }
        }
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getDescription()
    {
        return description;
    }

    public int getAddress()
    {
        return address;
    }

    public DataType getType()
    {
        return type;
    }

    public int getWords()
    {
        return words;
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public String getRange()
    {
        return range;
    }

    public String getPath()
    {
        return path;
    }

    public boolean isWritable()
    {
        return writable;
    }

    public String getUnit()
    {
        return unit;
    }

    public String getRemarks()
    {
        return remarks;
    }

    private void type(String typeString)
    {
        switch (typeString)
        {
            case "uint16":
                type = USHORT;
                words = type.getWords();
                break;
            case "int16":
                type = SHORT;
                words = type.getWords();
                break;
            case "uint32":
                type = UINT;
                words = type.getWords();
                break;
            case "int32":
                type = INT;
                words = type.getWords();
                break;
            default:
                if (typeString.toLowerCase().startsWith("string["))
                {
                    type = STRING;
                    words = Integer.parseInt(typeString.substring(7, typeString.indexOf(']')));
                }
                else
                {
                    throw new UnsupportedOperationException(type+" not supported");
                }
                break;
        }
    }

}
