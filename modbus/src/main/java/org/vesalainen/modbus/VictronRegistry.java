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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VictronRegistry
{
    private Map<String,Service> map = new HashMap<>();
    
    public VictronRegistry()
    {
        try {
            URL url = VictronRegistry.class.getResource("/CCGX-Modbus-TCP-register-list-2.70.csv");
            Path path = Paths.get(url.toURI());
            try (BufferedReader br = Files.newBufferedReader(path))
            {
                String hdr1 = br.readLine();
                String hdr2 = br.readLine();
                String line = br.readLine();
                while (line != null)
                {
                    String[] split = line.split("\\,");
                    Register r = new Register(split);
                    String serviceName = r.getServiceName();
                    Service service = map.get(serviceName);
                    if (service == null)
                    {
                        service = new Service(serviceName);
                        map.put(serviceName, service);
                    }
                    service.add(r);
                    line = br.readLine();
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public Service getService(String serviceName)
    {
        return map.get(serviceName);
    }
}
