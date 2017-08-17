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
package org.vesalainen.pm.rpm;

import org.vesalainen.pm.rpm.CPIO;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.FilterByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CPIOTest
{
    private static final String SS = "070701"
    + "000012ac"
    + "000a12ac"
    + "00001bac"
    + "000012dc"
    + "000012ac"
    + "000013ac"
    + "000012ac"
    + "000012af"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac"
    + "000012ac";
    private static final byte[] EXP = SS.getBytes(US_ASCII);
    
    public CPIOTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        byte[] got = Arrays.copyOf(EXP, EXP.length);
        ByteBuffer bb = ByteBuffer.wrap(got);
        FilterByteBuffer fbb = new FilterByteBuffer(bb, BufferedInputStream::new, BufferedOutputStream::new);
        CPIO cpio = new CPIO(fbb);
        bb.flip();
        cpio.save(fbb);
        assertArrayEquals(EXP, got);
    }
    
}
