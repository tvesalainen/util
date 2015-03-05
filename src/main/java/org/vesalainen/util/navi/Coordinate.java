/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util.navi;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author tkv
 */
public class Coordinate
{

    public enum Type {LATITUDE, LONGITUDE, UNKNOWN};
    public enum Direction {N, S, W, E};
    
    private static final String DEGREE = "\u00B0";
    private static final CoordinateFormat DEGMINSECFORMATLAT = new CoordinateFormat(Locale.US, "DD\u00B0MM'ssss\"N");
    private static final CoordinateFormat DEGMINSECFORMATLON = new CoordinateFormat(Locale.US, "DDD\u00B0MM'ssss\"E");
    private static final CoordinateFormat DEGMINFORMATLAT = new CoordinateFormat(Locale.US, "DD\u00B0mmmmm'N");
    private static final CoordinateFormat DEGMINFORMATLON = new CoordinateFormat(Locale.US, "DDD\u00B0mmmmm'E");
    private static final CoordinateFormat DEGFORMATLAT = new CoordinateFormat(Locale.US, "ddddd\u00B0N");
    private static final CoordinateFormat DEGFORMATLON = new CoordinateFormat(Locale.US, "dddddd\u00B0E");
    private static final CoordinateFormat[] FORMATS = {
                                                        DEGFORMATLAT,
                                                        DEGFORMATLON,
                                                        DEGMINFORMATLAT,
                                                        DEGMINFORMATLON,
                                                        DEGMINSECFORMATLAT,
                                                        DEGMINSECFORMATLON
                                                    };
    private double _coordinate;
    private Type _type;

    /** Creates a new instance of Coordinate
     * @param coordinate If type is LATITUDE negative value means SOUTH. If type is LONGITUDE negative value means WEST
     * @param type
     */
    public Coordinate(double coordinate, Type type)
    {
        _coordinate = coordinate;
        _type = type;
    }

    /**
     * Creates a new instance of Coordinate
     * @param coordinate
     * @param direction
     */
    protected Coordinate(double coordinate, Direction direction)
    {
        if (coordinate < 0)
        {
            throw new IllegalArgumentException(String.valueOf(coordinate));
        }
        switch (direction)
        {
            case N:
            _coordinate = coordinate;
            _type = Type.LATITUDE;
            break;
            case S:
            _coordinate = -coordinate;
            _type = Type.LATITUDE;
            break;
            case W:
            _coordinate = -coordinate;
            _type = Type.LONGITUDE;
            break;
            case E:
            _coordinate = coordinate;
            _type = Type.LONGITUDE;
            break;
        }
    }

    /** 
     * Creates a new instance of Coordinate
     * @param str Coordinate in one of following formats
     * Degree minute second. Example 60\u00B025'34.5"N
     * Degree minute. Example 60\u00B025.5' W
     * Decimal. Example -60,1234
     */
    public Coordinate(String str)
    {
        Coordinate coordinate = match(str);
        _coordinate = coordinate.getCoordinate();
        _type = coordinate.getType();
    }

    public static final Coordinate match(String str)
    {
        for (CoordinateFormat fmt : FORMATS)
        {
            try
            {
                double dd = fmt.parseDouble(str);
                if (fmt.isLatitude())
                {
                    return new Coordinate(dd, Type.LATITUDE);
                }
                else
                {
                    return new Coordinate(dd, Type.LONGITUDE);
                }
            }
            catch (ParseException ex)
            {

            }
        }
        throw new IllegalArgumentException(str);
    }
    
    public static final Coordinate lookingAt(String str, ParsePosition idx)
    {
        for (CoordinateFormat fmt : FORMATS)
        {
            ParsePosition pos = new ParsePosition(idx.getIndex());
            Double dd = (Double) fmt.parseObject(str, pos);
            if (dd != null)
            {
                idx.setIndex(pos.getIndex());
                if (fmt.isLatitude())
                {
                    return new Coordinate(dd, Type.LATITUDE);
                }
                else
                {
                    return new Coordinate(dd, Type.LONGITUDE);
                }
            }
        }
        throw new IllegalArgumentException(str);
    }

