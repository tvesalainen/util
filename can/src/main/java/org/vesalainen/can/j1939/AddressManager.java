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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.Frame;
import org.vesalainen.can.PgnHandler;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.nio.ReadBuffer;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AddressManager extends JavaLogging implements PgnHandler
{
    private static final int ALL = 255;
    private static final int NULL = 254;
    private static final int REQUEST_MESSAGE = 59904;
    private static final int ADDRESS_CLAIM = 60928;
    private static final int PRODUCT_INFO = 126996;
    
    private Map<Long,Byte> nameMap = new HashMap<>();
    private Map<Byte,Long> saMap = new HashMap<>();
    private Map<Long,Name> names = new ConcurrentHashMap<>();
    private byte[] buf = new byte[8];
    private AbstractCanService service;
    private CachedScheduledThreadPool executor;

    private int canId;
    private int pf;
    private int ps;
    private int sa;
    private int ownSA = NULL;
    private byte[] ownName;
    private IsoRequest isoRequest = new IsoRequest();
    private byte[] requestForAddressClaimed = new byte[3];
    private byte[] requestForProductInformation = new byte[3];
    private long ownNumericName;
    private ProductInformation ownProductInformation;
    private byte[] ownProductInformationBuf = new byte[134];
    private ScheduledFuture<?> productInformationPollFuture;
    private List<Consumer<Name>> nameObservers = new ArrayList<>();

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
    public int[] pgnsToHandle()
    {
        return new int[]{REQUEST_MESSAGE, ADDRESS_CLAIM, PRODUCT_INFO};
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
    }

    @Override
    public void start()
    {
        executor.submit(this::requestAddresses);
    }

    private synchronized void startPollingProductInformation()
    {
        if (productInformationPollFuture == null)
        {
            config("startPollingProductInformation");
            productInformationPollFuture = executor.scheduleAtFixedRate(this::pollProductInformation, 1, 1, TimeUnit.SECONDS);
        }
    }
    private synchronized void stopPollingProductInformation()
    {
        productInformationPollFuture.cancel(true);
        productInformationPollFuture = null;
        config("stopPollingProductInformation");
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
        this.pf = PGN.pduFormat(canId);
        this.ps = PGN.pduSpecific(canId);
        this.sa = PGN.sourceAddress(canId);
        switch (PGN.pgn(canId))
        {
            case REQUEST_MESSAGE:
                handleIsoRequest(time, canId, data);
                break;
            case ADDRESS_CLAIM:
                handleAddressClaimed(time, canId, data);
                break;
            case PRODUCT_INFO:
                handleProductInformation(time, canId, data);
                break;
            default:
                throw new UnsupportedOperationException(PGN.pgn(canId)+" not supported");
        }
    }

    private void handleIsoRequest(long time, int canId, ReadBuffer  data)
    {
        data.get(buf);
        isoRequest.write(buf);
        int pgn = isoRequest.getPgnBeingRequested();
        fine("IsoRequest pgn=%d %x->%x", pgn, sa, ps);
        if (ps == ownSA || ps == ALL)
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

    private void handleAddressClaimed(long time, int canId, ReadBuffer  data)
    {
        fine("AddressClaimed %x name=%x", sa, data);
        if (sa < NULL)
        {
            data.get(buf);
            long name = DataUtil.asLong(buf);
            if (sa == ownSA)    // conflict
            {
                Name n = new Name(buf);
                if (Long.compareUnsigned(ownNumericName, n.getName()) < 0)
                {
                    fine("minor name claimed %x", sa);
                    claimOwnAddress();
                }
                else
                {
                    ownSA = NULL;
                    warning("conflicting SA=%x", sa);
                }
            }
            Byte oldAddr = nameMap.get(name);
            if (oldAddr == null)
            {
                fine("SA %x Name %x", sa, name);
                nameMap.put(name, (byte)sa);
                saMap.put((byte)sa, name);
                names.put(name, new Name(buf));
                startPollingProductInformation();
            }
            else
            {
                if (oldAddr != (byte)sa)
                {
                    warning("SA %x %x -> %x %x", (int)(oldAddr&0xff), saMap.get(oldAddr), sa, name);
                    nameMap.put(name, (byte)sa);
                    saMap.put((byte)sa, name);
                    saMap.remove(oldAddr);
                    Name n = names.get(name);
                    nameObservers.forEach((o)->o.accept(n));
                }
                if (ownSA == NULL)
                {
                    claimOwnAddress();
                }
            }
        }
    }
    private void pollProductInformation()
    {
        IntReference count = new IntReference(0);
        names.forEach((d,n)->
        {
            if (!n.ready)
            {
                requestProductInformation(n.getSource());
                count.add(1);
            }
        });
        if (count.getValue() == 0)
        {
            stopPollingProductInformation();
        }
    }
    private void handleProductInformation(long time, int canId, ReadBuffer data)
    {
        Long n = saMap.get((byte)sa);
        if (n != null)
        {
            Name name = names.get(n);
            fine("got info %x from %x", name.name, sa);
            name.frame(time, canId, data);
        }
    }

    private void requestAddresses()
    {
        int canId = PGN.canId(2, REQUEST_MESSAGE, ALL, NULL);
        fine("requestAddresses from all");
        try
        {
            service.send(canId, requestForAddressClaimed);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }
    
    private void claimOwnAddress()
    {
        fine("claiming own address");
        if (ownSA == NULL)
        {
            for (int ii=0;ii<NULL;ii++)
            {
                if (!saMap.containsKey((byte)ii))
                {
                    ownSA = ii;
                    config("claimed own address %d", ownSA);
                    break;
                }
            }
        }
        int canId = PGN.canId(2, ADDRESS_CLAIM, ALL, ownSA);
        fine("sendAddressClaimed to all");
        try
        {
            service.send(canId, ownName);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void requestProductInformation(int da)
    {
        int canId = PGN.canId(2, REQUEST_MESSAGE, da, ownSA);
        fine("requestProductInformation to %x %s", da, PGN.toString(canId));
        try
        {
            service.send(canId, requestForProductInformation);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    private void sendProductInformation(int da)
    {
        int canId = PGN.canId(2, PRODUCT_INFO, ownSA);
        fine("sendProductInformation to %x", da);
        try
        {
            ownProductInformation.write(ownProductInformationBuf);
            service.send(canId, ownProductInformationBuf);
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
    
    public class Name extends AbstractDynamicMBean implements Frame, SignalCompiler<Object>
    {
        private boolean ready;
        private ObjectName objectName;
        private IsoAddressClaim isoAddressClaim = new IsoAddressClaim();
        private final long name;
        private ProductInformation productInformation = new ProductInformation();
        private byte[] info = new byte[134];

        public Name(byte[] buf)
        {
            super("Name");
            isoAddressClaim.read(buf);
            this.name = DataUtil.asLong(buf);
            addAttributes(this);
            try
            {
                this.objectName = ObjectName.getInstance(Name.class.getName()+":Model="+isoAddressClaim.getDeviceFunction()+",Id="+isoAddressClaim.getUniqueNumber());
                register();
                nameObservers.forEach((o)->o.accept(this));
            }
            catch (MalformedObjectNameException | NullPointerException ex)
            {
                log(Level.SEVERE, ex, "");
            }
        }

        @Override
        public void frame(long time, int canId, ReadBuffer data)
        {
            try
            {
                if (sa != getSource())
                {
                    throw new IllegalArgumentException();
                }
                data.get(info);
                productInformation.read(info);
                if (!ready)
                {
                    try
                    {
                        unregister();
                        this.objectName = ObjectName.getInstance(Name.class.getName()+":Type="+productInformation.getManufacturerSModelId().replace(' ', '_')+",Model="+isoAddressClaim.getDeviceFunction()+",Id="+isoAddressClaim.getUniqueNumber());
                    }
                    catch (MalformedObjectNameException | NullPointerException ex)
                    {
                        log(Level.SEVERE, ex, "");
                    }
                    register();
                    nameObservers.forEach((o)->o.accept(this));
                    ready = true;
                }
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "%s", ex.getMessage());
            }
        }

        @Override
        protected ObjectName createObjectName() throws MalformedObjectNameException
        {
            return objectName;
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

        public int getUniqueNumber()
        {
            return isoAddressClaim.getUniqueNumber();
        }

        public int getManufacturerCode()
        {
            return isoAddressClaim.getManufacturerCode();
        }

        public int getDeviceInstanceLower()
        {
            return isoAddressClaim.getDeviceInstanceLower();
        }

        public int getDeviceInstanceUpper()
        {
            return isoAddressClaim.getDeviceInstanceUpper();
        }

        public int getDeviceFunction()
        {
            return isoAddressClaim.getDeviceFunction();
        }

        public int getReserved()
        {
            return isoAddressClaim.getReserved();
        }

        public int getDeviceClass()
        {
            return isoAddressClaim.getDeviceClass();
        }

        public int getSystemInstance()
        {
            return isoAddressClaim.getSystemInstance();
        }

        public int getIndustryGroup()
        {
            return isoAddressClaim.getIndustryGroup();
        }

        public int getReservedIsoSelfConfigurable()
        {
            return isoAddressClaim.getReservedIsoSelfConfigurable();
        }

        public int getNmea2000DatabaseVersion()
        {
            return productInformation.getNmea2000DatabaseVersion();
        }

        public int getNmeaManufacturerSProductCode()
        {
            return productInformation.getNmeaManufacturerSProductCode();
        }

        public String getManufacturerSModelId()
        {
            return productInformation.getManufacturerSModelId();
        }

        public String getManufacturerSSoftwareVersionCode()
        {
            return productInformation.getManufacturerSSoftwareVersionCode();
        }

        public String getManufacturerSModelVersion()
        {
            return productInformation.getManufacturerSModelVersion();
        }

        public String getManufacturerSModelSerialCode()
        {
            return productInformation.getManufacturerSModelSerialCode();
        }

        public int getLoadEquivalency()
        {
            return productInformation.getLoadEquivalency();
        }

    }
}
