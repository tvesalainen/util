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
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.IntReference;
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
    private Map<Long,Name> names = new ConcurrentHashMap<>();
    private byte[] buf = new byte[8];
    private AbstractCanService service;
    private CachedScheduledThreadPool executor;

    private int canId;
    private int pf;
    private int ps;
    private int sa;
    private int ownSA = 254;
    private byte[] ownName;
    private IsoRequest isoRequest = new IsoRequest(60928);
    private byte[] requestForAddressClaimed = new byte[3];
    private byte[] requestForProductInformation = new byte[3];
    private long ownNumericName;
    private byte[] ownProductInformation;
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
        
        ProductInformation ownInfo = new ProductInformation();
        ownInfo.setManufacturerSModelId("Java CAN library");
        this.ownProductInformation = new byte[134];
        ownInfo.write(ownProductInformation);
        
        IsoRequest rfac = new IsoRequest(60928);
        rfac.write(requestForAddressClaimed);
        IsoRequest rfpi = new IsoRequest(126996);
        rfpi.write(requestForProductInformation);
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
    }

    @Override
    public void start()
    {
        executor.submit(this::requestAddresses);
    }

    private void startPollingProductInformation()
    {
        if (productInformationPollFuture == null)
        {
            config("startPollingProductInformation");
            productInformationPollFuture = executor.scheduleAtFixedRate(this::pollProductInformation, 1, 1, TimeUnit.SECONDS);
        }
    }
    private void stopPollingProductInformation()
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
            case 59904:
                handleIsoRequest(time, canId, data);
                break;
            case 60928:
                handleAddressClaimed(time, canId, data);
                break;
            case 126996:
                handleProductInformation(time, canId, data);
                break;
        }
    }

    private void handleIsoRequest(long time, int canId, ReadBuffer  data)
    {
        data.get(buf);
        isoRequest.write(buf);
        int pgn = isoRequest.getPgnBeingRequested();
        info("RequestForAddressClaimed pgn=%d %x->%x", pgn, sa, ps);
        if (ps == ownSA || ps == 255)
        {
            switch (pgn)
            {
                case 60928:
                    sendAddressClaimed();
                    break;
                case 126996:
                    sendProductInformation(ps);
                    break;
                default:
                    throw new UnsupportedOperationException(pgn+" not supported");
            }
        }
    }

    private void handleAddressClaimed(long time, int canId, ReadBuffer  data)
    {
        data.get(buf);
        long name = DataUtil.asLong(buf);
        fine("AddressClaimed %x name=%x", sa, data);
        if (sa < 254)
        {
            if (sa == ownSA)    // conflict
            {
                Name n = new Name(buf);
                if (Long.compareUnsigned(ownNumericName, n.getName()) < 0)
                {
                    info("minor name claimed %x", sa);
                    executor.submit(()->sendAddressClaimed());
                }
                else
                {
                    ownSA = 254;
                    warning("conflicting SA=%x", sa);
                    executor.submit(this::claimOwnAddress);
                }
            }
            Byte oldAddr = nameMap.get(name);
            if (oldAddr == null)
            {
                info("SA %x Name %x", sa, name);
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
            info("got info %x from %x", name.name, sa);
            name.frame(time, canId, data);
        }
    }

    private void sendAddressClaimed()
    {
        int canId = PGN.canId(2, 60928, 255, ownSA);
        info("sendAddressClaimed to all");
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
        info("requestAddresses from all");
        try
        {
            service.send(canId, requestForAddressClaimed);
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
                sendAddressClaimed();
                info("claimed %d", ownSA);
                return;
            }
        }
    }
    private void requestProductInformation(int da)
    {
        int canId = PGN.canId(2, 59904, da, ownSA);
        info("requestProductInformation to %x", da);
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
        int canId = PGN.canId(2, 126996, da, ownSA);
        fine("sendProductInformation to %x", da);
        try
        {
            service.send(canId, ownProductInformation);
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
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
                    if (ready == false)
                    {
                        info("");
                    }
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
                    info("%b", ready);
                    ready = true;
                    info("name %x ready", name);
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
