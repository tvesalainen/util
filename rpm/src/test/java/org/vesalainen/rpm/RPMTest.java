/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.rpm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.vesalainen.nio.FileUtil;

/**
 *
 * @author tkv
 */
public class RPMTest
{
    static final Path LOCAL = new File("localTest").toPath();
    
    public RPMTest()
    {
    }

    @Before
    public void before() throws IOException
    {
        Files.createDirectories(LOCAL);
    }
    @After
    public void after() throws IOException
    {
        System.gc();
        FileUtil.deleteDirectory(LOCAL);
    }
    @Test
    public void testBuild() throws IOException, NoSuchAlgorithmException
    {
        RPMBuilder builder = new RPMBuilder()
                .setName("test2")
                .setVersion("1.0")
                .setRelease("r1")
                .setArch("noarch")
                .setDescription("description...")
                .setGroup("Test/Test")
                .setLicense("GPL")
                .setOs("linux")
                .setSummary("summary...")
                .addRequire("lsb")
                .addRequire("java7-runtime-headless")
                .setPostIn("echo qwerty >/tmp/test\n")
                ;
        
        builder.addFile(Paths.get("pom.xml"), "/opt/org.vesalainen/foo/pom.xml")
                .setMode("-rwxr--r--")
                .build();
        
        Path rpmFile = builder.build(LOCAL);
        Path z = Paths.get("Z:");   // TODO REMOVE!!!!!!!!
        Path target = z.resolve(rpmFile.getFileName());
        Files.copy(rpmFile, target, REPLACE_EXISTING);
        try (   RPM rpm2 = new RPM())
        {
            Path path = LOCAL.resolve("lsb-test1-1.0-r1.rpm");
            long size = Files.size(path);
            ByteBuffer bb = ByteBuffer.allocate((int) size);
            byte[] buf = Files.readAllBytes(path);
            ByteBuffer exp = ByteBuffer.wrap(buf);
            rpm2.load(path);
            // lead
            assertArrayEquals(RPM.LEAD_MAGIC, rpm2.lead.magic);
            assertEquals(3, rpm2.lead.major);
            assertEquals(0, rpm2.lead.minor);
            assertEquals(0, rpm2.lead.type);
            //assertEquals(1, rpm.archnum);
            //assertEquals("lsb-4.0-3mdv2010.1", rpm.name);
            assertEquals(1, rpm2.lead.osnum);
            assertEquals(5, rpm2.lead.signatureType);
            // header
            assertArrayEquals(RPM.HEADER_MAGIC, rpm2.signature.magic);
            rpm2.append(System.err);
            rpm2.save(bb);
            assertSame(exp, bb);
        }
    }
    //@Test
    public void test1() throws IOException, URISyntaxException, NoSuchAlgorithmException
    {
        URL url = RPMTest.class.getResource("/redhat-lsb-4.0-2.1.4.el5.i386.rpm");
        //
        //
        try (   RPM rpm = new RPM())
        {
            Path path = Paths.get(url.toURI());
            long size = Files.size(path);
            ByteBuffer bb = ByteBuffer.allocate((int) size);
            byte[] buf = Files.readAllBytes(path);
            ByteBuffer exp = ByteBuffer.wrap(buf);
            rpm.load(path);
            // lead
            assertArrayEquals(RPM.LEAD_MAGIC, rpm.lead.magic);
            assertEquals(3, rpm.lead.major);
            assertEquals(0, rpm.lead.minor);
            assertEquals(0, rpm.lead.type);
            //assertEquals(1, rpm.archnum);
            //assertEquals("lsb-4.0-3mdv2010.1", rpm.name);
            assertEquals(1, rpm.lead.osnum);
            assertEquals(5, rpm.lead.signatureType);
            // header
            assertArrayEquals(RPM.HEADER_MAGIC, rpm.signature.magic);
            rpm.append(System.err);
            rpm.save(bb);
            //assertSame(exp, bb);
        }
    }
    
    private void assertSame(ByteBuffer b1, ByteBuffer b2)
    {
        int len = b2.position();
        for (int ii=0;ii<len;ii++)
        {
            byte c1 = b1.get(ii);
            byte c2 = b2.get(ii);
            if (c1 != c2)
            {
                throw new AssertionError(c1+" != "+c2+" at position "+ii);
            }
        }
    }
}
