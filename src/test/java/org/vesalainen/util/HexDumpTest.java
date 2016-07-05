/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.util;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class HexDumpTest
{
    
    public HexDumpTest()
    {
    }

    @Test
    public void test1()
    {
        byte[] a = "qwerty\nasdfg\t\t\n1234567890".getBytes();
        String h = HexDump.toHex(a);
        System.err.println(h);
        assertEquals("    00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n" +
                "00: 71 77 65 72 74 79 0a 61 73 64 66 67 09 09 0a 31  q w e r t y . a s d f g . . . 1 \n" +
                "10: 32 33 34 35 36 37 38 39 30                       2 3 4 5 6 7 8 9 0 \n", h);
        Assert.assertArrayEquals(a, HexDump.fromHex(h));
    }
    @Test
    public void test2()
    {
        String hd = "     00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n" +
                    "000: 4a af 81 80 00 01 00 04 00 0d 00 0d 03 77 77 77  J ﾯ . . . . . . . . . . . w w w\n" +
                    "010: 04 66 65 6d 61 03 67 6f 76 00 00 01 00 01 c0 0c  . f e m a . g o v . . . . . ￀ .\n" +
                    "020: 00 05 00 01 00 00 3a f0 00 1a 03 77 77 77 04 66  . . . . . . : ￰ . . . w w w . f\n" +
                    "030: 65 6d 61 03 67 6f 76 07 65 64 67 65 6b 65 79 03  e m a . g o v . e d g e k e y .\n" +
                    "040: 6e 65 74 00 c0 2a 00 05 00 01 00 00 01 2b 00 21  n e t . ￀ * . . . . . . . + . !\n" +
                    "050: 04 66 65 6d 61 0d 67 65 6f 72 65 64 69 72 65 63  . f e m a . g e o r e d i r e c\n" +
                    "060: 74 6f 72 04 66 65 6d 61 06 61 6b 61 64 6e 73 c0  t o r . f e m a . a k a d n s ￀\n" +
                    "070: 3f c0 50 00 05 00 01 00 00 01 2b 00 18 05 65 36  ? ￀ P . . . . . . . + . . . e 6\n" +
                    "080: 34 38 35 04 64 73 63 62 0a 61 6b 61 6d 61 69 65  4 8 5 . d s c b . a k a m a i e\n" +
                    "090: 64 67 65 c0 3f c0 7d 00 01 00 01 00 00 00 3c 00  d g e ￀ ? ￀ } . . . . . . . < .\n" +
                    "0a0: 04 17 01 74 38 00 00 00 02 00 01 00 03 2a 53 00  . . . t 8 . . . . . . . . * S .\n" +
                    "0b0: 11 01 6c 0c 72 6f 6f 74 2d 73 65 72 76 65 72 73  . . l . r o o t - s e r v e r s\n" +
                    "0c0: c0 3f c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6b  ￀ ? ￀ ﾥ . . . . . . * S . . . k\n" +
                    "0d0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 65  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . e\n" +
                    "0e0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 62  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . b\n" +
                    "0f0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6d  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . m\n" +
                    "100: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6a  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . j\n" +
                    "110: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 68  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . h\n" +
                    "120: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 67  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . g\n" +
                    "130: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 64  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . d\n" +
                    "140: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 63  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . c\n" +
                    "150: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 61  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . a\n" +
                    "160: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 69  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . i\n" +
                    "170: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 66  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . f\n" +
                    "180: c0 b3 c0 b1 00 01 00 01 00 00 74 8e 00 04 c7 07  ￀ ﾳ ￀ ﾱ . . . . . . t . . . ￇ .\n" +
                    "190: 53 2a c0 ce 00 01 00 01 00 00 74 8f 00 04 c1 00  S * ￀ ￎ . . . . . . t . . . ￁ .\n" +
                    "1a0: 0e 81 c0 de 00 01 00 01 00 01 a2 6d 00 04 c0 cb  . . ￀ ￞ . . . . . . ﾢ m . . ￀ ￋ\n" +
                    "1b0: e6 0a c0 ee 00 01 00 01 00 01 a2 6d 00 04 c0 e4  ￦ . ￀ ￮ . . . . . . ﾢ m . . ￀ ￤\n" +
                    "1c0: 4f c9 c0 fe 00 01 00 01 00 05 69 e3 00 04 ca 0c  O ￉ ￀ <fffe> . . . . . . i ￣ . . ￊ .\n" +
                    "1d0: 1b 21 c1 0e 00 01 00 01 00 00 74 90 00 04 c0 3a  . ! ￁ . . . . . . . t . . . ￀ :\n" +
                    "1e0: 80 1e c1 1e 00 01 00 01 00 01 a2 6d 00 04 c6 61  . . ￁ . . . . . . . ﾢ m . . ￆ a\n" +
                    "1f0: be 35 c1 2e 00 01 00 01 00 01 a2 6d 00 04 c0 70  ﾾ 5 ￁ . . . . . . . ﾢ m . . ￀ p\n" +
                    "200: 24 04 c1 3e 00 01 00 01 00 01 a2 6d 00 04 c7 07  $ . ￁ > . . . . . . ﾢ m . . ￇ .\n" +
                    "210: 5b 0d c1 4e 00 01 00 01 00 01 a2 6d 00 04 c0 21  [ . ￁ N . . . . . . ﾢ m . . ￀ !\n" +
                    "220: 04 0c c1 5e 00 01 00 01 00 00 50 e5 00 04 c6 29  . . ￁ ^ . . . . . . P ￥ . . ￆ )\n" +
                    "230: 00 04 c1 6e 00 01 00 01 00 00 74 90 00 04 c0 24  . . ￁ n . . . . . . t . . . ￀ $\n" +
                    "240: 94 11 c1 7e 00 01 00 01 00 01 a2 6d 00 04 c0 05  . . ￁ ~ . . . . . . ﾢ m . . ￀ .\n" +
                    "250: 05 f1                                            . ￱";
        byte[] fromHex = HexDump.fromHex(hd);
        System.err.println(HexDump.toHex(fromHex));
    }
    
}
