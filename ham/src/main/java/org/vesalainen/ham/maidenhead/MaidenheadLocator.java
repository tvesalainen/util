package org.vesalainen.ham.maidenhead;

/*
 * Copyright (C) 2013 Timo Vesalainen
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


import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.vesalainen.util.navi.Location;

/**
 * @author Timo Vesalainen
 * @see <a href="http://en.wikipedia.org/wiki/Maidenhead_Locator_System">Maidenhead Locator System</a>
 */
public class MaidenheadLocator implements Cloneable
{
    private char latField;
    private char latSquare;
    private char latSubsquare;
    private char lonField;
    private char lonSquare;
    private char lonSubsquare;
    public enum LocatorLevel {Field, Square, Subsquare };

    public MaidenheadLocator(String value)
    {
        Objects.requireNonNull(value);
        if (value.length() != 6)
        {
            throw new IllegalArgumentException(value);
        }
        this.lonField = value.charAt(0);
        this.latField = value.charAt(1);
        this.lonSquare = value.charAt(2);
        this.latSquare = value.charAt(3);
        this.lonSubsquare = value.charAt(4);
        this.latSubsquare = value.charAt(5);
    }

    public MaidenheadLocator(Location location)
    {
        this(location.getLatitude(), location.getLongitude());
    }
    
    public MaidenheadLocator(double latitude, double longitude)
    {
        latitude += 90;
        latField = (char) ('A' + (latitude / 10));
        latSquare = (char) ('0' + latitude % 10);
        latSubsquare = (char) ('A' + (latitude % 1) * 24);
        
        longitude += 180;
        longitude /= 2;
        lonField = (char) ('A' + (longitude / 10));
        lonSquare = (char) ('0' + longitude % 10);
        lonSubsquare = (char) ('A' + (longitude % 1) * 24);
    }
    public Location getLocation()
    {
        return new Location(getLatitude(), getLongitude());
    }
    public double getLatitude()
    {
        double latitude = (latField - 'A')*10;
        latitude += (latSquare - '0');
        latitude += (double)(latSubsquare - 'A')/24.0;
        return latitude-90;
    }
    public double getLongitude()
    {
        double longitude = (lonField - 'A')*10;
        longitude += (lonSquare - '0');
        longitude += (double)(lonSubsquare - 'A')/24.0;
        return longitude*2-180;
    }
    @Override
    protected MaidenheadLocator clone() throws CloneNotSupportedException
    {
        return (MaidenheadLocator) super.clone();
    }

    public void addLongitude(LocatorLevel level)
    {
        switch (level)
        {
            case Field:
                if (lonField == 'R')
                {
                    throw new IllegalArgumentException("cannot add R");
                }
                lonField++;
                break;
            case Square:
                if (lonSquare == '9')
                {
                    addLongitude(LocatorLevel.Field);
                    lonSquare = '0';
                }
                else
                {
                    lonSquare++;
                }
                break;
            case Subsquare:
                if (lonSubsquare == 'X')
                {
                    addLongitude(LocatorLevel.Square);
                    lonSubsquare = 'A';
                }
                else
                {
                    lonSubsquare++;
                }
                break;
            default:
                throw new IllegalArgumentException("unknown "+level);
        }
    }
    
    public void addLatitude(LocatorLevel level)
    {
        switch (level)
        {
            case Field:
                if (latField == 'R')
                {
                    throw new IllegalArgumentException("cannot add R");
                }
                latField++;
                break;
            case Square:
                if (latSquare == '9')
                {
                    addLatitude(LocatorLevel.Field);
                    latSquare = '0';
                }
                else
                {
                    latSquare++;
                }
                break;
            case Subsquare:
                if (latSubsquare == 'X')
                {
                    addLatitude(LocatorLevel.Square);
                    latSubsquare = 'A';
                }
                else
                {
                    latSubsquare++;
                }
                break;
            default:
                throw new IllegalArgumentException("unknown "+level);
        }
    }
    
