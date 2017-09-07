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
package org.vesalainen.nio;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileUtil
{
    public static final DirectoryDeletor FILE_DELETOR = new DirectoryDeletor();
    /**
     * Reads InputStream as stream of lines. Line separator is '\n' while '\r' 
     * is simply ignored. Character set is UTF-8
     * @param is
     * @return 
     */
    public static final Stream<String> lines(InputStream is)
    {
        return lines(is, UTF_8);
    }
    /**
     * Reads InputStream as stream of lines. Line separator is '\n' while '\r' 
     * is simply ignored.
     * @param is
     * @param cs
     * @return 
     */
    public static final Stream<String> lines(InputStream is, Charset cs)
    {
        return StreamSupport.stream(new StringSplitIterator(is, cs), false);
    }
    private static final class StringSplitIterator extends AbstractSpliterator<String>
    {
        private IOIntSupplier supplier;
        private StringBuilder sb = new StringBuilder();
        public StringSplitIterator(InputStream is, Charset cs)
        {
            super(Long.MAX_VALUE, 0);
            is = buffer(is);
            if (ISO_8859_1.contains(cs))
            {
                supplier = is::read;
            }
            else
            {
                InputStreamReader isr = new InputStreamReader(is, cs);
                supplier = isr::read;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action)
        {
            try
            {
                sb.setLength(0);
                while (true)
                {
                    int cc = supplier.read();
                    switch (cc)
                    {
                        case -1:
                            if (sb.length() > 0)
                            {
                                action.accept(sb.toString());
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        case '\r':
                            break;
                        case '\n':
                                action.accept(sb.toString());
                                return true;
                        default:
                            sb.append((char)cc);
                            break;
                    }
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }

    }
    @FunctionalInterface
    private interface IOIntSupplier
    {
        int read() throws IOException;
    }
    /**
     * Read all bytes from InputStream and returns them as byte array. 
     * InputStream is not closed after call.
     * @param is
     * @return
     * @throws IOException 
     */
    public static final byte[] readAllBytes(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int rc = is.read(buf);
        while (rc != -1)
        {
            baos.write(buf, 0, rc);
            rc = is.read(buf);
        }
        return baos.toByteArray();
    }
    /**
     * Delete directory and all files in it.
     * @param dir
     * @throws IOException 
     */
    public static final void deleteDirectory(File dir) throws IOException
    {
        deleteDirectory(dir.toPath());
    }
    /**
     * Delete directory and all files in it.
     * @param dir
     * @throws IOException 
     */
    public static final void deleteDirectory(Path dir) throws IOException
    {
        Files.walkFileTree(dir, FILE_DELETOR);
    }
    
    public static final class DirectoryDeletor extends SimpleFileVisitor<Path>
    {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            Files.delete(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            Files.delete(file);
            return CONTINUE;
        }
        
    }
    /**
     * Wraps InputStream with BufferedInputStream if is is not BufferedInputStream.
     * If it is returns is.
     * @param is    
     * @return 
     */
    public static final BufferedInputStream buffer(InputStream is)
    {
        if (is instanceof BufferedInputStream)
        {
            return (BufferedInputStream) is;
        }
        else
        {
            return new BufferedInputStream(is);
        }
    }
    /**
     * Copies class resource to path
     * @param source As in getResourceAsStream
     * @param target Target file
     * @param cls A class for finding class loader.
     * @throws IOException 
     * @see java.lang.Class#getResourceAsStream(java.lang.String) 
     */
    public static final void copyResource(String source, Path target, Class<?> cls) throws IOException
    {
        try (InputStream is = cls.getResourceAsStream(source))
        {
            Files.copy(is, target, REPLACE_EXISTING);
        }
    }
    public static final FileTime getLastModifiedTime(Path file) throws IOException
    {
        return (FileTime) Files.getAttribute(file, "lastModifiedTime");
    }
    public static final FileTime getLastAccessTime(Path file) throws IOException
    {
        return (FileTime) Files.getAttribute(file, "lastAccessTime");
    }
    public static final FileTime getCreationTime(Path file) throws IOException
    {
        return (FileTime) Files.getAttribute(file, "creationTime");
    }
    /**
     * Set files times. Non null times are changed.
     * @param lastModifiedTime Can be null
     * @param lastAccessTime Can be null
     * @param createTime Can be null
     * @param files
     * @throws IOException 
     */
    public static final void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime, Path... files) throws IOException
    {
        for (Path file : files)
        {
            BasicFileAttributeView view = Files.getFileAttributeView(file, BasicFileAttributeView.class);
            view.setTimes(lastModifiedTime, lastAccessTime, createTime);
        }
    }
    /**
     * Enhances Files.copy to directory. If both source and target are 
     * directories the source directory structure is copied to target.
     * @param source
     * @param target
     * @param options
     * @throws IOException 
     * @see java.nio.file.Files#copy(java.nio.file.Path, java.nio.file.Path, java.nio.file.CopyOption...) 
     */
    public static final void copy(Path source, Path target, CopyOption... options) throws IOException
    {
        if (Files.isRegularFile(target))
        {
            if (Files.isRegularFile(target))
            {
                Files.copy(source, target, options);
            }
            else
            {
                if (Files.isDirectory(target))
                {
                    Path t = target.resolve(source.getFileName());
                    Files.copy(source, t, options);
                }
            }
        }
        else
        {
            if (Files.isDirectory(source) && Files.isDirectory(target))
            {
                Path fileName = source.getFileName();
                final Path dir = fileName != null ? target.resolve(fileName) : target;
                Files.createDirectories(target);
                Files.walk(source).forEach((p)->
                {
                    try
                    {
                        Path rel = source.relativize(p);
                        Path trg = dir.resolve(rel);
                        if (Files.isDirectory(p))
                        {
                            Files.createDirectories(trg);
                        }
                        else
                        {
                            if (Files.isRegularFile(p))
                            {
                                Files.copy(p, trg, options);
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                });
            }
            else
            {
                throw new IllegalArgumentException("don't know what to do");
            }
        }
    }
}
