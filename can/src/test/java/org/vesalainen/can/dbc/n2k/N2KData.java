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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.can.dbc.DBCParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KData
{
    public static final N2KData N2K = new N2KData();
    
    private Map<Integer,PGNInfo> map = new HashMap<>();
    
    private N2KData()
    {
        try (   InputStream is = DBCParser.class.getResourceAsStream("/n2k.txt");
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr)
                )
        {
            PGNInfo pgnInfo = null;
            String line = br.readLine();
            while (line != null)
            {
                if (Character.isDigit(line.charAt(0)) && line.contains(" - "))
                {
                    String[] split = line.split(" - ", 2);
                    int pgn = Integer.parseInt(split[0]);
                    String name = split[1];
                    pgnInfo = new PGNInfo(pgn, line);
                    map.put(pgn, pgnInfo);
                }
                else
                {
                    if (line.startsWith("Category: "))
                    {
                        String category = line.substring(10);
                        pgnInfo.setCategory(category);
                    }
                    else
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
    public PGNInfo getPGNInfo(int pgn)
    {
        return map.get(pgn);
    }
    public static class PGNInfo
    {
        private int pgn;
        private String name;
        private String category;
        private StringBuilder description = new StringBuilder();

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
            description.append(txt);
        }
        
    }
    
}
