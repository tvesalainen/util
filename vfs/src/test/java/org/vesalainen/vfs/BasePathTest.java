package org.vesalainen.vfs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */


public class BasePathTest
{
    private boolean defPath = false;
    FileSystem fs;
    
    public BasePathTest() throws URISyntaxException
    {
        if (defPath)
        {
            fs = FileSystems.getDefault();
        }
        else
        {
            fs = FileSystems.getFileSystem(new URI("org.vesalainen.vfs:///", null, null));
        }
    }

    private Path getPath(String first, String... more)
    {
        String f = first.replace("/", fs.getSeparator());
        if (f.startsWith("\\"))
        {
            f = "c:"+f;
        }
        String[] m = new String[more.length];
        for (int ii=0;ii<m.length;ii++)
        {
            m[ii] = more[ii].replace("/", fs.getSeparator());
        }
        return fs.getPath(f, m);
    }
    /**
     * Test of getFileSystem method, of class PMPath.
     */
    @Test
    public void testGetFileSystem()
    {
        Path path = fs.getPath("foo", "bar");
        assertEquals(fs, path.getFileSystem());
    }

    /**
     * Test of isAbsolute method, of class PMPath.
     */
    @Test
    public void testIsAbsolute()
    {
        Path p1 = getPath("foo", "bar");
        assertFalse(p1.isAbsolute());
        Path p2 = getPath("/foo", "bar");
        assertTrue(p2.isAbsolute());
    }

    /**
     * Test of getRoot method, of class PMPath.
     */
    @Test
    public void testGetRoot()
    {
        Path p1 = getPath("/foo", "bar");
        assertNotNull(p1.getRoot());
        Path p2 = getPath("foo", "bar");
        assertNull(p2.getRoot());
    }

    /**
     * Test of getFileName method, of class PMPath.
     */
    @Test
    public void testGetFileName()
    {
        assertEquals(getPath("foo"), getPath("/foo").getFileName());
        assertEquals(getPath("bar"), getPath("/foo/bar").getFileName());
        assertNull(getPath("/").getFileName());
    }

    /**
     * Test of getParent method, of class PMPath.
     */
    @Test
    public void testGetParent()
    {
        assertEquals(getPath("/"), getPath("/foo").getParent());
        assertEquals(getPath("/foo"), getPath("/foo/bar").getParent());
        assertNull(getPath("foo").getParent());
    }

    /**
     * Test of getNameCount method, of class PMPath.
     */
    @Test
    public void testGetNameCount()
    {
        assertEquals(2, getPath("/foo/bar").getNameCount());
        assertEquals(1, getPath("/foo/bar").getFileName().getNameCount());
        assertEquals(1, getPath("/foo").getNameCount());
    }

    /**
     * Test of getName method, of class PMPath.
     */
    @Test
    public void testGetName()
    {
        assertEquals(getPath("foo"), getPath("/foo/bar").getName(0));
        assertEquals(getPath("bar"), getPath("/foo/bar").getName(1));
    }

    /**
     * Test of subpath method, of class PMPath.
     */
    @Test
    public void testSubpath()
    {
        assertEquals(getPath("bar/goo"), getPath("/foo/bar/goo/bra").subpath(1, 3));
        assertEquals(getPath("foo/bar"), getPath("/foo/bar/goo/bra").subpath(0, 2));
        assertEquals(getPath("goo/bra"), getPath("/foo/bar/goo/bra").subpath(2, 4));
        assertFalse(getPath("/foo/bar/goo/bra").subpath(0, 2).isAbsolute());
    }

    /**
     * Test of startsWith method, of class PMPath.
     */
    @Test
    public void testStartsWith_String()
    {
        assertTrue(getPath("foo/bar/goo").startsWith("foo"));
        assertTrue(getPath("foo/bar/goo").startsWith("foo/bar"));
        assertTrue(getPath("foo/bar/goo").startsWith("foo/bar/goo"));
        assertFalse(getPath("/foo/bar/goo").startsWith("foo/bar/goo"));
        assertFalse(getPath("foo/bar/goo").startsWith("/foo/bar/goo"));
    }

    /**
     * Test of endsWith method, of class PMPath.
     */
    @Test
    public void testEndsWith_String()
    {
        assertTrue(getPath("foo/bar/goo").endsWith("goo"));
        assertTrue(getPath("foo/bar/goo").endsWith("bar/goo"));
        assertTrue(getPath("foo/bar/goo").endsWith("foo/bar/goo"));
        assertTrue(getPath("/foo/bar/goo").endsWith("foo/bar/goo"));
        assertFalse(getPath("/foo/bar").endsWith("/foo/bar/goo"));
    }

