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
package org.vesalainen.can.socketcand;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.xml.SimpleXMLParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SocketCandInfo extends JavaLogging
{
    
    private InetSocketAddress address;
    private final Set<String> busses = new HashSet<>();

    public SocketCandInfo(SimpleXMLParser.Element root)
    {
        super(SocketCandInfo.class);
        root.forEachChild((e) ->
        {
            switch (e.getTag())
            {
                case "URL":
                    {
                        try
                        {
                            URI uri = new URI(e.getText());
                            address = new InetSocketAddress(uri.getHost(), uri.getPort());
                        }
                        catch (URISyntaxException ex)
                        {
                            log(Level.SEVERE, ex, "%s", ex.getMessage());
                        }
                    }
                    break;
                case "Bus":
                    busses.add(e.getAttributeValue("name"));
                    break;
            }
        });
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    public Set<String> getBusses()
    {
        return busses;
    }
    
}
