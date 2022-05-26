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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import static java.util.logging.Level.SEVERE;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.PgnHandler;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.nio.ReadBuffer;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AddressManager extends JavaLogging implements PgnHandler
{
    public static final byte ALL = (byte) 255;
    public static final byte NULL = (byte) 254;

    private static final int UNKNOWN = 1;
    private static final int NAME = 2;
    private static final int INFO = 4;
    private static final int REQUEST_MESSAGE = 59904;
    private static final int ADDRESS_CLAIM = 60928;
    private static final int PRODUCT_INFO = 126996;
    private static final int PRIORITY = 2;
    
    private Map<Long,Name> nameMap = new HashMap<>();
    private Map<Byte,Name> saMap = new HashMap<>();
    private byte[] buf = new byte[8];
    private AbstractCanService service;
    private CachedScheduledThreadPool executor;

    private int canId;
    private int pf;
    private byte ps;
    private byte sa;
    private byte ownSA = NULL;
    private byte[] ownName;
    private IsoRequest isoRequest = new IsoRequest();
    private byte[] requestForAddressClaimed = new byte[3];
    private byte[] requestForProductInformation = new byte[3];
    private long ownNumericName;
    private ProductInformation ownProductInformation;
    private byte[] ownProductInformationBuf = new byte[134];
    private Poller<Byte> namePoller;
    private Poller<Byte> infoPoller;
    private List<Consumer<Name>> nameObservers = new ArrayList<>();
    private int pgn;

    public AddressManager()
    {
        super(AddressManager.class);
        
        IsoAddressClaim ownClaim = new IsoAddressClaim();
        ownClaim.setIndustryGroup(4);
        ownClaim.setUniqueNumber((int)System.currentTimeMillis());
        ownClaim.setReservedIsoSelfConfigurable(1);
        ownName = new byte[8];
        ownClaim.write(ownName);
        ownNumericName = DataUtil.asLong(ownName);
        
        ownProductInformation = new ProductInformation();
        ownProductInformation.setManufacturerSModelId("Java CAN library");
        
        isoRequest.setPgnBeingRequested(ADDRESS_CLAIM);
        isoRequest.write(requestForAddressClaimed);
        isoRequest.setPgnBeingRequested(PRODUCT_INFO);
        isoRequest.write(requestForProductInformation);
    }

    @Override
    public void init(AbstractCanService service, CachedScheduledThreadPool executor)
    {
        this.service = service;
        this.executor = executor;
        DBC.addJ1939();
        namePoller = new Poller<>(executor, this::requestAddress, 100, 1, TimeUnit.SECONDS);
        infoPoller = new Poller<>(executor, this::requestProductInformation, 100, 1, TimeUnit.SECONDS);
    }

    @Override
    public void start()
    {
    }

    public void addNameObserver(Consumer<Name> observer)
    {
        nameObservers.add(observer);
    }
    public void removeNameObserver(Consumer<Name> observer)
    {
        nameObservers.remove(observer);
    }

    @Override
    public void frame(long time, int canId, ReadBuffer  data)
    {
        this.canId = canId;
        finest("%s", PGN.toString(canId));
        this.pgn = PGN.pgn(canId);
        this.pf = (byte) PGN.pduFormat(canId);
        this.ps = (byte) PGN.pduSpecific(canId);
        this.sa = (byte) PGN.sourceAddress(canId);
        Name name = saMap.get(sa);
        if (name == null)
        {
            name = new Name(sa);
            saMap.put(sa, name);
        }
        switch (PGN.pgn(canId))
        {
            case REQUEST_MESSAGE:
                handleIsoRequest(time, canId, data);
                break;
            case ADDRESS_CLAIM:
                handleAddressClaimed(name, data);
                break;
            case PRODUCT_INFO:
                name.setInfo(data);
                break;
        }
        name.update();
    }

    public byte getOwnSA()
    {
        ensureOwnAddress();
        return ownSA;
    }

    private void handleIsoRequest(long time, int canId, ReadBuffer  data)
    {
        data.get(buf);
        isoRequest.write(buf);
        int pgn = isoRequest.getPgnBeingRequested();
        fine("IsoRequest pgn=%d %x->%x", pgn, sa, ps);
        if (isPeer(ownSA) && (ps == ownSA || ps == ALL))
        {
            switch (pgn)
            {
                case ADDRESS_CLAIM:
                    claimOwnAddress();
                    break;
                case PRODUCT_INFO:
                    sendProductInformation(ps);
                    break;
                default:
                    throw new UnsupportedOperationException(pgn+" not supported");
            }
        }
    }

    private void handleAddressClaimed(Name name, ReadBuffer  data)
    {
        fine("AddressClaimed %x", sa);
        if (isPeer(sa))
        {
            data.get(buf);
            name.setName(buf);
            long n = name.getName();
            if (sa == ownSA)    // conflict
            {
                if (Long.compareUnsigned(ownNumericName, n) < 0)
                {
                    warning("minor name claimed %x", sa);
                    claimOwnAddress();
                    return;
                }
                else
                {
                    ownSA = NULL;
                    warning("conflicting SA=%x", sa);
                }
            }
        }
    }
    private void requestAddress(byte from)
    {
        int canId = PGN.canId(2, REQUEST_MESSAGE, from, ownSA);
        fine("requestAddress from %x", from);
        try
        {
            service.sendRaw(canId, requestForAddressClaimed);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
    private void ensureOwnAddress()
    {
        fine("claiming own address");
        if (isNull(ownSA))
        {
            for (int ii=0;ii<254;ii++)
            {
                if (!saMap.containsKey((byte)ii))
                {
                    ownSA = (byte) ii;
                    config("claimed own address %d", ownSA);
                    break;
                }
            }
            if (ownSA != NULL)
            {
                claimOwnAddress();
            }
            else
            {
                warning("couldn't get own address");
            }
        }
    }
    private void claimOwnAddress()
    {
        if (isPeer(ownSA))
        {
            int canId = PGN.canId(2, ADDRESS_CLAIM, ALL, ownSA);
            fine("sendAddressClaimed to all");
            try
            {
                service.sendRaw(canId, ownName);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            warning("can't claim own address because there's none");
        }
    }
    private void requestProductInformation(byte da)
    {
        int canId = PGN.canId(2, REQUEST_MESSAGE, da, ownSA);
        fine("requestProductInformation to %x %s", da, PGN.toString(canId));
        try
        {
            service.sendRaw(canId, requestForProductInformation);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    private void sendProductInformation(byte da)
    {
        fine("sendProductInformation to %x", da);
        try
        {
            ownProductInformation.write(ownProductInformationBuf);
            service.sendPgn(PRODUCT_INFO, da, PRIORITY, ownProductInformationBuf);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    /**
     * Returns ProductInformation for this product. Set attributes that are
     * visible at n2k network.
     * @return 
     */
    public ProductInformation getOwnProductInformation()
    {
        return ownProductInformation;
    }
    
    public static boolean isNull(int addr)
    {
        return addr == NULL;
    }
    public static boolean isAll(int addr)
    {
        return addr == ALL;
    }
    public static boolean isPeer(int addr)
    {
        return addr != ALL && addr != NULL;
    }
    public class Name extends AbstractDynamicMBean
    {
        private int state = UNKNOWN;
        private int regState = 0;
        private byte sa;
        private IsoAddressClaim isoAddress;
        private long name;
        private ProductInformation info;

        public Name(byte sa)
        {
            super("Name");
            this.sa = sa;
            addAttributes(this);
        }
        public void setName(byte[] buf)
        {
            if ((state & NAME) == 0)
            {
                isoAddress = new IsoAddressClaim();
                isoAddress.read(buf);
                long n = DataUtil.asLong(buf);
                if (n != name)
                {
                    Name on = nameMap.get(name);
                    if (on != null)
                    {
                        on.reset();
                    }
                    name = n;
                    info = null;
                    namePoller.disable(sa);
                    nameMap.put(name, this);
                    state |= NAME;
                }
            }
        }

        public void setInfo(ReadBuffer data)
        {
            if ((state & INFO) == 0)
            {
                byte[] buf = new byte[134];
                info = new ProductInformation();
                try
                {
                    if (sa != getSource())
                    {
                        throw new IllegalArgumentException();
                    }
                    data.get(buf);
                    info.read(buf);
                    nameObservers.forEach((o)->o.accept(this));
                    infoPoller.disable(sa);
                    state |= INFO;
                }
                catch (Throwable ex)
                {
                    log(SEVERE, ex, "%s", ex.getMessage());
                }
            }
        }

        public void update()
        {
            if (isoAddress == null)
            {
                namePoller.enable(sa);
            }
            if (info == null)
            {
                infoPoller.enable(sa);
            }
            if (state != regState)
            {
                register();
                regState = state;
            }
        }
        private void reset()
        {
            isoAddress = null;
            info = null;
            state = UNKNOWN;
            if (state != regState)
            {
                register();
                regState = state;
            }
        }

        @Override
        protected ObjectName createObjectName() throws MalformedObjectNameException
        {
            if (info != null)
            {
                if (isoAddress != null)
                {
                    return ObjectName.getInstance(Name.class.getName()+":Src="+sa+"-"+info.getManufacturerSModelId().replace(' ', '_')+",Model="+isoAddress.getDeviceFunction()+",Id="+isoAddress.getUniqueNumber());
                }
                else
                {
                    return ObjectName.getInstance(Name.class.getName()+":Src="+sa+"-"+info.getManufacturerSModelId().replace(' ', '_'));
                }
            }
            if (isoAddress != null)
            {
                return ObjectName.getInstance(Name.class.getName()+":Src="+sa+"-"+isoAddress.getDeviceFunction()+",Id="+isoAddress.getUniqueNumber());
            }
            return ObjectName.getInstance(Name.class.getName()+":Src="+sa);
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 37 * hash + (int) (this.name ^ (this.name >>> 32));
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
            if (this.name != other.name)
            {
                return false;
            }
            return true;
        }

        public byte getSource()
        {
            return sa;
        }
        public long getName()
        {
            return name;
        }

        public int getUniqueNumber()
        {
            return isoAddress.getUniqueNumber();
        }

        public int getManufacturerCode()
        {
            return isoAddress.getManufacturerCode();
        }

        public int getDeviceInstanceLower()
        {
            return isoAddress.getDeviceInstanceLower();
        }

        public int getDeviceInstanceUpper()
        {
            return isoAddress.getDeviceInstanceUpper();
        }

        public int getDeviceFunction()
        {
            return isoAddress.getDeviceFunction();
        }

        public int getReserved()
        {
            return isoAddress.getReserved();
        }

        public int getDeviceClass()
        {
            return isoAddress.getDeviceClass();
        }

        public int getSystemInstance()
        {
            return isoAddress.getSystemInstance();
        }

        public int getIndustryGroup()
        {
            return isoAddress.getIndustryGroup();
        }

        public int getReservedIsoSelfConfigurable()
        {
            return isoAddress.getReservedIsoSelfConfigurable();
        }

        public int getNmea2000DatabaseVersion()
        {
            return info.getNmea2000DatabaseVersion();
        }

        public int getNmeaManufacturerSProductCode()
        {
            return info.getNmeaManufacturerSProductCode();
        }

        public String getManufacturerSModelId()
        {
            return info.getManufacturerSModelId();
        }

        public String getManufacturerSSoftwareVersionCode()
        {
            return info.getManufacturerSSoftwareVersionCode();
        }

        public String getManufacturerSModelVersion()
        {
            return info.getManufacturerSModelVersion();
        }

        public String getManufacturerSModelSerialCode()
        {
            return info.getManufacturerSModelSerialCode();
        }

        public int getLoadEquivalency()
        {
            return info.getLoadEquivalency();
        }

    }
}
