/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class LocationTest
{

    public LocationTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class Location.
     */
    @Test
    public void testGetInstance() throws Exception
    {
        System.out.println("getInstance");
        String latitude = "65\u00B0N";
        String longitude = "25\u00B0E";
        Location expResult = new Location(65, 25);
        Location result = Location.getInstance(latitude, longitude);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLatitude method, of class Location.
     */
    @Test
    public void testGetLatitude()
    {
        System.out.println("getLatitude");
        Location instance = new Location();
        double expResult = 0.0;
        double result = instance.getLatitude();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getLongitude method, of class Location.
     */
    @Test
    public void testGetLongitude()
    {
        System.out.println("getLongitude");
        Location instance = new Location();
        double expResult = 0.0;
        double result = instance.getLongitude();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of copy method, of class Location.
     */
    @Test
    public void testCopy()
    {
        System.out.println("copy");
        Location instance = new Location();
        Location expResult = instance;
        Location result = instance.copy();
        assertEquals(expResult, result);
    }

    /**
     * Test of move method, of class Location.
     */
    @Test
    public void testMove_Motion_TimeSpan()
    {
        System.out.println("move");
        Motion motion = new Motion(new Knots(60), new Degree(0));
        TimeSpan timeSpan = new TimeSpan(1, TimeUnit.HOURS);
        Location instance = new Location();
        Location expResult = new Location(1,0);
        Location result = instance.move(motion, timeSpan);
        assertEquals(expResult, result);
    }

    /**
     * Test of move method, of class Location.
     */
    @Test
    public void testMove_3args()
    {
        System.out.println("move");
        Angle bearing = new Degree(90);
        Velocity speed = new Knots(60);
        TimeSpan timeSpan = new TimeSpan(30, TimeUnit.MINUTES);
        Location instance = new Location(60,0);
        Location expResult = new Location(60,1);
        Location result = instance.move(bearing, speed, timeSpan);
        assertEquals(expResult, result);
    }

    /**
     * Test of move method, of class Location.
     */
    @Test
    public void testMove_Angle_Distance()
    {
        System.out.println("move");
        Angle bearing = new Degree(270);
        Distance distance = new Miles(60);
        Location instance = new Location(60,2);
        Location expResult = new Location(60,0);
        Location result = instance.move(bearing, distance);
        assertEquals(expResult, result);
    }

    /**
     * Test of departure method, of class Location.
     */
    @Test
    public void testDeparture_0args()
    {
        System.out.println("departure");
        Location instance = new Location(60,123);
        double expResult = 0.5;
        double result = instance.departure();
        assertEquals(expResult, result, 0.0000001);
    }

    /**
     * Test of departure method, of class Location.
     */
    @Test
    public void testDeparture_Location()
    {
        System.out.println("departure");
        Location location = new Location(60,123);
        double expResult = 0.5;
        double result = Location.departure(location);
        assertEquals(expResult, result, 0.0000001);
    }

    /**
     * Test of departure method, of class Location.
     */
    @Test
    public void testDeparture_Location_Location()
    {
        System.out.println("departure");
        Location loc1 = new Location(61,123);
        Location loc2 = new Location(59,22);
        double expResult = 0.5;
        double result = Location.departure(loc1, loc2);
        assertEquals(expResult, result, 0.00000001);
    }

    /**
     * Test of bearing method, of class Location.
     */
    @Test
    public void testBearing_Location()
    {
        System.out.println("bearing");
        Location loc = new Location(1,1);
        Location instance = new Location();
        Angle expResult = new Degree(45);
        Angle result = instance.bearing(loc);
        assertEquals(expResult, result);
    }

    /**
     * Test of bearing method, of class Location.
     */
    @Test
    public void testBearing_Location_Location()
    {
        System.out.println("bearing");
        Location loc1 = new Location(0,0);
        Location loc2 = new Location(1,-1);
        Angle expResult = new Degree(315);
        Angle result = Location.bearing(loc1, loc2);
        assertEquals(expResult, result);
    }

    /**
     * Test of distance method, of class Location.
     */
    @Test
    public void testDistance_Location()
    {
        System.out.println("distance");
        Location loc = new Location(1,0);
        Location instance = new Location();
        Distance expResult = new Miles(60);
        Distance result = instance.distance(loc);
        assertEquals(expResult, result);
    }

    /**
     * Test of distance method, of class Location.
     */
    @Test
    public void testDistance_Location_Location()
    {
        System.out.println("distance");
        Location loc1 = new Location();
        Location loc2 = new Location(0,1);
        Distance expResult = new Miles(60);
        Distance result = Location.distance(loc1, loc2);
        assertEquals(expResult, result);
    }

    /**
     * Test of center method, of class Location.
     */
    @Test
    public void testCenter()
    {
        System.out.println("center");
        Location[] location = {new Location(-2,2),new Location(2,2),new Location(2,-2),new Location(-2,-2)};
        Location expResult = new Location();
        Location result = Location.center(location);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNMEALongitude method, of class Location.
     */
    @Test
    public void testGetNMEALongitude()
    {
        System.out.println("getNMEALongitude");
        Location instance = new Location();
        String expResult = "0000.000";
        String result = instance.getNMEALongitude();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNMEALatitude method, of class Location.
     */
    @Test
    public void testGetNMEALatitude()
    {
        System.out.println("getNMEALatitude");
        Location instance = new Location();
        String expResult = "000.000";
        String result = instance.getNMEALatitude();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLongitudeWE method, of class Location.
     */
    @Test
    public void testGetLongitudeWE()
    {
        System.out.println("getLongitudeWE");
        Location instance = new Location(0,1);
        String expResult = "E";
        String result = instance.getLongitudeWE();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLatitudeNS method, of class Location.
     */
    @Test
    public void testGetLatitudeNS()
    {
        System.out.println("getLatitudeNS");
        Location instance = new Location(1,0);
        String expResult = "N";
        String result = instance.getLatitudeNS();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Location.
     */
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        Object ob = null;
        Location instance = new Location();
        boolean expResult = false;
        boolean result = instance.equals(ob);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLatitudeString method, of class Location.
     */
    @Test
    public void testGetLatitudeString()
    {
        System.out.println("getLatitudeString");
        Location instance = new Location(65,25);
        String expResult = "65\u00B00.00'N";
        String result = instance.getLatitudeString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLongitudeString method, of class Location.
     */
    @Test
    public void testGetLongitudeString()
    {
        System.out.println("getLongitudeString");
        Location instance = new Location(65,25);
        String expResult = "025\u00B00.00'E";
        String result = instance.getLongitudeString();
        assertEquals(expResult, result);
    }

    /**
     * Test of distanceToStartLine method, of class Location.
     */
    @Test
    public void testDistanceToStartLine()
    {
        System.out.println("distanceToStartLine");
        Location current = new Location(-1,0);
        Location portBuoy = new Location(0,-1);
        Location starboardBuoy = new Location(0,1);
        Distance expResult = new Miles(60);
        Distance result = Location.distanceToStartLine(current, portBuoy, starboardBuoy);
        assertEquals(expResult, result);
    }

    @Test
    public void testRateOfTurnMove1()
    {
        System.out.println("rateOfTurnMove1");
        Velocity velocity = new Knots(1);
        RateOfTurn instance = new DegreesPerMinute(6);
        Location loc = new Location();
        Angle bearing = new Angle();
        TimeSpan timeSpan = new TimeSpan(30, TimeUnit.MINUTES);
        Location moved = loc.move(bearing, velocity, instance, timeSpan);
        Distance expResult = new Miles(2/Math.PI);
        Distance result = moved.distance(loc);
        assertEquals(expResult, result);
        Angle bearing1 = loc.bearing(moved);
        assertEquals(new Degree(90), bearing1);
    }

    @Test
    public void testRateOfTurnMove2()
    {
        System.out.println("rateOfTurnMove2");
        Velocity velocity = new Knots(1);
        RateOfTurn instance = new DegreesPerMinute(-6);
        Location loc = new Location();
        Angle bearing = new Angle();
        TimeSpan timeSpan = new TimeSpan(30, TimeUnit.MINUTES);
        Location moved = loc.move(bearing, velocity, instance, timeSpan);
        Distance expResult = new Miles(2/Math.PI);
        Distance result = moved.distance(loc);
        assertEquals(expResult, result);
        Angle bearing1 = loc.bearing(moved);
        assertEquals(new Degree(270), bearing1);
    }

    @Test
    public void testRateOfTurnMove3()
    {
        System.out.println("rateOfTurnMove3");
        Velocity velocity = new Knots(1);
        RateOfTurn instance = new DegreesPerMinute(6);
        Location loc = new Location();
        Angle bearing = new Angle();
        TimeSpan timeSpan = new TimeSpan(15, TimeUnit.MINUTES);
        Location moved = loc.move(bearing, velocity, instance, timeSpan);
        Distance expResult = new Miles(Math.hypot(1/Math.PI, 1/Math.PI));
        Distance result = moved.distance(loc);
        assertEquals(expResult, result);
        Angle bearing1 = loc.bearing(moved);
        assertEquals(new Degree(45), bearing1);
    }

    @Test
    public void testRateOfTurnMove4()
    {
        System.out.println("rateOfTurnMove4");
        Velocity velocity = new Knots(1);
        RateOfTurn instance = new DegreesPerMinute(-6);
        Location loc = new Location();
        Angle bearing = new Angle();
        TimeSpan timeSpan = new TimeSpan(15, TimeUnit.MINUTES);
        Location moved = loc.move(bearing, velocity, instance, timeSpan);
        Distance expResult = new Miles(Math.hypot(1/Math.PI, 1/Math.PI));
        Distance result = moved.distance(loc);
        assertEquals(expResult, result);
        Angle bearing1 = loc.bearing(moved);
        assertEquals(new Degree(315), bearing1);
    }

}
