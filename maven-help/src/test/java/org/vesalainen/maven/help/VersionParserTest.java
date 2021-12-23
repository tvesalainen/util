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
public class VersionParserTest
{
    
    public VersionParserTest()
    {
    }

    @Test
    public void test1()
    {
        VersionParser vp = VersionParser.getInstance();
        assertNotNull(vp);
        SimpleVersion v1 = (SimpleVersion) vp.parseVersion("1.2.3");
        assertEquals(1, v1.getMajor());
        assertEquals(2, v1.getMinor());
        assertEquals(3, v1.getIncremental());
    }
    
    @Test
    public void test2()
    {
        VersionParser vp = VersionParser.getInstance();
        assertNotNull(vp);
        SimpleVersion v1 = (SimpleVersion) vp.parseVersion("1.2.3-20170215-1527-1");
        assertEquals(1, v1.getMajor());
        assertEquals(2, v1.getMinor());
        assertEquals(3, v1.getIncremental());
        assertEquals("20170215-1527", v1.getQualifier());
        assertEquals(1, v1.getBuildNumber());
    }
    
    @Test
    public void test3()
    {
        VersionParser vp = VersionParser.getInstance();
        assertNotNull(vp);
        SimpleVersion v1 = (SimpleVersion) vp.parseVersion("1.2-20170215-1527-1");
        assertEquals(1, v1.getMajor());
        assertEquals(2, v1.getMinor());
        assertEquals(-1, v1.getIncremental());
        assertEquals("20170215-1527", v1.getQualifier());
        assertEquals(1, v1.getBuildNumber());
    }
    
    @Test
    public void test4()
    {
        VersionParser vp = VersionParser.getInstance();
        assertNotNull(vp);
        SimpleVersion v1 = (SimpleVersion) vp.parseVersion("1-20170215-1527-1");
        assertEquals(1, v1.getMajor());
        assertEquals(-1, v1.getMinor());
        assertEquals(-1, v1.getIncremental());
        assertEquals("20170215-1527", v1.getQualifier());
        assertEquals(1, v1.getBuildNumber());
    }
    
    @Test
    public void test5()
    {
        VersionParser vp = VersionParser.getInstance();
        assertNotNull(vp);
        SimpleVersion v1 = (SimpleVersion) vp.parseVersion("1-201702151527");
        assertEquals(1, v1.getMajor());
        assertEquals(-1, v1.getMinor());
        assertEquals(-1, v1.getIncremental());
        assertEquals("201702151527", v1.getQualifier());
        assertEquals(-1, v1.getBuildNumber());
    }
    
}
