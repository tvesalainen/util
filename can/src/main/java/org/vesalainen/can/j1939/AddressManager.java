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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.IntSupplier;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.ArrayFuncs;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.FastMessage;
import org.vesalainen.can.Frame;
import org.vesalainen.can.PgnHandler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AddressManager extends JavaLogging implements PgnHandler
{
    private Map<Name,Byte> nameMap = new HashMap<>();
    private Map<Byte,Name> saMap = new HashMap<>();
    private byte[] data = new byte[8];
    private AbstractCanService service;
    private ExecutorService executor;
    private IntSupplier pgnBeingRequested;
    private IntSupplier uniqueNumber;
    private IntSupplier manufacturerCode;
    private IntSupplier deviceInstanceLower;
    private IntSupplier deviceInstanceUpper;
    private IntSupplier deviceFunction;
    private IntSupplier deviceClass;
    private IntSupplier systemInstance;
    private IntSupplier industryGroup;
    private int pf;
    private int ps;
    private int sa;
    private MessageClass productInformationClass;

    public AddressManager()
    {
        super(AddressManager.class);
    }

    @Override
    public int[] pgnsToHandle()
    {
        return new int[]{59904, 60928, 126996};
    }

    @Override
    public void init(AbstractCanService service, ExecutorService executor)
    {
        this.service = service;
        this.executor = executor;
    }

    @Override
    public void init(int pgn, MessageClass mc)
    {
        switch (pgn)
        {
            case 59904:
                compileRequestForAddressClaimed(mc);
                break;
            case 60928:
                compileAddressClaimed(mc);
                break;
            case 126996:
                this.productInformationClass = mc;
                break;
        }
    }

    @Override
    public void frame(long time, int canId, int dataLength, long data)
    {
        DataUtil.fromLong(data, this.data, 0, dataLength);
        this.pf = PGN.pduFormat(canId);
        this.ps = PGN.pduSpecific(canId);
        this.sa = PGN.sourceAddress(canId);
        switch (PGN.pgn(canId))
        {
            case 59904:
                handleRequestForAddressClaimed();
                break;
            case 60928:
                handleAddressClaimed();
                break;
            case 126996:
                handleProductInformation(time, canId, dataLength, data);
                break;
        }
    }

    private void compileAddressClaimed(MessageClass mc)
    {
        mc.forEach((s)->
        {
            switch (s.getName())
            {
                case "Unique_Number":
                    uniqueNumber = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Manufacturer_Code":
                    manufacturerCode = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Device_Instance_Lower":
                    deviceInstanceLower = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Device_Instance_Upper":
                    deviceInstanceUpper = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Device_Function":
                    deviceFunction = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Device_Class":
                    deviceClass = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "System_Instance":
                    systemInstance = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Industry_Group":
                    industryGroup = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                case "Reserved":
                case "Reserved_Iso_Self_Configurable":
                    break;
                default:
                    throw new UnsupportedOperationException(s.getName()+" not supported");
            }
        });
    }

    private void compileRequestForAddressClaimed(MessageClass mc)
    {
        mc.forEach((s)->
        {
            switch (s.getName())
            {
                case "Pgn_Being_Requested":
                    pgnBeingRequested = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                default:
                    throw new UnsupportedOperationException(s.getName()+" not supported");
            }
        });
    }

    private void handleRequestForAddressClaimed()
    {
        int pgn = pgnBeingRequested.getAsInt();
        switch (pgn)
        {
            case 60928:
                break;
            default:
                throw new UnsupportedOperationException(pgn+" not supported");
        }
    }

    private void handleAddressClaimed()
    {
        Name name = new Name();
        info("address claim %s", name);
        Byte a = nameMap.get(name);
        if (a == null)
        {
            nameMap.put(name, (byte)sa);
            saMap.put((byte)sa, name);
        }
        else
        {
            if (a != (byte)sa)
            {
                warning("SA %d -> %d", a, sa);
                nameMap.put(name, (byte)sa);
                saMap.put((byte)sa, name);
                saMap.remove(a);
            }
        }
    }

    private void handleProductInformation(long time, int canId, int dataLength, long data)
    {
        Name name = saMap.get((byte)sa);
        if (name != null)
        {
            name.frame(time, canId, dataLength, data);
        }
    }
    
    private class Name implements Frame
    {
        private FastMessage fast = new FastMessage(executor, productInformationClass, PGN.canId(126996), 134, "");
        private int id;
        private int manufacturer;
        private int instanceLower;
        private int instanceUpper;
        private int function;
        private int cls;
        private int system;
        private int industry;
        private final int hash;

        public Name()
        {
            this.id = uniqueNumber.getAsInt();
            this.manufacturer = manufacturerCode.getAsInt();
            this.instanceLower = deviceInstanceLower.getAsInt();
            this.instanceUpper = deviceInstanceUpper.getAsInt();
            this.function = deviceFunction.getAsInt();
            this.cls = deviceClass.getAsInt();
            this.system = systemInstance.getAsInt();
            this.industry = industryGroup.getAsInt();
            int h = 3;
            h = 73 * h + this.id;
            h = 73 * h + this.manufacturer;
            h = 73 * h + this.instanceLower;
            h = 73 * h + this.instanceUpper;
            h = 73 * h + this.function;
            h = 73 * h + this.cls;
            h = 73 * h + this.system;
            h = 73 * h + this.industry;
            this.hash = h;
        }

        @Override
        public void frame(long time, int canId, int dataLength, long data)
        {
            fast.frame(time, canId, dataLength, data);
        }
        
        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Name other = (Name) obj;
            if (this.id != other.id)
            {
                return false;
            }
            if (this.manufacturer != other.manufacturer)
            {
                return false;
            }
            if (this.instanceLower != other.instanceLower)
            {
                return false;
            }
            if (this.instanceUpper != other.instanceUpper)
            {
                return false;
            }
            if (this.function != other.function)
            {
                return false;
            }
            if (this.cls != other.cls)
            {
                return false;
            }
            if (this.system != other.system)
            {
                return false;
            }
            if (this.industry != other.industry)
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Name{" + "id=" + id + ", manufacturer=" + manufacturer + ", instance=" + instanceUpper + "." + instanceLower + ", function=" + function + ", cls=" + cls + ", system=" + system + ", industry=" + industry + '}';
        }

    }
}
