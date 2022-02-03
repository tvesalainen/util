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
import org.vesalainen.can.n2k.FastWriter;
import org.vesalainen.can.n2k.FastReader;
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

    private int canId;
    private int pf;
    private int ps;
    private int sa;
    private int ownSA = 254;
    private byte[] ownName;
    private IsoRequest isoRequest = new IsoRequest(60928);
    private IsoRequest requestForAddressClaimed = new IsoRequest(60928);
    private IsoRequest requestForProductInformation = new IsoRequest(126996);
    private long ownNumericName;
    private long[] ownProductInformation;

    public AddressManager()
    {
        super(AddressManager.class);
        
        IsoAddressClaim ownClaim = new IsoAddressClaim();
        ownClaim.setIndustryGroup(4);
        ownClaim.setUniqueNumber(123456);
        ownClaim.setReservedIsoSelfConfigurable(1);
        ownName = new byte[8];
        ownClaim.write(ownName);
        ownNumericName = DataUtil.asLong(ownName);
        
        ProductInformation ownInfo = new ProductInformation();
        ownInfo.setManufacturerSModelId("Java CAN library");
        List<Long> list = new ArrayList<>();
        FastWriter writer = new FastWriter();
        byte[] b = new byte[134];
        ownInfo.write(b);
        writer.write(134, b, list::add);
        int size = list.size();
        ownProductInformation = new long[size];
        for (int ii=0;ii<size;ii++)
        {
            ownProductInformation[ii] = list.get(ii);
        }
        
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

    private void handleRequestForAddressClaimed()
    {
        isoRequest.write(data);
        int pgn = isoRequest.getPgnBeingRequested();
        info("RequestForAddressClaimed pgn=%d %s", pgn, PGN.toString(canId));
        if (ps == ownSA || ps == 255)
        {
            switch (pgn)
            {
                case 60928:
                    sendAddressClaimed(ownSA);
                    break;
                case 126996:
                    sendProductInformation();
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

    private void sendProductInformation()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public class Name extends AbstractDynamicMBean implements Frame, SignalCompiler<Object>
    {
        private boolean ready;
        private ObjectName objectName;
        private final long name;
        private IsoAddressClaim isoAddressClaim = new IsoAddressClaim();
        private ProductInformation productInformation = new ProductInformation();
        private byte[] info = new byte[134];
        private FastReader fastReader = new FastReader("ProductInformation", info);

        public Name(long name)
        {
            super("Name");
            this.name = name;
            byte[] b = new byte[8];
            isoAddressClaim.read(b);
            addAttributes(this);
        }

        @Override
        public void frame(long time, int canId, int dataLength, long data)
        {
            if (fastReader.update(time, canId, dataLength, data))
            {
                productInformation.read(info);
                if (!ready)
                {
                    try
                    {
                        this.objectName = ObjectName.getInstance(Name.class.getName()+":Type="+productInformation.getManufacturerSModelId().replace(' ', '_')+",Model="+isoAddressClaim.getDeviceFunction()+",Id="+isoAddressClaim.getUniqueNumber());
                    }
                    catch (MalformedObjectNameException | NullPointerException ex)
                    {
                        log(Level.SEVERE, null, ex);
                    }
                    register();
                    ready = true;
                }
            }
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

    }
}