    public static void check(MaidenheadLocator[] bb)
    {
    }
    public static int fieldCountBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw = bb[0];
        MaidenheadLocator ne = bb[1];
        return (ne.latField-sw.latField+1)*(ne.lonField-sw.lonField+1);
    }
    public static int squareCountBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw = bb[0];
        MaidenheadLocator ne = bb[1];
        return squareCountBetweenLat(sw, ne)*squareCountBetweenLon(sw, ne);
    }
    private static int squareCountBetweenLat(MaidenheadLocator sw, MaidenheadLocator ne)
    {
        int count = 0;
        char swLatField = sw.latField;
        char neLatField = ne.latField;
        if (swLatField == neLatField)
        {
            return ne.latSquare - sw.latSquare + 1;
        }
        count += '9' - sw.latSquare + 1;
        count += (neLatField - (swLatField+1))*10;
        count += ne.latSquare - '0' + 1;
        return count;
    }
    private static int squareCountBetweenLon(MaidenheadLocator sw, MaidenheadLocator ne)
    {
        int count = 0;
        char swLonField = sw.lonField;
        char neLonField = ne.lonField;
        if (swLonField == neLonField)
        {
            return ne.lonSquare - sw.lonSquare + 1;
        }
        count += '9' - sw.lonSquare + 1;
        count += (neLonField - (swLonField+1))*10;
        count += ne.lonSquare - '0' + 1;
        return count;
    }
    public static int subsquareCountBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw = bb[0];
        MaidenheadLocator ne = bb[1];
        return subsquareCountBetweenLon(sw,ne)*subsquareCountBetweenLat(sw,ne);
    }
    private static int subsquareCountBetweenLat(MaidenheadLocator sw, MaidenheadLocator ne)
    {
        int count = 0;
        int squareCountBetweenLat = squareCountBetweenLat(sw, ne);
        if (squareCountBetweenLat == 1)
        {
            return ne.latSubsquare - sw.latSubsquare + 1;
        }
        count += 'X' - sw.latSubsquare + 1;
        count += (squareCountBetweenLat - 2)*24;
        count += ne.latSubsquare - 'A' + 1;
        return count;
    }
    private static int subsquareCountBetweenLon(MaidenheadLocator sw, MaidenheadLocator ne)
    {
        int count = 0;
        int squareCountBetweenLon = squareCountBetweenLon(sw, ne);
        if (squareCountBetweenLon == 1)
        {
            return ne.lonSubsquare - sw.lonSubsquare + 1;
        }
        count += 'X' - sw.lonSubsquare + 1;
        count += (squareCountBetweenLon - 2)*24;
        count += ne.lonSubsquare - 'A' + 1;
        return count;
    }
    public static Set<String> fieldsBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw = bb[0];
        MaidenheadLocator ne = bb[1];
        Set<String> set = new TreeSet<>();
        for (char latf=sw.latField;latf<=ne.latField;latf++)
        {
            for (char lonf=sw.lonField;lonf<=ne.lonField;lonf++)
            {
                set.add(new String(new char[] {lonf, latf}));
            }
        }
        return set;
    }
    public static Set<String> squaresBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw;
        try
        {
            sw = bb[0].clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        MaidenheadLocator ne = bb[1];
        Set<String> set = new TreeSet<>();
        int squareCountBetweenLon = MaidenheadLocator.squareCountBetweenLon(sw, ne);
        int squareCountBetweenLat = MaidenheadLocator.squareCountBetweenLat(sw, ne);
        char initLatField = sw.latField;
        char initLatSquare = sw.latSquare;
        for (int lon = 0;lon < squareCountBetweenLon; lon++)
        {
            sw.latField = initLatField;
            sw.latSquare = initLatSquare;
            for (int lat = 0;lat < squareCountBetweenLat;lat++)
            {
                set.add(sw.getSquare());
                sw.addLatitude(LocatorLevel.Square);
            }
            sw.addLongitude(LocatorLevel.Square);
        }
        return set;
    }
    public static Set<String> subsquaresBetween(MaidenheadLocator[] bb)
    {
        check(bb);
        MaidenheadLocator sw;
        try
        {
            sw = bb[0].clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        MaidenheadLocator ne = bb[1];
        Set<String> set = new TreeSet<>();
        int subsquareCountBetweenLon = MaidenheadLocator.subsquareCountBetweenLon(sw, ne);
        int subsquareCountBetweenLat = MaidenheadLocator.subsquareCountBetweenLat(sw, ne);
        char initLatField = sw.latField;
        char initLatSquare = sw.latSquare;
        char initLatSubsquare = sw.latSubsquare;
        for (int lon = 0;lon < subsquareCountBetweenLon; lon++)
        {
            sw.latField = initLatField;
            sw.latSquare = initLatSquare;
            sw.latSubsquare = initLatSubsquare;
            for (int lat = 0;lat < subsquareCountBetweenLat;lat++)
            {
                set.add(sw.getSubsquare());
                sw.addLatitude(LocatorLevel.Subsquare);
            }
            sw.addLongitude(LocatorLevel.Subsquare);
        }
        return set;
    }
    public String getField()
    {
        return new String(new char[] {lonField, latField});
    }
    
    public String getSquare()
    {
        return new String(new char[] {lonField, latField, lonSquare, latSquare});
    }
    
    public String getSubsquare()
    {
        return new String(new char[] {lonField, latField, lonSquare, latSquare, lonSubsquare, latSubsquare});
    }

    @Override
    public String toString()
    {
        return getSubsquare();
    }

}
