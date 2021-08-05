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
package org.vesalainen.can.dbc.n2k;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.vesalainen.can.dbc.DBCParser;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KData
{
    public static final N2KData N2K = new N2KData();
    
    private MapList<Integer,PGNInfo> map = new HashMapList<>();
    
    private N2KData()
    {
        n2kmsg();
        n2k();
    }
    public PGNInfo getPGNInfo(int pgn, String name)
    {
        List<PGNInfo> list = map.get(pgn);
        if (list != null)
        {
            if (list.isEmpty())
            {
                return null;
            }
            if (list.size() == 1)
            {
                return list.get(0);
            }
            else
            {
                for (int ii=0;ii<list.size();ii++)
                {
                    PGNInfo p = list.get(ii);
                    if (p.name.toString().contains(name))
                    {
                        return p;
                    }
                }
                throw new IllegalArgumentException(pgn+" "+name+" not found");
            }
        }
        return null;
    }
    public IntStream getPGNs()
    {
        return map.keySet().stream().mapToInt((i)->i);
    }

    private void n2k()
    {
        try (   InputStream is = DBCParser.class.getResourceAsStream("/n2k.txt");
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr)
                )
        {
            List<PGNInfo> pgnInfos = null;
            String line = br.readLine();
            while (line != null)
            {
                if (Character.isDigit(line.charAt(0)) && line.contains(" - "))
                {
                    String[] split = line.split(" - ", 2);
                    int pgn = Integer.parseInt(split[0]);
                    String name = split[1];
                    pgnInfos = map.get(pgn);
                }
                else
                {
                    if (line.startsWith("Category: "))
                    {
                        String category = line.substring(10);
                        pgnInfos.forEach((p)->p.setCategory(category));
                    }
                }
                line = br.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(N2KData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void n2kmsg()
    {
        try (   InputStream is = DBCParser.class.getResourceAsStream("/n2kmsg.txt");
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr)
                )
        {
            PGNInfo pgnInfo = null;
            String line = br.readLine();
            while (line != null)
            {
                if (Character.isDigit(line.charAt(0)))
                {
                    String[] split = line.split(" ", 2);
                    int n = 0;
                    try
                    {
                        n = Integer.parseInt(split[0]);
                    }
                    catch (NumberFormatException nfe)
                    {
                        n = -1;
                    }
                    if (n > 0)
                    {
                        if (n > 100)
                        {
                            pgnInfo = new PGNInfo(n, "Pgn"+line);
                            map.add(n, pgnInfo);
                        }
                        else
                        {
                            pgnInfo.setField(n, split[1]);
                        }
                    }
                }
                else
                {
                    if (!line.equals("Field # Field Description"))
                    {
                        pgnInfo.addDescription(line);
                    }
                }
                line = br.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(N2KData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static class PGNInfo
    {
        private int pgn;
        private String name;
        private String category = "Unknown";
        private StringBuilder description = new StringBuilder();
        private List<String> fields = new ArrayList<>();

        public PGNInfo(int pgn, String name)
        {
            this.pgn = pgn;
            this.name = name;
        }

        public int getPgn()
        {
            return pgn;
        }

        public String getName()
        {
            return name;
        }

        public String getCategory()
        {
            return category;
        }

        public void setCategory(String category)
        {
            this.category = category;
        }

        public String getDescription()
        {
            return description.toString();
        }

        public void addDescription(String txt)
        {
            if (description.length() > 0)
            {
                description.append(' ');
            }
            description.append(txt.replace('"', '\''));
        }
        public boolean isFieldInRange(int order)
        {
            return order >= 1 && order <= fields.size();
        }
        public String getField(int order)
        {
            if (!isFieldInRange(order))
            {
                throw new IllegalArgumentException("order out of range");
            }
            return fields.get(order-1);
        }
        private void setField(int n, String field)
        {
            if (n != fields.size()+1)
            {
                throw new IllegalArgumentException("not in sequence");
            }
            fields.add(field);
        }
        
    }
    
}