    /**
     * Test of normalize method, of class PMPath.
     */
    @Test
    public void testNormalize()
    {
        assertEquals(getPath("foo"), getPath("./foo").normalize());
        assertEquals(getPath("/foo"), getPath("/./foo").normalize());
        assertEquals(getPath("bar/foo"), getPath("bar/./foo").normalize());
        assertEquals(getPath("/bar/foo"), getPath("/bar/./foo").normalize());
        assertEquals(getPath("foo"), getPath("bar/../foo").normalize());
        assertEquals(getPath("/foo"), getPath("/bar/../foo").normalize());
        assertEquals(getPath("../foo"), getPath("../foo").normalize());
        assertEquals(getPath("bar/foo"), getPath("bar/goo/bra/../../foo").normalize());
    }

    /**
     * Test of resolve method, of class PMPath.
     */
    @Test
    public void testResolve_String()
    {
        assertEquals(getPath("/foo"), getPath("/foo").resolve(""));
        assertEquals(getPath("/bar"), getPath("/foo").resolve("/bar"));
        assertEquals(getPath("foo/bar"), getPath("foo").resolve("bar"));
        assertEquals(getPath("/foo/bar"), getPath("/foo").resolve("bar"));
    }

    /**
     * Test of resolveSibling method, of class PMPath.
     */
    @Test
    public void testResolveSibling_String()
    {
        assertEquals(getPath("/foo/goo"), getPath("/foo/bar").resolveSibling("goo"));
    }

    /**
     * Test of relativize method, of class PMPath.
     */
    @Test
    public void testRelativize()
    {
        assertEquals(getPath("goo"), getPath("/foo/bar").relativize(getPath("/foo/bar/goo")));
        assertEquals(getPath("goo"), getPath("foo/bar").relativize(getPath("foo/bar/goo")));
    }

    @Test
    public void testCompare()
    {
        List<Path> list = new ArrayList<>();
        list.add(getPath("/"));
        list.add(getPath("/foo"));
        list.add(getPath("/foo/bar"));
        list.add(getPath("/foo/bar/goo"));
        list.add(getPath("foo"));
        List<Path> exp = new ArrayList<>(list);
        Collections.sort(list);
        assertEquals(exp, list);
    }
    
    @Test
    public void testEnding1()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("*.java");
        assertTrue(m.matches(getPath("/foo/bar/goo.java")));
        assertFalse(m.matches(getPath("/foo/bar/goo.cpp")));
    }
    @Test
    public void testEnding2()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("*.*");
        assertTrue(m.matches(getPath("/foo/bar/goo.java")));
        assertFalse(m.matches(getPath("/foo/bar/goocpp")));
    }
    @Test
    public void testEnding3()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("foo.?");
        assertTrue(m.matches(getPath("/foo/bar/foo.1")));
        assertFalse(m.matches(getPath("/foo/bar/goocpp")));
    }
    @Test
    public void testGroup()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("{java,jar}");
        assertTrue(m.matches(getPath("/foo/bar/goo.java")));
        assertTrue(m.matches(getPath("/foo/bar/goo.jar")));
        assertFalse(m.matches(getPath("/foo/bar/goo.cpp")));
    }
    @Test
    public void testBracketExpression1()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("*[-]ver");
        assertTrue(m.matches(getPath("/foo/bar/goo-ver")));
        assertFalse(m.matches(getPath("/foo/bar/goo.ver")));
    }
    @Test
    public void testBracketExpression2()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("*[!a-c].ver");
        assertTrue(m.matches(getPath("/foo/bar/goo.ver")));
        assertFalse(m.matches(getPath("/foo/bar/baa.ver")));
    }
    @Test
    public void testDirectory1()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("/home/*/*");
        assertTrue(m.matches(getPath("/home/gus/data")));
        assertFalse(m.matches(getPath("/home/gus")));
    }
    @Test
    public void testDirectory2()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("/home/**");
        assertTrue(m.matches(getPath("/home/gus/data")));
        assertTrue(m.matches(getPath("/home/gus")));
    }
    @Test
    public void testDirectory3()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("/home/**/hello.class");
        assertTrue(m.matches(getPath("/home/gus/data/hello.class")));
        assertTrue(m.matches(getPath("/home/gus/hello.class")));
    }
    @Test
    public void testDirectory4()
    {
        Glob g = Glob.newInstance();
        PathMatcher m = g.globMatcher("/home/**/middle/**hello.class");
        assertFalse(m.matches(getPath("/home/gus/data/hello.class")));
        assertTrue(m.matches(getPath("/home/foo/middle/gus/hello.class")));
    }
}
