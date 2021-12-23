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
public class VersionTest
{
    
    public VersionTest()
    {
    }

    @Test
    public void test1()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3");
        Version v2 = vp.parseVersion("2.2.3");
        assertTrue(v1.compareTo(v2) < 0);
        assertNotEquals(v1, v2);
    }
    
    @Test
    public void test2()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3");
        Version v2 = vp.parseVersion("1.3.3");
        assertTrue(v1.compareTo(v2) < 0);
        assertNotEquals(v1, v2);
    }
    
    @Test
    public void test3()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3");
        Version v2 = vp.parseVersion("1.2.4");
        assertTrue(v1.compareTo(v2) < 0);
        assertNotEquals(v1, v2);
    }
    
    @Test
    public void test4()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3-alpha-2");
        Version v2 = vp.parseVersion("1.2.3-beta-1");
        assertTrue(v1.compareTo(v2) < 0);
        assertNotEquals(v1, v2);
    }
    
    @Test
    public void test5()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3-beta-1");
        Version v2 = vp.parseVersion("1.2.3-beta-2");
        assertTrue(v1.compareTo(v2) < 0);
        assertNotEquals(v1, v2);
    }
    
    @Test
    public void test6()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3-beta-1");
        Version v2 = vp.parseVersion("1.2.3-beta-01");
        assertTrue(v1.compareTo(v2) == 0);
        assertEquals(v1, v2);
    }
    
    @Test
    public void test7()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3");
        Version v2 = vp.parseVersion("1.2.3-beta-01");
        assertTrue(v1.compareTo(v2) < 0);
    }
    
    @Test
    public void test8()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("1.2.3-beta-1");
        Version v2 = vp.parseVersion("1.2.3");
        assertTrue(v1.compareTo(v2) > 0);
    }
    
    @Test
    public void test9()
    {
        VersionParser vp = VersionParser.getInstance();
        Version v1 = vp.parseVersion("9.4.35.v20201120");
        Version v2 = vp.parseVersion("1.2.3");
        assertTrue(v1.compareTo(v2) > 0);
    }
    
}
