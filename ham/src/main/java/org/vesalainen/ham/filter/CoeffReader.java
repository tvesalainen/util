/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoeffReader
{
    public static final double[] read(String resource) throws IOException
    {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try (InputStream is = CoeffReader.class.getResourceAsStream("/"+resource);
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr)
                )
        {
            String line = br.readLine();
            while (line != null)
            {
                line = line.trim();
                if (!line.isEmpty())
                {
                    if (line.endsWith("E") || "-".equals(line))
                    {
                        sb.append(line);
                    }
                    else
                    {
                        if (sb.length() != 0)
                        {
                            list.add(sb.toString()+line);
                            sb.setLength(0);
                        }
                        else
                        {
                            list.add(line);
                        }
                    }
                }
                line = br.readLine();
            }
        }
        double[] arr = new double[list.size()];
        int index = 0;
        for (String s : list)
        {
            arr[index++] = Double.parseDouble(s.replace(',', '.'));
        }
        return arr;
    }
}
