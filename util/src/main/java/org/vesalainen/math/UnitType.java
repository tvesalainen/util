/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum UnitType
{
    MULTIPLIER_10_ONE(UnitCategory.POWER_OF_10_MULTIPLIER, 1, ""),
    MULTIPLIER_10_YOCTO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-24, "y"),
    MULTIPLIER_10_ZEPTO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-21, "z"),
    MULTIPLIER_10_ATTO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-18, "a"),
    MULTIPLIER_10_FEMTO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-15, "f"),
    MULTIPLIER_10_PICO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-12, "p"),
    MULTIPLIER_10_NANO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-9, "n"),
    MULTIPLIER_10_MICRO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-6, "μ"),
    MULTIPLIER_10_MILLI(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-3, "m"),
    MULTIPLIER_10_CENTI(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-2, "c"),
    MULTIPLIER_10_DECI(UnitCategory.POWER_OF_10_MULTIPLIER, 1e-1, "d"),
    MULTIPLIER_10_DECA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e1, "da"),
    MULTIPLIER_10_HECTO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e2, "h"),
    MULTIPLIER_10_KILO(UnitCategory.POWER_OF_10_MULTIPLIER, 1e3, "k"),
    MULTIPLIER_10_MEGA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e6, "M"),
    MULTIPLIER_10_GIGA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e9, "G"),
    MULTIPLIER_10_TERA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e12, "T"),
    MULTIPLIER_10_PETA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e15, "P"),
    MULTIPLIER_10_EXA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e18, "E"),
    MULTIPLIER_10_ZETTA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e21, "Z"),
    MULTIPLIER_10_YOTTA(UnitCategory.POWER_OF_10_MULTIPLIER, 1e24, "Y"),
    
    MULTIPLIER_2_ONE(UnitCategory.POWER_OF_2_MULTIPLIER, 1, ""),
    MULTIPLIER_2_KILO(UnitCategory.POWER_OF_2_MULTIPLIER, 1024, "k"),
    MULTIPLIER_2_MEGA(UnitCategory.POWER_OF_2_MULTIPLIER, 1048576, "M"),
    MULTIPLIER_2_GIGA(UnitCategory.POWER_OF_2_MULTIPLIER, 1073741824, "G"),
    MULTIPLIER_2_TERA(UnitCategory.POWER_OF_2_MULTIPLIER, Math.pow(2, 40), "T"),
    MULTIPLIER_2_PETA(UnitCategory.POWER_OF_2_MULTIPLIER, Math.pow(2, 50), "P"),
    MULTIPLIER_2_EXA(UnitCategory.POWER_OF_2_MULTIPLIER, Math.pow(2, 60), "E"),
    MULTIPLIER_2_ZETTA(UnitCategory.POWER_OF_2_MULTIPLIER, Math.pow(2, 70), "Z"),
    MULTIPLIER_2_YOTTA(UnitCategory.POWER_OF_2_MULTIPLIER, Math.pow(2, 80), "Y"),
    
    DURATION_DAYS(UnitCategory.DURATION, TimeUnit.DAYS.toSeconds(1), "d"),
    DURATION_HOURS(UnitCategory.DURATION, TimeUnit.HOURS.toSeconds(1), "h"),
    DURATION_MINUTES(UnitCategory.DURATION, TimeUnit.MINUTES.toSeconds(1), "m"),
    DURATION_SECONDS(UnitCategory.DURATION, 1, "s"),
    DURATION_MILLI_SECONDS(UnitCategory.DURATION, 0.001, "ms"),
    DURATION_MICRO_SECONDS(UnitCategory.DURATION, 0.000001, "μs"),
    DURATION_NANO_SECONDS(UnitCategory.DURATION, 0.000000001, "ns"),
    /**
     * m/s<sup>2</sup>
     */
    MSS(UnitCategory.ACCELERATION, 1.0, "m/s2"),    // TODO
    /**
     * <i>g</i>0 Standard acceleration
     */
    GFORCE_EARTH(UnitCategory.ACCELERATION, 9.80665, "g"),
    /**
     * PASCAL
     */
    PASCAL(UnitCategory.PRESSURE, 1.0, "Pa"),
    /**
     * hPa 
     */
    HPA(UnitCategory.PRESSURE, 100.0, "hPa"),
    /**
     * Bar
     */
    BAR(UnitCategory.PRESSURE, 100000.0, "bar"),
    /**
     * Atmosphere
     */
    ATM(UnitCategory.PRESSURE, 101325.0, "atm"),
    /**
     * DEGREE 0 - 360
     */
    DEGREE(UnitCategory.PLANE_ANGLE, 1.0, "\u00B0"),
    /**
     * DEGREE -180 - 180
     */
    DEGREE_NEG(UnitCategory.PLANE_ANGLE, "\u00B0", (double v)->{return v > 180 ? v-360 : v;}, (double v)->{return v < 0 ? 360+v : v;}),
    /**
     * Radians
     */
    RADIAN(UnitCategory.PLANE_ANGLE, Math.toDegrees(1), "Rad"),
    /**
     * N, E, S, W
     */
    CARDINAL_DIRECTION(UnitCategory.PLANE_ANGLE, 1.0, ""),
    /**
     * N, NE, E, SE, ...
     */
    INTERCARDINAL_DIRECTION(UnitCategory.PLANE_ANGLE, 1.0, ""),
    /**
     * N, NNE, NE, ENE, E, ESE, SE, ...
     */
    SECONDARY_INTERCARDINAL_DIRECTION(UnitCategory.PLANE_ANGLE, 1.0, ""),
    /**
     * CELSIUS
     */
    CELSIUS(UnitCategory.TEMPERATURE, 1.0, "\u00B0C"),
    /**
     * FAHRENHEIT
     */
    FAHRENHEIT(UnitCategory.TEMPERATURE, "\u00B0F", (double v)->{return v*1.8+32.0;}, (double v)->{return (v-32.0)/1.8;}),
    /**
     * KELVIN
     */
    KELVIN(UnitCategory.TEMPERATURE, "\u00B0K", (double v)->{return v+273.15;}, (double v)->{return v-273.15;}),
    /**
     * FATHOM
     */
    FATHOM(UnitCategory.LENGTH, 1.8288, "Fathom"),
    /**
     * METER
     */
    METER(UnitCategory.LENGTH, 1.0, "m"),
    /**
     * MILE
     */
    MILE(UnitCategory.LENGTH, 1609.34, "Mile"),
    /**
     * FOOT
     */
    FOOT(UnitCategory.LENGTH, 0.3048, "Foot"),
    /**
     * YARD
     */
    YARD(UnitCategory.LENGTH, 0.9144, "Yard"),
    /**
     * INCH
     */
    INCH(UnitCategory.LENGTH, 0.0254, "Inch"),
    /**
     * Kilometer
     */
    KILO_METER(UnitCategory.LENGTH, 1000.0, "Km"),
    /**
     * Nautical mile
     */
    NAUTICAL_MILE(UnitCategory.LENGTH, 1852.0, "NM"),
    /**
     * Nautical mile
     */
    NAUTICAL_DEGREE(UnitCategory.LENGTH, 60*1852.0, "˚"),
    /**
     * KNOT
     */
    KNOT(UnitCategory.SPEED, 0.514444, "Knots"),
    /**
     * m/s
     */
    METERS_PER_SECOND(UnitCategory.SPEED, 1.0, "m/s"),
    /**
     * Km/h
     */
    KILO_METERS_PER_HOUR(UnitCategory.SPEED, 0.277778, "Km/h"),
    /**
     * Miles/hour
     */
    MILES_PER_HOUR(UnitCategory.SPEED, 0.44704, "Miles/h"),
    /**
     * BEAUFORT
     */
    BEAUFORT(UnitCategory.SPEED, "B", (double v)->{return Math.round(Math.pow(v/0.837, 2.0/3.0));}, (double v)->{return 0.837*Math.pow(v, 3.0/2.0);}),
    /**
     * @deprecated Use LATITUDE/LONGITUDE equivalent
     * COORDINATE degrees
     */
    COORDINATE_DEGREES(UnitCategory.COORDINATE, 1, "˚"),
    /**
     * @deprecated Use LATITUDE/LONGITUDE equivalent
     * COORDINATE degrees and minutes
     */
    COORDINATE_DEGREES_AND_MINUTES(UnitCategory.COORDINATE, 1, ""),
    /**
     * @deprecated Use LATITUDE/LONGITUDE equivalent
     * COORDINATE degrees, minutes and seconds
     */
    COORDINATE_DEGREES_MINUTES_SECONDS(UnitCategory.COORDINATE, 1, ""),
    /**
     * COORDINATE degrees
     */
    COORDINATE_DEGREES_LONGITUDE(UnitCategory.COORDINATE, 1, "˚"),
    /**
     * COORDINATE degrees and minutes
     */
    COORDINATE_DEGREES_AND_MINUTES_LONGITUDE(UnitCategory.COORDINATE, 1, ""),
    /**
     * COORDINATE degrees, minutes and seconds
     */
    COORDINATE_DEGREES_MINUTES_SECONDS_LONGITUDE(UnitCategory.COORDINATE, 1, ""),
    /**
     * COORDINATE degrees
     */
    COORDINATE_DEGREES_LATITUDE(UnitCategory.COORDINATE, 1, "˚"),
    /**
     * COORDINATE degrees and minutes
     */
    COORDINATE_DEGREES_AND_MINUTES_LATITUDE(UnitCategory.COORDINATE, 1, ""),
    /**
     * COORDINATE degrees, minutes and seconds
     */
    COORDINATE_DEGREES_MINUTES_SECONDS_LATITUDE(UnitCategory.COORDINATE, 1, ""),
    /**
     * VOLT
     */
    VOLT(UnitCategory.VOLTAGE, 1, "V"),
    /**
     * Kilo VOLT
     */
    KILO_VOLT(UnitCategory.VOLTAGE, 1000, "KV"),
    /**
     * Milli VOLT
     */
    MILLI_VOLT(UnitCategory.VOLTAGE, 0.001, "mV"),
    /**
     * AMPERE
     */
    AMPERE(UnitCategory.ELECTRIC_CURRENT, 1, "A"),
    /**
     * Kilo AMPERE
     */
    KILO_AMPERE(UnitCategory.ELECTRIC_CURRENT, 1000, "KA"),
    /**
     * Milli AMPERE
     */
    MILLI_AMPERE(UnitCategory.ELECTRIC_CURRENT, 0.001, "mA"),
    /**
     * WATT
     */
    WATT(UnitCategory.ELECTRIC_POWER, 1, "W"),
    /**
     * Kilo WATT
     */
    KILO_WATT(UnitCategory.ELECTRIC_POWER, 1000, "KW"),
    /**
     * Milli WATT
     */
    MILLI_WATT(UnitCategory.ELECTRIC_POWER, 0.001, "mW"),
    /**
     * Degrees per Second
     */
    DEGREES_PER_SECOND(UnitCategory.RATE_OF_TURN, 1, "Deg/Sec"),
    /**
     * Degrees per Minute
     */
    DEGREES_PER_MINUTE(UnitCategory.RATE_OF_TURN, 1.0/60.0, "Deg/Min"),
    /**
     * Radians per Second
     */
    RADIANS_PER_SECOND(UnitCategory.RATE_OF_TURN, 180, "Rad/Sec"),
    /**
     * UNITLESS
     */
    UNITLESS(UnitCategory.UNKNOWN, 1, "")
    ;
    private final UnitCategory category;
    private final String unit;
    private DoubleUnaryOperator fromSI;
    private DoubleUnaryOperator toSI;

    private UnitType(UnitCategory category, double multiplier, String unit)
    {
        this(category, unit, (double v)->{ return v/multiplier;}, (double v)->{ return v*multiplier;});
    }

    private UnitType(UnitCategory category, double multiplier, double offset, String unit)
    {
        this(category, unit, (double v)->{ return v/multiplier-offset;}, (double v)->{ return v*multiplier+offset;});
    }

    private UnitType(UnitCategory category, String unit, DoubleUnaryOperator fromSI, DoubleUnaryOperator toSI)
    {
        this.category = category;
        this.unit = unit;
        this.fromSI = fromSI;
        this.toSI = toSI;
    }
    /**
     * Converts value from this unit to to unit
     * @param value
     * @param to
     * @return 
     */
    public double convertTo(double value, UnitType to)
    {
        if (equals(to))
        {
            return value;
        }
        check(this, to);
        return to.fromSI.applyAsDouble(toSI.applyAsDouble(value));
    }
    /**
     * Converts value from from unit to to unit.
     * @param value
     * @param from
     * @param to
     * @return 
     */
    public static double convert(double value, UnitType from, UnitType to)
    {
        return from.convertTo(value, to);
    }
    
    private static void check(UnitType from, UnitType to)
    {
        if (!from.category.equals(to.getCategory()))
        {
            throw new IllegalArgumentException(from+" cannot be converted to "+to);
        }
    }
    /**
     * Returns category
     * @return 
     */
    public UnitCategory getCategory()
    {
        return category;
    }
    /**
     * Returns display unit
     * @return 
     */
    public String getUnit()
    {
        return unit;
    }
    /**
     * Parses text.
     * <p>If text contains unit string of one of same category UnitTypes it is
     * used to convert value to this UnitType.
     * <p>Example: DURATION_MILLI_SECONDS.parse("2 s") returns 2000
     * @param text
     * @return 
     */
    public double parse(CharSequence text)
    {
        double d = Primitives.findDouble(text);
        for (UnitType u : values())
        {
            if (u.category == category)
            {
                if (CharSequences.indexOf(text, u.unit) > 0)
                {
                    return u.convertTo(d, this);
                }
            }
        }
        return d;
    }
}
