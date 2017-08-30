/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.file.attribute.UserAttrs;
import org.vesalainen.regex.Regex;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl.BasicFileAttributesImpl;
import org.vesalainen.vfs.attributes.PosixFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.PosixFileAttributeViewImpl.PosixFileAttributesImpl;
import org.vesalainen.vfs.unix.UnixFileAttributeView;
import org.vesalainen.vfs.unix.UnixFileAttributeViewImpl;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystemProviderTest
{
    FileSystem fileSystem;
    public VirtualFileSystemProviderTest() throws URISyntaxException, IOException
    {
        fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.vfs:///", null, null));
        Files.createDirectories(fileSystem.getPath("/etc/default/java"));
        Files.createDirectories(fileSystem.getPath("/usr/local/bin"));
        Files.createDirectories(fileSystem.getPath("/usr/local/lib"));
        Files.createDirectories(fileSystem.getPath("/bin"));
        Files.createDirectories(fileSystem.getPath("/var/log"));
        Files.createDirectories(fileSystem.getPath("/tmp"));
        Files.createDirectories(fileSystem.getPath("/home/timo"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/java"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/jar"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/README"));
        Files.createFile(fileSystem.getPath("/bin/bash"));
        Files.createFile(fileSystem.getPath("/bin/sh"));
        Files.createFile(fileSystem.getPath("/bin/pg"));
        Files.createFile(fileSystem.getPath("/home/timo/hello.c"));
        Files.createFile(fileSystem.getPath("/home/timo/hello.o"));
        Files.createFile(fileSystem.getPath("/home/timo/hello"));
    }

    @Test
    public void test1() throws URISyntaxException, IOException
    {
        Path cr = Paths.get("c:\\temp");
        Path temp = Paths.get("temp");
        Path parent = cr.getParent();
        int nc1 = cr.getNameCount();
        Path fn = cr.getFileName();
        Path root = cr.getRoot();
        int nc2 = root.getNameCount();
        URI uri = new URI("file:///");
        Path d = Paths.get("d:\\");
        Set<String> supportedFileAttributeViews = d.getFileSystem().supportedFileAttributeViews();
    }

    @Test
    public void testCopy() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        assertEquals(Files.size(source), Files.size(target));
        List<String> lines = Files.readAllLines(target, US_ASCII);
        assertEquals(exp, lines);
        Path target2 = fileSystem.getPath("bar");
        Files.copy(target, target2);
        List<String> lines2 = Files.readAllLines(target2, US_ASCII);
        assertEquals(exp, lines2);
    }
    @Test
    public void testCopy2() throws IOException
    {
        Path target = fileSystem.getPath("/home/timo/trash/../../notes.txt");
        Path targetExp = fileSystem.getPath("/home/notes.txt");
        ByteArrayInputStream bais = new ByteArrayInputStream("spanish waters".getBytes());
        Files.copy(bais, target);
        assertEquals(14, Files.size(targetExp));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Files.copy(target, baos);
        assertEquals("spanish waters", baos.toString());
    }
    @Test
    public void testCreateDirectory() throws IOException
    {
        Path target = fileSystem.getPath("foo");
        Files.createDirectory(target);
        assertTrue(Files.exists(target));
        assertTrue(Files.isDirectory(target));
        Path bar = fileSystem.getPath("foo/bar");
        Files.createFile(bar);
        try
        {
            Files.delete(target);
            fail("DirectoryNotEmptyException");
        }
        catch(DirectoryNotEmptyException ex)
        {
        }
        Files.delete(bar);
        Files.delete(target);
        assertFalse(Files.exists(target));
    }
    @Test
    public void testCreate() throws URISyntaxException, IOException
    {
        Path target = fileSystem.getPath("foo/bar");
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        assertTrue(Files.exists(target));
        assertTrue(Files.isRegularFile(target));
        Files.delete(target);
        assertFalse(Files.exists(target));
    }
    @Test
    public void testCreateSymbolicLink() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path link = fileSystem.getPath("bar");
        Files.createSymbolicLink(link, target);
        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
        assertEquals(Files.size(source), Files.size(link));
        assertTrue(Files.isSymbolicLink(link));
    }
    @Test
    public void testCreateLink() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path link = fileSystem.getPath("bar");
        Files.createLink(link, target);
        assertEquals(Files.size(source), Files.size(link));
        assertTrue(Files.isSameFile(link, target));
        Files.deleteIfExists(target);
        assertFalse(Files.exists(target));
        assertTrue(Files.exists(link));
        List<String> lines = Files.readAllLines(link, US_ASCII);
        assertEquals(exp, lines);
    }
    @Test
    public void testMove() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path bar = fileSystem.getPath("bar");
        Files.move(target, bar);
        assertFalse(Files.exists(target));
        assertTrue(Files.exists(bar));
        assertEquals(Files.size(source), Files.size(bar));
    }
    @Test
    public void testDirectoryStream1() throws URISyntaxException, IOException
    {
        List<Path> list1 = Files.list(fileSystem.getPath("/usr/local/bin")).collect(Collectors.toList());
        assertEquals(3, list1.size());
        assertTrue(list1.contains(fileSystem.getPath("/usr/local/bin/README")));
        assertTrue(list1.contains(fileSystem.getPath("/usr/local/bin/jar")));
        assertTrue(list1.contains(fileSystem.getPath("/usr/local/bin/java")));
        
    }
    @Test
    public void testDirectoryStream2() throws URISyntaxException, IOException
    {
        DirectoryStream<Path> ds = Files.newDirectoryStream(fileSystem.getPath("/home/timo"), "*.c");
        Iterator<Path> iterator = ds.iterator();
        assertTrue(iterator.hasNext());
        while (iterator.hasNext())
        {
            assertEquals(fileSystem.getPath("/home/timo/hello.c"), iterator.next());
        }
    }
    @Test
    public void testDirectoryStream3() throws URISyntaxException, IOException
    {
        DirectoryStream<Path> ds = Files.newDirectoryStream(fileSystem.getPath("/usr/local/bin"), RegexPathMatcher.createFilter(".*read.*", Regex.Option.CASE_INSENSITIVE));
        Iterator<Path> iterator = ds.iterator();
        assertTrue(iterator.hasNext());
        while (iterator.hasNext())
        {
            assertEquals(fileSystem.getPath("/usr/local/bin/README"), iterator.next());
        }
    }
    @Test
    public void testUserDefinedAttributes() throws IOException
    {
        Path path = fileSystem.getPath("/usr/local/bin/README");
        UserAttrs.setShortAttribute(path, "user:short", (short)1234);
        assertEquals(1234, UserAttrs.getShortAttribute(path, "user:short"));
        UserAttrs.setIntAttribute(path, "user:int", 1234);
        assertEquals(1234, UserAttrs.getIntAttribute(path, "user:int"));
        UserAttrs.setLongAttribute(path, "user:long", 1234L);
        assertEquals(1234, UserAttrs.getLongAttribute(path, "user:long"));
        UserAttrs.setFloatAttribute(path, "user:float", (float)1234.56);
        assertEquals((float)1234.56, UserAttrs.getFloatAttribute(path, "user:float"), 1e-10);
        UserAttrs.setDoubleAttribute(path, "user:double", 1234.56);
        assertEquals(1234.56, UserAttrs.getDoubleAttribute(path, "user:double"), 1e-10);
        UserAttrs.setStringAttribute(path, "user:string", "qwerty");
        assertEquals("qwerty", UserAttrs.getStringAttribute(path, "user:string"));
    }
    @Test
    public void testCreateTempDirectory() throws IOException
    {
        Path tmp = fileSystem.getPath("/tmp");
        Path tempDirectory = Files.createTempDirectory(tmp, null);
        assertTrue(Files.exists(tempDirectory));
    }
    @Test
    public void testCreateTempFile() throws IOException
    {
        Path tmp = fileSystem.getPath("/tmp");
        Path tempFile = Files.createTempFile(tmp, null, null);
        assertTrue(Files.exists(tempFile));
    }
    @Test
    public void testFind() throws IOException
    {
        Path root = fileSystem.getPath("/");
        Stream<Path> stream = Files.find(root, 6, (p,b)->b.isDirectory());
        List<Path> list = stream.collect(Collectors.toList());
        assertTrue(list.size()> 0);
        assertTrue(list.stream().allMatch((p)->Files.isDirectory(p)));
    }
    @Test
    public void testReadAttributes() throws IOException
    {
        Path path = fileSystem.getPath("/usr/local/bin/README");
        BasicFileAttributes bfa = Files.readAttributes(path, BasicFileAttributes.class);
        assertTrue(bfa.getClass().equals(BasicFileAttributesImpl.class));
        PosixFileAttributes pfa = Files.readAttributes(path, PosixFileAttributes.class);
        assertTrue(pfa.getClass().equals(PosixFileAttributesImpl.class));
    }
    @Test
    public void testGetFileAttributeView() throws IOException
    {
        Path path = fileSystem.getPath("/usr/local/bin/README");
        BasicFileAttributeView bfav = Files.getFileAttributeView(path, BasicFileAttributeView.class);
        assertTrue(bfav.getClass().equals(BasicFileAttributeViewImpl.class));
        PosixFileAttributeView pfav = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        assertTrue(pfav.getClass().equals(PosixFileAttributeViewImpl.class));
        UnixFileAttributeView ufav = Files.getFileAttributeView(path, UnixFileAttributeView.class);
        assertTrue(ufav.getClass().equals(UnixFileAttributeViewImpl.class));
    }
    @Test
    public void testWalk() throws IOException
    {
        Path path = fileSystem.getPath("/");
        Stream<Path> stream = Files.walk(path);
        List<Path> list = stream.collect(Collectors.toList());
        assertTrue(list.size() > 10);
    }
    @Test
    public void testWrite() throws IOException
    {
        Path path = fileSystem.getPath("/home/timo/hello.c");
        Files.write(path, "hello".getBytes(US_ASCII));
        assertEquals(5, Files.size(path));
        Files.write(path, "hello".getBytes(US_ASCII), APPEND);
        assertEquals(10, Files.size(path));
        Files.write(path, "hello".getBytes(US_ASCII));
        assertEquals(5, Files.size(path));
    }
    @Test
    public void testChannel() throws IOException
    {
        byte[] exp = "0123456789".getBytes();
        ByteBuffer bb = ByteBuffer.wrap(exp);
        Path path = fileSystem.getPath("/home/timo/hello.o");
        Path path2 = fileSystem.getPath("/home/timo/file.tmp");
        try (   FileChannel ch = FileChannel.open(path, EnumSet.of(READ,WRITE));
                FileChannel ch2 = FileChannel.open(path2, EnumSet.of(READ,WRITE, CREATE)))
        {
            ch.write(bb);
            assertEquals(10, ch.position());
            bb.clear();
            int read = ch.read(bb, 5);
            assertEquals(5, read);
            int write = ch.write(ByteBuffer.wrap("asd".getBytes()), 3); // 012asd6789
            assertEquals(3, write);
            assertArrayEquals("012asd6789".getBytes(), Files.readAllBytes(path));
            bb.clear();
            ch.read(bb, 3);
            bb.flip();
            assertEquals('a', bb.get());
            ch.transferTo(3, 5, ch2);
            assertArrayEquals("asd67".getBytes(), Files.readAllBytes(path2));
            bb.clear();
            ch2.read(bb, 0);
            bb.flip();
            byte[] b1 = new byte[5];
            bb.get(b1);
            assertArrayEquals("asd67".getBytes(), b1);
            ch2.position(1);
            long tf = ch.transferFrom(ch2, 1, 3);
            assertEquals(3, tf);
            assertArrayEquals("0sd6sd6789".getBytes(), Files.readAllBytes(path));
        }
    }
 }
