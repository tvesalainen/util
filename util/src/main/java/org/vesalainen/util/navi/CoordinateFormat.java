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

import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.ConvertUtilityException;

/**
 * @deprecated This class doesn't have test cases also zero padding is not working.
 * Parses and formats GPS coordinates. Positive latitude means north.
 * Positive longitude means east.
 *
 * Following tokens are recognized as format tokens. All the others are
 * handled like token separators.
 *
 * DDD  Whole degrees
 * ddd  fraction degrees
 * MM   Whole minutes
 * mm   fraction minutes
 * SS   Whole seconds
 * ss   fraction seconds
 * N    Longitude N/E
 * E    Latitude W/E
 *
 * Number of formatting characters matter when formatting. If number of characters
 * is greater than outputted numbers zero padding is used.
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateFormat extends Format
{
    private static final Pattern DIGITS = Pattern.compile("[0-9]+");
    private static final Pattern SIGNED_C = Pattern.compile("[0-9,+\\-]+");
    private static final Pattern SIGNED_P = Pattern.compile("[0-9\\.+\\-]+");
    private static final Pattern LON = Pattern.compile("[WE]");
    private static final Pattern LAT = Pattern.compile("[NS]");
    private static final Pattern SEPARATOR_C = Pattern.compile("[^0-9,NSWE]*");
    private static final Pattern SEPARATOR_P = Pattern.compile("[^0-9\\.NSWE]*");
    private static final Pattern PARSE = Pattern.compile("([D]+)|([d]+)|([M]+)|([m]+)|([S]+)|([s]+)|([N])|([E])");

    /**
     * @return the latitude
     */
    public boolean isLatitude()
    {
        return latitude;
    }
    private static enum Field { DEGREE, degree, MINUTE, minute, SECOND, second, LONGITUDE, LATITUDE };
    private CharSequence originalPattern;
    private Pattern[] patterns;
    private Field[] fields;
    private Integer[] precisions;
    private Locale locale;
    private String format;
    private ParseException exception;
    private boolean latitude;
    private char decimalSeparator;

    public CoordinateFormat(CharSequence pattern)
    {
        this.locale = Locale.getDefault();
        init(pattern);
    }

    public CoordinateFormat(Locale locale, CharSequence pattern)
    {
        this.locale = locale;
        init(pattern);
    }

    private void init(CharSequence pattern)
    {
        this.originalPattern = pattern;
        DecimalFormatSymbols cfs = new DecimalFormatSymbols(locale);
        decimalSeparator = cfs.getDecimalSeparator();
        boolean latlonset = false;
        List<Pattern> pts = new ArrayList<>();
        List<Field> tps = new ArrayList<>();
        List<Integer> ps = new ArrayList<>();
        Matcher m = PARSE.matcher(pattern);
        StringBuffer sb = new StringBuffer();
        int last = 0;
        pts.add(separator());
        while (m.find())
        {
            if (m.start() != last)
            {
                pts.add(separator());
            }
            last = m.end();
            String s = m.group();
            int cc = s.charAt(0);
            switch (cc)
            {
                case 'E':
                    pts.add(LON);
                    tps.add(Field.LONGITUDE);
                    ps.add(0);
                    m.appendReplacement(sb, "%s");
                    latitude = false;
                    latlonset = true;
                    break;
                case 'N':
                    pts.add(LAT);
                    tps.add(Field.LATITUDE);
                    ps.add(0);
                    m.appendReplacement(sb, "%s");
                    latitude = true;
                    latlonset = true;
                    break;
                case 'D':
                    pts.add(DIGITS);
                    tps.add(Field.DEGREE);
                    ps.add(0);
                    m.appendReplacement(sb, String.format("%%0%dd", s.length()));
                    break;
                case 'd':
                    pts.add(signed());
                    tps.add(Field.degree);
                    ps.add(Math.max(0, s.length()-3));
                    m.appendReplacement(sb, String.format("%%0%d.%df", Math.min(1, s.length()), Math.max(0, s.length()-3)));
                    break;
                case 'M':
                    pts.add(DIGITS);
                    tps.add(Field.MINUTE);
                    ps.add(0);
                    m.appendReplacement(sb, String.format("%%0%dd", s.length()));
                    break;
                case 'S':
                    pts.add(DIGITS);
                    tps.add(Field.SECOND);
                    ps.add(0);
                    m.appendReplacement(sb, String.format("%%0%dd", s.length()));
                    break;
                case 's':
                    pts.add(signed());
                    tps.add(Field.second);
                    ps.add(Math.max(0, s.length()-3));
                    m.appendReplacement(sb, String.format("%%0%d.%df", Math.min(1, s.length()), Math.max(0, s.length()-3)));
                    break;
                case 'm':
                    pts.add(signed());
                    tps.add(Field.minute);
                    ps.add(Math.max(0, s.length()-3));
                    m.appendReplacement(sb, String.format("%%0%d.%df", Math.min(1, s.length()), Math.max(0, s.length()-3)));
                    break;
                default:
                    throw new UnsupportedOperationException("Unexpected token "+s);
            }
        }
        if (!latlonset)
        {
            throw new IllegalArgumentException("N/E not set");
        }
        if (last < pattern.length())
        {
            pts.add(separator());
        }
        m.appendTail(sb);
        patterns = pts.toArray(new Pattern[pts.size()]);
        fields = tps.toArray(new Field[tps.size()]);
        precisions = ps.toArray(new Integer[ps.size()]);
        format = sb.toString();
    }

    public Pattern signed()
    {
        if (decimalSeparator == '.')
        {
            return SIGNED_P;
        }
        else
        {
            return SIGNED_C;
        }
    }
    public Pattern separator()
    {
        if (decimalSeparator == '.')
        {
            return SEPARATOR_P;
        }
        else
        {
            return SEPARATOR_C;
        }
    }
    public String format(double coordinate)
    {
        StringBuffer sb = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        format(coordinate, sb, pos);
        return sb.toString();
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
    {
        try
        {
            Double dd = (Double) ConvertUtility.convert(Double.class, obj);
            double c1 = Math.abs(dd);
            double c2 = 60*(c1 - Math.floor(c1));
            double c3 = 60*(c2 - Math.floor(c2));
            Object[] args = new Object[fields.length];
            for (int ii=0;ii<fields.length;ii++)
            {
                switch (fields[ii])
                {
                    case DEGREE:
                        args[ii] = new Integer((int)Math.floor(c1));
                        break;
                    case degree:
                        args[ii] = trunc(dd, precisions[ii]);
                        break;
                    case MINUTE:
                        args[ii] = new Integer((int)Math.floor(c2));
                        break;
                    case minute:
                        args[ii] = trunc(c2, precisions[ii]);
                        break;
                    case SECOND:
                        args[ii] = new Integer((int)c3);
                        break;
                    case second:
                        args[ii] = trunc(c3, precisions[ii]);
                        break;
                    case LATITUDE:
                        if (dd > 0)
                        {
                            args[ii] = "N";
                        }
                        else
                        {
                            args[ii] = "S";
                        }
                        break;
                    case LONGITUDE:
                        if (dd > 0)
                        {
                            args[ii] = "E";
                        }
                        else
                        {
                            args[ii] = "W";
                        }
                        break;
                }
            }
            String res = String.format(locale, format, args);
            toAppendTo.append(res);
            return toAppendTo;
        }
        catch (ConvertUtilityException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private double trunc(double d, int precision)
    {
        double p = Math.pow(10, precision);
        return Math.floor(d*p)/p;
    }

    public double parseDouble(String source) throws ParseException
    {
        ParsePosition pos = new ParsePosition(0);
        Double dd = (Double) parseObject(source, pos);
        if (dd == null)
        {
            throw exception;
        }
        return dd.doubleValue();
    }

    @Override
    public Object parseObject(String source, ParsePosition pos)
    {
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        Boolean lat = null;
        double degree = 0;
        double minute = 0;
        double second = 0;
        double sign = 1;
        int index = 0;
        int safe = pos.getIndex();
        try
        {
            for (Pattern pattern : patterns)
            {
                Matcher mm = pattern.matcher(source);
                mm.region(pos.getIndex(), source.length());
                if (mm.lookingAt())
                {
                    pos.setIndex(mm.end());
                }
                else
                {
                    pos.setErrorIndex(pos.getIndex());
                    pos.setIndex(safe);
                    exception = new ParseException(pattern.toString(), pos.getErrorIndex());
                    return null;
                }
                if (!(SEPARATOR_P.equals(pattern) || SEPARATOR_C.equals(pattern)))
                {
                    Field field = fields[index++];
                    switch (field)
                    {
                        case DEGREE:
                            degree = Integer.parseInt(mm.group());
                            if (degree < 0 || degree > 180)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Degree not in range "+degree, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case degree:
                            degree = nf.parse(mm.group()).doubleValue();
                            if (degree < 0 || degree > 180)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Degree not in range "+degree, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case MINUTE:
                            minute = Integer.parseInt(mm.group());
                            if (minute < 0 || minute > 60)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Minute not in range "+minute, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case minute:
                            minute = nf.parse(mm.group()).doubleValue();
                            if (minute < 0 || minute > 60)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Minute not in range "+minute, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case SECOND:
                            second = Integer.parseInt(mm.group());
                            if (second < 0 || second > 60)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Second not in range "+second, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case second:
                            second = nf.parse(mm.group()).doubleValue();
                            if (second < 0 || second > 60)
                            {
                                pos.setErrorIndex(pos.getIndex());
                                pos.setIndex(safe);
                                exception = new ParseException("Second not in range "+second, pos.getErrorIndex());
                                return null;
                            }
                            break;
                        case LATITUDE:
                            lat = true;
                            if ("S".equals(mm.group()))
                            {
                                sign = -1;
                            }
                            break;
                        case LONGITUDE:
                            lat = false;
                            if ("W".equals(mm.group()))
                            {
                                sign = -1;
                            }
                            break;
                    }
                }
            }
        }
        catch (ParseException ex)
        {
            pos.setErrorIndex(pos.getIndex());
            pos.setIndex(safe);
            exception = ex;
            return null;
        }
        catch (NumberFormatException ex)
        {
            pos.setErrorIndex(pos.getIndex());
            pos.setIndex(safe);
            exception = new ParseException(ex.getMessage(), pos.getErrorIndex());
            return null;
        }
        double res = sign*(degree+minute/60.+second/3600.);
        if (lat != null && lat)
        {
            if (Math.abs(res) > 90)
            {
                pos.setErrorIndex(pos.getIndex());
                pos.setIndex(safe);
                exception = new ParseException("Latitude not in range "+res, pos.getErrorIndex());
                return null;
            }
        }
        else
        {
            if (Math.abs(res) > 180)
            {
                pos.setErrorIndex(pos.getIndex());
                pos.setIndex(safe);
                exception = new ParseException("Longitude not in range "+res, pos.getErrorIndex());
                return null;
            }
        }
        return new Double(res);
    }

    public String toString()
    {
        return originalPattern.toString();
    }

    public static void main(String... args)
    {
        try
        {
            CoordinateFormat cf = new CoordinateFormat(Locale.US, "DDD,mmmmmN");
            System.err.println(cf.parseObject(cf.format(0.99999999999999)));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