    /**
     * 
     * @return Coordinate as decimal. Negative means south or west
     */
    public double getCoordinate()
    {
        return _coordinate;
    }

    public Type getType()
    {
        return _type;
    }
    /**
     * 
     * @return Degree minute. Example 60\u00B025.5' W
     */
    @Override
    public String toString()
    {
        return toDegMin();
    }

    public String toDeg()
    {
        return toDeg(_coordinate, _type);
    }
    
    public String toDegMin()
    {
        return toDegMin(_coordinate, _type);
    }
    
    public String toDegMinSec()
    {
        return toDegMinSec(_coordinate, _type);
    }
    
    public String getWhere()
    {
        return getWhere(_coordinate, _type);
    }

    public static String toDeg(double coordinate, Type type)
    {
        switch (type)
        {
            case LATITUDE:
                return DEGMINFORMATLAT.format(coordinate);
            case LONGITUDE:
                return DEGMINFORMATLON.format(coordinate);
            default:
                throw new UnsupportedOperationException("Unknown type");
        }
    }
    
    public static String toDegMin(double coordinate, Type type)
    {
        switch (type)
        {
            case LATITUDE:
                return DEGMINFORMATLAT.format(coordinate);
            case LONGITUDE:
                return DEGMINFORMATLON.format(coordinate);
            default:
                throw new UnsupportedOperationException("Unknown type");
        }
    }
    
    public static String toDegMinSec(double coordinate, Type type)
    {
        switch (type)
        {
            case LATITUDE:
                return DEGMINSECFORMATLAT.format(coordinate);
            case LONGITUDE:
                return DEGMINSECFORMATLON.format(coordinate);
            default:
                throw new UnsupportedOperationException("Unknown type");
        }
    }
    
    public static String getWhere(double coordinate, Type type)
    {
        switch (type)
        {
            case LATITUDE:
                if (coordinate >= 0)
                {
                    return "N";
                }
                else
                {
                    return "S";
                }
            case LONGITUDE:
                if (coordinate >= 0)
                {
                    return "E";
                }
                else
                {
                    return "W";
                }
            default:
                return "?";
        }
        
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof Coordinate)
        {
            Coordinate coordinate = (Coordinate)oth;
            return getType().equals(coordinate.getType()) &&
                    Math.abs(_coordinate - coordinate._coordinate) < 0.0001;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 47 * hash + (int) (Double.doubleToLongBits(this._coordinate) ^ (Double.doubleToLongBits(this._coordinate) >>> 32));
        hash = 47 * hash + this._type.hashCode();
        return hash;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Random rand = new Random();
            for (int ii=0;ii<1000;ii++)
            {
                Coordinate c1 = null;
                if (rand.nextBoolean())
                {
                    double coordinate = 180*rand.nextDouble()-90;
                    c1 = new Coordinate(coordinate, Coordinate.Type.LATITUDE);
                }
                else
                {
                    double coordinate = 360*rand.nextDouble()-180;
                    c1 = new Coordinate(coordinate, Coordinate.Type.LONGITUDE);
                }
                Coordinate c2 = new Coordinate(c1.toDeg());
                Coordinate c3 = new Coordinate(c1.toDegMin());
                Coordinate c4 = new Coordinate(c1.toDegMinSec());
                check(c1, c2);
                check(c1, c3);
                check(c1, c4);
                //System.err.println(c1);
            }
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static void check(Coordinate c1, Coordinate c2)
    {
        if (!c1.equals(c2))
        {
            double diff = c1.getCoordinate()-c2.getCoordinate();
            System.err.println(c1+" <> "+c2+" "+diff);
        }
    }
}
