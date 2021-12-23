/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.maven.help;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VersionRangeTest
{
    
    public VersionRangeTest()
    {
    }

    @Test
    public void testInclusive1()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("[1.0,3.0]");
        assertFalse(vr.in("0.9"));
        assertTrue(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertTrue(vr.in("3.0"));
        assertFalse(vr.in("3.1"));
        assertEquals("[1.0,3.0]", vr.toString());
    }
    
    @Test
    public void testInclusive2()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("[1.0,]");
        assertFalse(vr.in("0.9"));
        assertTrue(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertTrue(vr.in("3.0"));
        assertTrue(vr.in("3.1"));
        assertEquals("[1.0,)", vr.toString());
    }
    
    @Test
    public void testInclusive3()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("[,3.0]");
        assertTrue(vr.in("0.9"));
        assertTrue(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertTrue(vr.in("3.0"));
        assertFalse(vr.in("3.1"));
        assertEquals("(,3.0]", vr.toString());
    }
    
    @Test
    public void testInclusive4()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("[3.0]");
        assertFalse(vr.in("0.9"));
        assertFalse(vr.in("1.0"));
        assertFalse(vr.in("1.1"));
        assertTrue(vr.in("3.0"));
        assertFalse(vr.in("3.1"));
        assertEquals("[3.0]", vr.toString());
    }
    
    @Test
    public void testExclusive1()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("(1.0,3.0)");
        assertFalse(vr.in("0.9"));
        assertFalse(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertFalse(vr.in("3.0"));
        assertFalse(vr.in("3.1"));
        assertEquals("(1.0,3.0)", vr.toString());
    }
    
    @Test
    public void testExclusive2()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("(1.0,)");
        assertFalse(vr.in("0.9"));
        assertFalse(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertTrue(vr.in("3.0"));
        assertTrue(vr.in("3.1"));
        assertEquals("(1.0,)", vr.toString());
    }
    
    @Test
    public void testExclusive3()
    {
        VersionParser vp = VersionParser.getInstance();
        VersionRange vr = vp.parseVersionRange("(,3.0)");
        assertTrue(vr.in("0.9"));
        assertTrue(vr.in("1.0"));
        assertTrue(vr.in("1.1"));
        assertFalse(vr.in("3.0"));
        assertFalse(vr.in("3.1"));
        assertEquals("(,3.0)", vr.toString());
    }
    
}
