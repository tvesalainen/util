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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;
import org.vesalainen.regex.Regex;
import org.vesalainen.util.function.IOFunction;
import static org.vesalainen.vfs.CompressorFactory.Compressor.*;

/**
 * CompressorFactory contains methods for creating supplier functions needed
 * if FilterChannel.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.nio.channels.FilterChannel
 */
public final class CompressorFactory
{
    public enum Compressor {GZIP, XZ}
    public static final PathMatcher GZIP_MATCHER;
    public static final PathMatcher XZ_MATCHER;
    static
    {
        Glob glob = Glob.newInstance();
        GZIP_MATCHER = glob.globMatcher("*.gz", Regex.Option.CASE_INSENSITIVE);
        XZ_MATCHER = glob.globMatcher("*.xz", Regex.Option.CASE_INSENSITIVE);
    }
    public static final IOFunction<? super OutputStream,? extends OutputStream> output(Path path)
    {
        return output(compressor(path));
    }
    public static final IOFunction<? super OutputStream,? extends OutputStream> output(Compressor comp)
    {
        switch (comp)
        {
            case GZIP:
                return CompressorFactory::createGZIPOutputStream;
            case XZ:
                return CompressorFactory::createXZOutputStream;
            default:
                throw new UnsupportedOperationException(comp+" not supported");
        }
    }
    public static final IOFunction<? super InputStream,? extends InputStream> input(Path path)
    {
        return input(compressor(path));
    }
    public static final IOFunction<? super InputStream,? extends InputStream> input(Compressor comp)
    {
        switch (comp)
        {
            case GZIP:
                return GZIPInputStream::new;
            case XZ:
                return XZInputStream::new;
            default:
                throw new UnsupportedOperationException(comp+" not supported");
        }
    }
    private static final Compressor compressor(Path path)
    {
        if (GZIP_MATCHER.matches(path))
        {
            return GZIP;
        }
        if (XZ_MATCHER.matches(path))
        {
            return XZ;
        }
        throw new UnsupportedOperationException(path+" not supported");
    }
    private static GZIPOutputStream createGZIPOutputStream(OutputStream out) throws IOException
    {
        return new GZOut(out, true);
    }
    private static XZOutputStream createXZOutputStream(OutputStream out) throws IOException
    {
        return new XZOut(out, new LZMA2Options());
    }
    private static class XZOut extends XZOutputStream
    {

        public XZOut(OutputStream out, FilterOptions fo) throws IOException
        {
            super(out, fo);
        }
        
        @Override
        public void flush() throws IOException
        {
            super.finish();
        }
        
    }
    private static class GZOut extends GZIPOutputStream
    {
        
        public GZOut(OutputStream out, boolean syncFlush) throws IOException
        {
            super(out, syncFlush);
        }

        @Override
        public void flush() throws IOException
        {
            super.finish();
            super.flush();
        }
        
    }
}
