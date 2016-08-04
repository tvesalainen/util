/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class EasterCalendar extends GregorianCalendar
{

    private Locale locale;
    private boolean finnish;
    
    public EasterCalendar(int i, int i1, int i2, int i3, int i4, int i5)
    {
        super(i, i1, i2, i3, i4, i5);
        locale = Locale.getDefault();
        finnish = "FIN".equals(locale.getISO3Country());
    }

    public EasterCalendar(int i, int i1, int i2, int i3, int i4)
    {
        super(i, i1, i2, i3, i4);
        locale = Locale.getDefault();
        finnish = "FIN".equals(locale.getISO3Country());
    }

    public EasterCalendar(int i, int i1, int i2)
    {
        super(i, i1, i2);
        locale = Locale.getDefault();
        finnish = "FIN".equals(locale.getISO3Country());
    }

    public EasterCalendar(TimeZone tz, Locale locale)
    {
        super(tz, locale);
        this.locale = locale;
        finnish = "FIN".equals(locale.getISO3Country());
    }

    public EasterCalendar(Locale locale)
    {
        this(TimeZone.getDefault(), locale);
    }

    public EasterCalendar(TimeZone tz)
    {
        this(tz, Locale.getDefault());
    }

    public EasterCalendar()
    {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    public boolean isHolyday()
    {
//System.out.print(getTime().toString());
        if (get(DAY_OF_WEEK) == SUNDAY)
        {
//System.out.println("=Sunday" );
            return true;
        }

        if (get(MONTH) == JANUARY && get(DATE) == 1)
        {
//System.out.println("=Uudenvuoden paiva" );
            return true;
        }

        if (get(MONTH) == JANUARY && get(DATE) == 6)
        {
//System.out.println("=loppiainen" );
            return true;
        }

        if (get(MONTH) == MAY && get(DATE) == 1)
        {
//System.out.println("=wappu" );
            return true;
        }

        if (get(MONTH) == JUNE && get(DAY_OF_WEEK) == FRIDAY)
        {
            if (get(DATE) <= 25 && get(DATE) > 18)
            {
//System.out.println("=juhannus" );
                return finnish && true;
            }
        }

        if (get(MONTH) == DECEMBER && get(DATE) == 6)
        {
//System.out.println("=itsenaisyyspaiva" );
            return finnish && true;
        }

        if (get(MONTH) == DECEMBER && get(DATE) == 24)
        {
//System.out.println("=joulu" );
            return true;
        }

        if (get(MONTH) == DECEMBER && get(DATE) == 25)
        {
//System.out.println("=joulu" );
            return true;
        }

        if (get(MONTH) == DECEMBER && get(DATE) == 26)
        {
//System.out.println("=joulu" );
            return true;
        }

        if (isEaster(this))
        {
//System.out.println("=Easter" );
            return true;
        }

        add(DATE, 2);
        if (isEaster(this))
        {
//System.out.println("=Good friday" );
            add(DATE, -2);
            return true;
        }
        add(DATE, -2);

        add(DATE, -1);
        if (isEaster(this))
        {
//System.out.println("=Easter2" );
            add(DATE, 1);
            return true;
        }
        add(DATE, 1);

        add(DATE, -39);
        if (isEaster(this))
        {
//System.out.println("=helatorstai" );
            add(DATE, 39);
            return finnish && true;
        }
        add(DATE, 39);


//System.out.println("=Work Day" );
        return false;
    }

    public boolean isEaster(Calendar cal)
    {
        int C;
        int N;
        int K;
        int I;
        int J;
        int L;
        int M;
        int D;
        int Y;

        Y = cal.get(Calendar.YEAR);
        C = Y / 100;
        N = Y - 19 * (Y / 19);
        K = (C - 17) / 25;
        I = C - C / 4 - (C - K) / 3 + 19 * N + 15;
        I = I - 30 * (I / 30);
        I = I - (I / 28) * (1 - (I / 28) * (29 / (I + 1)) * ((21 - N) / 11));
        J = Y + Y / 4 + I + 2 - C + C / 4;
        J = J - 7 * (J / 7);
        L = I - J;
        M = 3 + (L + 40) / 44;
        D = L + 28 - 31 * (M / 4);

        return (M == cal.get(Calendar.MONTH) + 1 && D == cal.get(Calendar.DATE));
    }

}
