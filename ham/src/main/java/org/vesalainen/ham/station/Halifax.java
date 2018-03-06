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
package org.vesalainen.ham.station;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.vesalainen.ham.jaxb.HfFaxType;
import org.vesalainen.math.OrderedPair;
import org.vesalainen.math.Sets;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Halifax extends DefaultCustomizer
{

    @Override
    public String scheduleLine(String line)
    {
        return super.scheduleLine(line)
                .replace("0&12/12&0", "00/12")
                .replace("18&00", "18/00")
                .replace("06&12", "06/12")
                ;
    }
    
    @Override
    public void after(HfFaxType hfFax, String line)
    {
        line = line.replace("SATELLITE", "");
        EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek dow : DayOfWeek.values())
        {
            int idx = line.indexOf(dow.name().substring(0, 3));
            if (idx != -1)
            {
                set.add(dow);
            }
        }
        Set<OrderedPair<DayOfWeek, DayOfWeek>> cartesianProduct = Sets.cartesianProduct(set, set);
        for (OrderedPair<DayOfWeek, DayOfWeek> pair : cartesianProduct)
        {
            DayOfWeek fst = pair.getFirstEntry();
            DayOfWeek sec = pair.getSecondEntry();
            if (fst != sec)
            {
                String key = String.format("%3.3s-%3.3s", fst, sec);
                if (line.indexOf(key) != -1)
                {
                    int o1 = fst.ordinal();
                    int o2 = sec.ordinal();
                    if (o1 < o2)
                    {
                        for (int ii=o1;ii<o2;ii++)
                        {
                            set.add(DayOfWeek.values()[ii]);
                        }
                    }
                    else
                    {
                        for (int ii=o1;ii<7;ii++)
                        {
                            set.add(DayOfWeek.values()[ii]);
                        }
                        for (int ii=0;ii<o2;ii++)
                        {
                            set.add(DayOfWeek.values()[ii]);
                        }
                    }
                }
            }
        }
        List<String> list = set.stream().map((d)->d.name().substring(0, 3)).collect(Collectors.toList());
        hfFax.getWeekdays().addAll(list);
    }
}
