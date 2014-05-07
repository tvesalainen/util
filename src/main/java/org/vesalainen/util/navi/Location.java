/*
 * Location.java
 *
 * Created on 28. marraskuuta 2004, 11:34
 */

package org.vesalainen.util.navi;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.ParsePosition;

/**
 *
 * @author  tkv
 */
public class Location extends Point2D.Double
{
    public static final double PI2 = 2 * Math.PI;
    private static final DecimalFormat MINUTEFORMATWITHDECIMAL = new DecimalFormat("00.00");
    private static final DecimalFormat MINYTEFORMATWITHOUTDECIMAL = new DecimalFormat("00");
    private static final DecimalFormat DEGREEFORMAT = new DecimalFormat("000");

    /** Creates a new instance of Location */
    public Location()
    {
        super(0, 0);
    }

    /** Creates a new instance of Location */
    public Location(double latitude, double longitude)
    {
        super(longitude, latitude);
    }
    
    public Location(Point2D point)
    {
        super(point.getX(), point.getY());
    }
    
    public Location(String loc)
    {
        try
        {
            boolean latitudeOk = false;
            boolean longitudeOk = false;
            ParsePosition pos = new ParsePosition(0);
            Coordinate coordinate = Coordinate.lookingAt(loc, pos);
            switch (coordinate.getType())
            {
                case LATITUDE:
                    y = coordinate.getCoordinate();
                    latitudeOk = true;
                    break;
                case LONGITUDE:
                    x = coordinate.getCoordinate();
                    longitudeOk = true;
                    break;
                default:
                    throw new IllegalArgumentException(loc+" cardinal direction unknown");
            }
            coordinate = Coordinate.lookingAt(loc, pos);
            switch (coordinate.getType())
            {
                case LATITUDE:
                    y = coordinate.getCoordinate();
                    latitudeOk = true;
                    break;
                case LONGITUDE:
                    x = coordinate.getCoordinate();
                    longitudeOk = true;
                    break;
                default:
                    throw new IllegalArgumentException(loc+" cardinal direction unknown");
            }
            if (!latitudeOk || !longitudeOk)
            {
                throw new IllegalArgumentException(loc+" cardinal direction(s) unknown");
            }
        } 
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /** Creates a new instance of Location */
    public Location(String latitude, String longitude)
    {
        try
        {
            Coordinate coordinate = Coordinate.match(latitude);
            if (Coordinate.Type.LATITUDE.equals(coordinate.getType()))
            {
                y = coordinate.getCoordinate();
            }
            else
            {
                throw new IllegalArgumentException(latitude);
            }
            coordinate = Coordinate.match(longitude);
            if (Coordinate.Type.LONGITUDE.equals(coordinate.getType()))
            {
                x = coordinate.getCoordinate();
            }
            else
            {
                throw new IllegalArgumentException(longitude);
            }
        } 
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /** Creates a new instance of Location */
    public static Location getInstance(String latitude, String longitude)
    {
        return new Location(latitude, longitude);
    }
    
    public double getLatitude()
    {
        return y;
    }
    
    public double getLongitude()
    {
        return x;
    }
    
    public Location copy()
    {
        Location loc = new Location(y, x);
        return loc;
    }

    public Location move(Motion motion, TimeSpan timeSpan)
    {
        Location loc = copy();
        loc.moveIt(motion, timeSpan);
        return loc;
    }
    
    private void moveIt(Motion motion, TimeSpan timeSpan)
    {
        moveIt(motion.getAngle(), motion.getSpeed(), timeSpan);
    }
    
    /**
     * @param bearing 
     * @param speed 
     * @param timeSpan
     */
    public Location move(Angle bearing, Velocity speed, TimeSpan timeSpan)
    {
        Location loc = copy();
        loc.moveIt(bearing, speed, timeSpan);
        return loc;
    }
    /**
     * @param bearing 
     * @param speed 
     * @param timeSpan
     */
    private void moveIt(Angle bearing, Velocity speed, TimeSpan timeSpan)
    {
        moveIt(bearing, speed.getDistance(timeSpan));
    }
    
    /**
     * @param bearing
     * @param distance
     */
    public Location move(Angle bearing, Distance distance)
    {
        Location loc = copy();
        loc.moveIt(bearing, distance);
        return loc;
    }
    /**
     * @param bearing
     * @param distance
     */
    private void moveIt(Angle bearing, Distance distance)
    {
        Location ll = copy();
        y += bearing.cos()*distance.getMiles()/60.;
        double dep = departure(this, ll);
        x += bearing.sin()*distance.getMiles()/(60.*dep);
    }
    /**
     * 
     * @return Departure of location. (cos(latitude))
     */
    public double departure()
    {
        return departure(this);
    }
    /**
     * 
     * @param location
     * @return Departure of location. (cos(latitude))
     */
    public static double departure(Location location)
    {
        return Math.cos(Math.toRadians(location.getLatitude()));
    }
    /**
     * 
     * @param loc1
     * @param loc2
     * @return Mean departure of two locations
     * @see departure
     */
    public static double departure(Location loc1, Location loc2)
    {
        return Math.cos(Math.toRadians((loc2.getLatitude()+loc1.getLatitude())/2));
    }

    /**
     * @return bearing to loc
     */
    public Angle bearing(Location loc)
    {
        return bearing(this, loc);
    }
    /**
     * @return bearing from loc1 to loc2
     */
    public static Angle bearing(Location loc1, Location loc2)
    {
        double dep = departure(loc1, loc2);
        double aa = dep*(loc2.getLongitude()-loc1.getLongitude());
        double bb = loc2.getLatitude()-loc1.getLatitude();
        double dd = Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return new Angle(dd);
    }
    
    /**
     * @return distance to loc
     */
    public Distance distance(Location loc)
    {
        return distance(this, loc);
    }
    
    /**
     * @return distance from loc1 to loc2
     */
    public static Distance distance(Location loc1, Location loc2)
    {
        double dep = departure(loc1, loc2);
        return new Miles(60*Math.sqrt(Math.pow(loc1.getLatitude()-loc2.getLatitude(),2)+Math.pow(dep*(loc1.getLongitude()-loc2.getLongitude()),2)));
    }
    /**
     * Calcúlates the center point of the list of locations.
     * @param location
     * @return
     */
    public static Location center(Location... location)
    {
        double lat = 0;
        double lon = 0;
        double height = 0;
        for (int ii=0;ii<location.length;ii++)
        {
            lat += location[ii].getLatitude();
            lon += location[ii].getLongitude();
        }
        Location loc = new Location(lat/location.length, lon/location.length);
        return loc;
    }
    
    public String getNMEALongitude()
    {
        String ss = getNMEAString(x);
        return "0"+ss.substring(0, 7);
    }
    
    public String getNMEALatitude()
    {
        String ss = getNMEAString(y);
        return ss.substring(0, 7);
    }
    /**
     * 
     * @return W or E depending on which part of the world the location is
     */
    public String getLongitudeWE()
    {
        return Coordinate.getWhere(x, Coordinate.Type.LONGITUDE);
    }
    /**
     * 
     * @return N or S depending on which part of the world the location is
     */
    public String getLatitudeNS()
    {
        return Coordinate.getWhere(y, Coordinate.Type.LATITUDE);
    }
    
    @Override
    public boolean equals(Object ob)
    {
        if (ob instanceof Location)
        {
            Location loc = (Location) ob;
            return (Math.abs(x-loc.x) < 0.0000001 && Math.abs(y-loc.y) < 0.0000001);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
    @Override
    public String toString()
    {
        return getLatitudeString()+" "+getLongitudeString();
    }
    /**
     * 
     * @return
     * @deprecated Use getLatitudeString
     */
    public String latitudeString()
    {
        return getLatitudeString();
    }
    /**
     * 
     * @return
     * @deprecated Use getLongitudeString
     */
    public String longitudeString()
    {
        return getLongitudeString();
    }
    /**
     * 
     * @return Latitude part of Location
     */
    public String getLatitudeString()
    {
        return Coordinate.toDegMin(y, Coordinate.Type.LATITUDE);
    }
    /**
     * 
     * @return Longitude part of Location
     */
    public String getLongitudeString()
    {
        return Coordinate.toDegMin(x, Coordinate.Type.LONGITUDE);
    }
    
    private static String getNMEAString(double coord)
    {
        double co = Math.abs(coord);
        double dd;
        int c1 = (int)co;
        dd = co-c1;
        double c2 = dd*60;
        String ss = java.lang.Double.toString(c2);
        int idx = ss.indexOf('.');
        if (idx < 2)
        {
            ss = "0"+ss;
        }
        while (ss.length() < 8) // TO DO
        {
            ss += "0";
        }
        return c1+ss.substring(0,8);
    }
    /**
     *
     * @param current
     * @param portBuoy
     * @param starboardBuoy
     * @return
     * @see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
     */
    public static final Distance distanceToStartLine(Location current, Location portBuoy, Location starboardBuoy)
    {
        double dep = departure(portBuoy, starboardBuoy);
        double x0 = current.getLongitude()*dep;
        double y0 = current.getLatitude();
        double x1 = portBuoy.getLongitude()*dep;
        double y1 = portBuoy.getLatitude();
        double x2 = starboardBuoy.getLongitude()*dep;
        double y2 = starboardBuoy.getLatitude();

        double d = ((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/Math.sqrt(Math.pow((x2-x1), 2)+Math.pow((y2-y1), 2));
        return new Miles(60*d);
    }

    private static double angleBetween(double a1, double a2)
    {
        double dd = normalize(a2 - a1);
        if (dd > Math.PI)
        {
            return PI2 - dd;
        }
        return dd;
    }

    private static double reverse(double grad)
    {
        return normalize(grad + Math.PI);
    }

    private static boolean clockwise(double a1, double a2)
    {
        double dd = Math.abs(a2 - a1);
        if (dd > Math.PI)
        {
            return a2 < a1;
        }
        else
        {
            return a2 > a1;
        }
    }

    private static double normalize(double grad)
    {
        return (PI2 + grad) % PI2;
    }

    public static void main(String[] args)
    {
        try
        {
            Location pb = new Location("61N25E");
            Location sb = new Location(60,25);
            Location cu = new Location(60,25);
            System.err.println(Location.distanceToStartLine(cu, pb, sb));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
