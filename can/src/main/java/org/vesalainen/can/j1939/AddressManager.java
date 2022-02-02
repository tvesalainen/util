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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.ArrayFuncs;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.FastMessage;
import org.vesalainen.can.Frame;
import org.vesalainen.can.PgnHandler;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AddressManager extends JavaLogging implements PgnHandler
{
    private Map<Long,Byte> nameMap = new HashMap<>();
    private Map<Byte,Long> saMap = new HashMap<>();
    private Map<Long,Name> names = new HashMap<>();
    private byte[] data = new byte[8];
    private AbstractCanService service;
    private CachedScheduledThreadPool executor;
    private IntSupplier pgnBeingRequested;

    private int ownUniqueNumber = 123;
    private int ownManufacturerCode;
    private int ownDeviceInstanceLower;
    private int ownDeviceInstanceUpper;
    private int ownDeviceFunction;
    private int ownDeviceClass = 70;
    private int ownSystemInstance;
    private int ownIndustryGroup = 4;
    private int ownAddressCapable = 1;

    private IntSupplier uniqueNumber;
    private IntSupplier manufacturerCode;
    private IntSupplier deviceInstanceLower;
    private IntSupplier deviceInstanceUpper;
    private IntSupplier deviceFunction;
    private IntSupplier deviceClass;
    private IntSupplier systemInstance;
    private IntSupplier industryGroup;
    private IntSupplier addressCapable;
    private int canId;
    private int pf;
    private int ps;
    private int sa;
    private int ownSA = 254;
    private byte[] ownName;
    private MessageClass productInformationClass;
    private long ownNumericName;

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
    public void init(AbstractCanService service, CachedScheduledThreadPool executor)
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
    public void start()
    {
        executor.submit(this::requestAddresses);
    }

    @Override
    public void frame(long time, int canId, int dataLength, long data)
    {
        this.canId = canId;
        finest("%s", PGN.toString(canId));
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
                handleAddressClaimed(data);
                break;
            case 126996:
                handleProductInformation(time, canId, dataLength, data);
                break;
        }
    }

    private void compileAddressClaimed(MessageClass mc)
    {
        ownName = new byte[8];
        /*
        mc.forEach((s)->
        {
            switch (s.getName())
            {
                case "Unique_Number":
                    uniqueNumber = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownUniqueNumber, ownName).run();
                    break;
                case "Manufacturer_Code":
                    manufacturerCode = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownManufacturerCode, ownName).run();
                    break;
                case "Device_Instance_Lower":
                    deviceInstanceLower = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownDeviceInstanceLower, ownName).run();
                    break;
                case "Device_Instance_Upper":
                    deviceInstanceUpper = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownDeviceInstanceUpper, ownName).run();
                    break;
                case "Device_Function":
                    deviceFunction = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownDeviceFunction, ownName).run();
                    break;
                case "Device_Class":
                    deviceClass = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownDeviceClass, ownName).run();
                    break;
                case "System_Instance":
                    systemInstance = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownSystemInstance, ownName).run();
                    break;
                case "Industry_Group":
                    industryGroup = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownIndustryGroup, ownName).run();
                    break;
                case "Reserved_Iso_Self_Configurable":
                    addressCapable = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    ArrayFuncs.getIntWriter(s.getStartBit(), s.getSize(), false, false, ()->ownAddressCapable, ownName).run();
                    break;
                case "Reserved":
                    break;
                default:
                    throw new UnsupportedOperationException(s.getName()+" not supported");
            }
        });
        ownNumericName = ArrayFuncs.getLongSupplier(0, 64, false, false, ownName).getAsLong();
*/
    }

    private void compileRequestForAddressClaimed(MessageClass mc)
    {
        mc.forEach((s)->
        {
            switch (s.getName())
            {
                case "Pgn_Being_Requested":
                    //pgnBeingRequested = ArrayFuncs.getIntSupplier(s.getStartBit(), s.getSize(), false, false, data);
                    break;
                default:
                    throw new UnsupportedOperationException(s.getName()+" not supported");
            }
        });
    }

    private void handleRequestForAddressClaimed()
    {
        int pgn = pgnBeingRequested.getAsInt();
        info("RequestForAddressClaimed pgn=%d %s", pgn, PGN.toString(canId));
        if (ps == ownSA || ps == 255)
        {
            switch (pgn)
            {
                case 60928:
                    sendAddressClaimed(ownSA);
                    break;
                case 126996:
                    break;
                default:
                    throw new UnsupportedOperationException(pgn+" not supported");
            }
        }
    }

    private void handleAddressClaimed(long data)
    {
        info("AddressClaimed %s", PGN.toString(canId));
        if (sa < 254 && sa == ownSA)    // conflict
        {
            Name n = new Name(data);
            if (Long.compareUnsigned(ownNumericName, n.name) < 0)
            {
                info("minor name claimed %d", sa);
                executor.submit(()->sendAddressClaimed(ownSA));
            }
            else
            {
                ownSA = 254;
                warning("conflicting SA=%d", sa);
                executor.submit(this::claimOwnAddress);
            }
        }
        Byte a = nameMap.get(data);
        if (a == null)
        {
            nameMap.put(data, (byte)sa);
            saMap.put((byte)sa, data);
            names.put(data, new Name(data));
            requestProductInformation(sa);
        }
        else
        {
            if (a != (byte)sa)
            {
                warning("SA %d -> %d", a, sa);
                nameMap.put(data, (byte)sa);
                saMap.put((byte)sa, data);
                saMap.remove(a);
            }
            Name name = names.get(data);
            if (!name.ready)
            {
                requestProductInformation(sa);
            }
        }
    }

    private void handleProductInformation(long time, int canId, int dataLength, long data)
    {
        Long n = saMap.get((byte)sa);
        if (n != null)
        {
            Name name = names.get(n);
            name.frame(time, canId, dataLength, data);
        }
    }

    private void sendAddressClaimed(int sa)
    {
        int canId = PGN.canId(2, 60928, 255, sa);
        info("send %X %s %s", canId, PGN.toString(canId), HexUtil.toString(ownName));
        try
        {
            service.send(canId, ownName);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void requestAddresses()
    {
        int canId = PGN.canId(2, 59904, 255, 254);
        info("send %X %s 00 ee oo", canId, PGN.toString(canId));
        try
        {
            service.send(canId, (byte)0x00, (byte)0xee, (byte)0x00);
            executor.schedule(this::claimOwnAddress, 1, TimeUnit.SECONDS);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
    private void claimOwnAddress()
    {
        info("claiming own address");
        for (int ii=0;ii<254;ii++)
        {
            if (!saMap.containsKey((byte)ii))
            {
                ownSA = ii;
                sendAddressClaimed(ownSA);
                info("claimed %d", ownSA);
                return;
            }
        }
    }
    private void requestProductInformation(int da)
    {
        int canId = PGN.canId(2, 59904, da, ownSA);
        info("send %X %s 14 f0 01", canId, PGN.toString(canId));
        try
        {
            service.send(canId, (byte)0x14, (byte)0xf0, (byte)0x01);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
    public class Name extends AbstractDynamicMBean implements Frame, SignalCompiler<Object>
    {
        private FastMessage fast;
        private int id;
        private int manufacturer;
        private int instanceLower;
        private int instanceUpper;
        private int function;
        private int cls;
        private int system;
        private int industry;
        private int capable;
        private final int hash;
        private int databaseVersion;
        private int productCode;
        private int certificationLevel;
        private int loadEquivalency;
        private String modelId;
        private String softwareVersionCode;
        private String modelVersion;
        private String modelSerialCode;
        private boolean ready;
        private ObjectName objectName;
        private final long name;

        public Name(long name)
        {
            super("Name");
            this.name = name;
            this.id = uniqueNumber.getAsInt();
            this.manufacturer = manufacturerCode.getAsInt();
            this.instanceLower = deviceInstanceLower.getAsInt();
            this.instanceUpper = deviceInstanceUpper.getAsInt();
            this.function = deviceFunction.getAsInt();
            this.cls = deviceClass.getAsInt();
            this.system = systemInstance.getAsInt();
            this.industry = industryGroup.getAsInt();
            this.capable = addressCapable.getAsInt();
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
            this.fast = new FastMessage(executor, productInformationClass, PGN.canId(126996), 134, "");
            fast.addSignals(this);
            addAttributes(this);
        }

        @Override
        public void frame(long time, int canId, int dataLength, long data)
        {
            fast.frame(time, canId, dataLength, data);
            if (!ready && modelId != null)
            {
                try
                {
                    this.objectName = ObjectName.getInstance(Name.class.getName()+":Type="+modelId.replace(' ', '_')+",Model="+function+",Id="+id);
                }
                catch (MalformedObjectNameException | NullPointerException ex)
                {
                    log(Level.SEVERE, null, ex);
                }
                register();
                ready = true;
            }
        }

        //@Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier intSupplier)
        {
            switch (sc.getName())
            {
                case "Nmea_2000_Database_Version":
                return ()->
                {
                    databaseVersion = intSupplier.getAsInt();
                };
                case "Nmea_Manufacturer_S_Product_Code":
                return ()->
                {
                    productCode = intSupplier.getAsInt();
                };
                case "Nmea_2000_Certification_Level":
                return ()->
                {
                    certificationLevel = intSupplier.getAsInt();
                };
                case "Load_Equivalency":
                return ()->
                {
                    loadEquivalency = intSupplier.getAsInt();
                };
                default:
                    throw new UnsupportedOperationException(sc.getName()+" not supported");
            }
        }

        //@Override
        public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss)
        {
            switch (sc.getName())
            {
                case "Manufacturer_S_Model_Id":
                return ()->
                {
                    modelId = ss.get();
                };
                case "Manufacturer_S_Software_Version_Code":
                return ()->
                {
                    softwareVersionCode = ss.get();
                };
                case "Manufacturer_S_Model_Version":
                return ()->
                {
                    modelVersion = ss.get();
                };
                case "Manufacturer_S_Model_Serial_Code":
                return ()->
                {
                    modelSerialCode = ss.get();
                };
                default:
                    throw new UnsupportedOperationException(sc.getName()+" not supported");
            }
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
            return "Name{" + "id=" + id + ", manufacturer=" + manufacturer + ", instance=" + instanceUpper + "." + instanceLower + ", function=" + function + ", cls=" + cls + ", system=" + system + ", industry=" + industry + "capable=" + capable +'}';
        }

        @Override
        protected ObjectName createObjectName() throws MalformedObjectNameException
        {
            return objectName;
        }

        public int getSource()
        {
            Byte sa = nameMap.get(name);
            if (sa != null)
            {
                return sa & 0xff;
            }
            else
            {
                return -1;
            }
        }
        public long getName()
        {
            return name;
        }

        public int getId()
        {
            return id;
        }

        public int getManufacturer()
        {
            return manufacturer;
        }

        public int getInstanceLower()
        {
            return instanceLower;
        }

        public int getInstanceUpper()
        {
            return instanceUpper;
        }

        public int getFunction()
        {
            return function;
        }

        public int getCls()
        {
            return cls;
        }

        public int getSystem()
        {
            return system;
        }

        public int getIndustry()
        {
            return industry;
        }

        public int getCapable()
        {
            return capable;
        }

        public int getDatabaseVersion()
        {
            return databaseVersion;
        }

        public int getProductCode()
        {
            return productCode;
        }

        public int getCertificationLevel()
        {
            return certificationLevel;
        }

        public int getLoadEquivalency()
        {
            return loadEquivalency;
        }

        public String getModelId()
        {
            return modelId;
        }

        public String getSoftwareVersionCode()
        {
            return softwareVersionCode;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public String getModelSerialCode()
        {
            return modelSerialCode;
        }

    }
}
