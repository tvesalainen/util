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

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.io.Printer;
import static org.vesalainen.modbus.DataType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ServiceModbus
{
    private final AbstractModbus modbus;
    private final Service service;
    private final int unitId;

    public ServiceModbus(int unitId, AbstractModbus modbus, Service service)
    {
        this.unitId = unitId;
        this.modbus = modbus;
        this.service = service;
    }
    
    public double getDouble(String path) throws IOException
    {
        Register register = service.getRegister(path);
        Objects.requireNonNull(register, path);
        double v = 0;
        switch (register.getType())
        {
            case SHORT:
                v = modbus.getShort(unitId, register.getAddress());
                break;
            case USHORT:
                v = modbus.getUnsignedShort(unitId, register.getAddress());
                break;
            case INT:
                v = modbus.getInt(unitId, register.getAddress());
                break;
            case UINT:
                v = modbus.getUnsignedInt(unitId, register.getAddress());
                break;
            default:
                throw new IllegalArgumentException(path+" not numeric");
        }
        return v / register.getScaleFactor();
    }   
    public Object getObject(String path) throws IOException
    {
        Register register = service.getRegister(path);
        Objects.requireNonNull(register, path);
        double v = 0;
        switch (register.getType())
        {
            case SHORT:
                v = modbus.getShort(unitId, register.getAddress());
                break;
            case USHORT:
                v = modbus.getUnsignedShort(unitId, register.getAddress());
                break;
            case INT:
                v = modbus.getInt(unitId, register.getAddress());
                break;
            case UINT:
                v = modbus.getUnsignedInt(unitId, register.getAddress());
                break;
            case STRING:
                return modbus.getString(unitId, register.getAddress(), register.getWords());
            default:
                throw new IllegalArgumentException(path+" not numeric");
        }
        return v / register.getScaleFactor();
    }   
    
    public void dump(Appendable out)
    {
        dump(new AppendablePrinter(out));
    }
    public void dump(Printer out)
    {
        service.forEach((path, register)->
        {
            try
            {
                Object value = getObject(register.getPath());
                out.format("%s %s: %s\t%s\n", register.getDescription(), path, value, register.getRemarks());
            }
            catch (IOException ex)
            {
                out.format("%s: %s\n", path, ex.getMessage());
            }
        });
    }
}
