/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.lang;

/**
 *
 * @author Timo Vesalainen
 */
public class NumberRanges
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        System.err.println("static final CharSequence[][] ByteRange = new String[][] {");
        print(Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.err.println("static final CharSequence[][] ShortRange = new String[][] {");
        print(Short.MIN_VALUE, Short.MAX_VALUE);
        System.err.println("static final CharSequence[][] IntRange = new String[][] {");
        print(Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.err.println("static final CharSequence[][] LongRange = new String[][] {");
        print(Long.MIN_VALUE, Long.MAX_VALUE);
        System.err.println("static final CharSequence[][] UnsignedIntRange = new String[][] {");
        print(0, -1, int.class);
        System.err.println("static final CharSequence[][] UnsignedLongRange = new String[][] {");
        print(0, -1, long.class);
    }

    private static void print(long min, long max)
    {
        print(min, max, null);
    }
    private static void print(long min, long max, Class<?> type)
    {
        for (int radix=0;radix<Character.MAX_RADIX;radix++)
        {
            if (radix < Character.MIN_RADIX)
            {
                System.err.println("{null, null},");
            }
            else
            {
                if (int.class == type)
                {
//                    System.err.print("{\""+Integer.toUnsignedString((int) min, radix)+"\", \""+Integer.toUnsignedString((int) max, radix)+"\"}");
                }
                else
                {
                    if (long.class == type)
                    {
  //                      System.err.print("{\""+Long.toUnsignedString(min, radix)+"\", \""+Long.toUnsignedString(max, radix)+"\"}");
                    }
                    else
                    {
                        System.err.print("{\""+Long.toString(min, radix)+"\", \""+Long.toString(max, radix)+"\"}");
                    }
                }
                if (radix+1<Character.MAX_RADIX)
                {
                    System.err.println(",");
                }
                else
                {
                    System.err.println("");
                }
            }
        }
        System.err.println("};");
    }
static final CharSequence[][] ByteRange = new String[][] {
{null, null},
{null, null},
{"-10000000", "1111111"},
{"-11202", "11201"},
{"-2000", "1333"},
{"-1003", "1002"},
{"-332", "331"},
{"-242", "241"},
{"-200", "177"},
{"-152", "151"},
{"-128", "127"},
{"-107", "106"},
{"-a8", "a7"},
{"-9b", "9a"},
{"-92", "91"},
{"-88", "87"},
{"-80", "7f"},
{"-79", "78"},
{"-72", "71"},
{"-6e", "6d"},
{"-68", "67"},
{"-62", "61"},
{"-5i", "5h"},
{"-5d", "5c"},
{"-58", "57"},
{"-53", "52"},
{"-4o", "4n"},
{"-4k", "4j"},
{"-4g", "4f"},
{"-4c", "4b"},
{"-48", "47"},
{"-44", "43"},
{"-40", "3v"},
{"-3t", "3s"},
{"-3q", "3p"},
{"-3n", "3m"}
};
static final CharSequence[][] ShortRange = new String[][] {
{null, null},
{null, null},
{"-1000000000000000", "111111111111111"},
{"-1122221122", "1122221121"},
{"-20000000", "13333333"},
{"-2022033", "2022032"},
{"-411412", "411411"},
{"-164351", "164350"},
{"-100000", "77777"},
{"-48848", "48847"},
{"-32768", "32767"},
{"-2268a", "22689"},
{"-16b68", "16b67"},
{"-11bb8", "11bb7"},
{"-bd28", "bd27"},
{"-9a98", "9a97"},
{"-8000", "7fff"},
{"-6b69", "6b68"},
{"-5b28", "5b27"},
{"-4eec", "4eeb"},
{"-41i8", "41i7"},
{"-3b68", "3b67"},
{"-31fa", "31f9"},
{"-2flg", "2flf"},
{"-28l8", "28l7"},
{"-22ai", "22ah"},
{"-1mc8", "1mc7"},
{"-1hph", "1hpg"},
{"-1dm8", "1dm7"},
{"-19rr", "19rq"},
{"-16c8", "16c7"},
{"-1331", "1330"},
{"-1000", "vvv"},
{"-u2w", "u2v"},
{"-sbq", "sbp"},
{"-qq8", "qq7"}
};
static final CharSequence[][] IntRange = new String[][] {
{null, null},
{null, null},
{"-10000000000000000000000000000000", "1111111111111111111111111111111"},
{"-12112122212110202102", "12112122212110202101"},
{"-2000000000000000", "1333333333333333"},
{"-13344223434043", "13344223434042"},
{"-553032005532", "553032005531"},
{"-104134211162", "104134211161"},
{"-20000000000", "17777777777"},
{"-5478773672", "5478773671"},
{"-2147483648", "2147483647"},
{"-a02220282", "a02220281"},
{"-4bb2308a8", "4bb2308a7"},
{"-282ba4aab", "282ba4aaa"},
{"-1652ca932", "1652ca931"},
{"-c87e66b8", "c87e66b7"},
{"-80000000", "7fffffff"},
{"-53g7f549", "53g7f548"},
{"-3928g3h2", "3928g3h1"},
{"-27c57h33", "27c57h32"},
{"-1db1f928", "1db1f927"},
{"-140h2d92", "140h2d91"},
{"-ikf5bf2", "ikf5bf1"},
{"-ebelf96", "ebelf95"},
{"-b5gge58", "b5gge57"},
{"-8jmdnkn", "8jmdnkm"},
{"-6oj8ioo", "6oj8ion"},
{"-5ehnckb", "5ehncka"},
{"-4clm98g", "4clm98f"},
{"-3hk7988", "3hk7987"},
{"-2sb6cs8", "2sb6cs7"},
{"-2d09uc2", "2d09uc1"},
{"-2000000", "1vvvvvv"},
{"-1lsqtl2", "1lsqtl1"},
{"-1d8xqrq", "1d8xqrp"},
{"-15v22un", "15v22um"}
};
static final CharSequence[][] LongRange = new String[][] {
{null, null},
{null, null},
{"-1000000000000000000000000000000000000000000000000000000000000000", "111111111111111111111111111111111111111111111111111111111111111"},
{"-2021110011022210012102010021220101220222", "2021110011022210012102010021220101220221"},
{"-20000000000000000000000000000000", "13333333333333333333333333333333"},
{"-1104332401304422434310311213", "1104332401304422434310311212"},
{"-1540241003031030222122212", "1540241003031030222122211"},
{"-22341010611245052052301", "22341010611245052052300"},
{"-1000000000000000000000", "777777777777777777777"},
{"-67404283172107811828", "67404283172107811827"},
{"-9223372036854775808", "9223372036854775807"},
{"-1728002635214590698", "1728002635214590697"},
{"-41a792678515120368", "41a792678515120367"},
{"-10b269549075433c38", "10b269549075433c37"},
{"-4340724c6c71dc7a8", "4340724c6c71dc7a7"},
{"-160e2ad3246366808", "160e2ad3246366807"},
{"-8000000000000000", "7fffffffffffffff"},
{"-33d3d8307b214009", "33d3d8307b214008"},
{"-16agh595df825fa8", "16agh595df825fa7"},
{"-ba643dci0ffeehi", "ba643dci0ffeehh"},
{"-5cbfjia3fh26ja8", "5cbfjia3fh26ja7"},
{"-2heiciiie82dh98", "2heiciiie82dh97"},
{"-1adaibb21dckfa8", "1adaibb21dckfa7"},
{"-i6k448cf4192c3", "i6k448cf4192c2"},
{"-acd772jnc9l0l8", "acd772jnc9l0l7"},
{"-64ie1focnn5g78", "64ie1focnn5g77"},
{"-3igoecjbmca688", "3igoecjbmca687"},
{"-27c48l5b37oaoq", "27c48l5b37oaop"},
{"-1bk39f3ah3dmq8", "1bk39f3ah3dmq7"},
{"-q1se8f0m04isc", "q1se8f0m04isb"},
{"-hajppbc1fc208", "hajppbc1fc207"},
{"-bm03i95hia438", "bm03i95hia437"},
{"-8000000000000", "7vvvvvvvvvvvv"},
{"-5hg4ck9jd4u38", "5hg4ck9jd4u37"},
{"-3tdtk1v8j6tpq", "3tdtk1v8j6tpp"},
{"-2pijmikexrxp8", "2pijmikexrxp7"}
};
static final CharSequence[][] UnsignedIntRange = new String[][] {
{null, null},
{null, null},
{"0", "11111111111111111111111111111111"},
{"0", "102002022201221111210"},
{"0", "3333333333333333"},
{"0", "32244002423140"},
{"0", "1550104015503"},
{"0", "211301422353"},
{"0", "37777777777"},
{"0", "12068657453"},
{"0", "4294967295"},
{"0", "1904440553"},
{"0", "9ba461593"},
{"0", "535a79888"},
{"0", "2ca5b7463"},
{"0", "1a20dcd80"},
{"0", "ffffffff"},
{"0", "a7ffda90"},
{"0", "704he7g3"},
{"0", "4f5aff65"},
{"0", "3723ai4f"},
{"0", "281d55i3"},
{"0", "1fj8b183"},
{"0", "1606k7ib"},
{"0", "mb994af"},
{"0", "hek2mgk"},
{"0", "dnchbnl"},
{"0", "b28jpdl"},
{"0", "8pfgih3"},
{"0", "76beigf"},
{"0", "5qmcpqf"},
{"0", "4q0jto3"},
{"0", "3vvvvvv"},
{"0", "3aokq93"},
{"0", "2qhxjlh"},
{"0", "2br45qa"}
};
static final CharSequence[][] UnsignedLongRange = new String[][] {
{null, null},
{null, null},
{"0", "1111111111111111111111111111111111111111111111111111111111111111"},
{"0", "11112220022122120101211020120210210211220"},
{"0", "33333333333333333333333333333333"},
{"0", "2214220303114400424121122430"},
{"0", "3520522010102100444244423"},
{"0", "45012021522523134134601"},
{"0", "1777777777777777777777"},
{"0", "145808576354216723756"},
{"0", "18446744073709551615"},
{"0", "335500516a429071284"},
{"0", "839365134a2a240713"},
{"0", "219505a9511a867b72"},
{"0", "8681049adb03db171"},
{"0", "2c1d56b648c6cd110"},
{"0", "ffffffffffffffff"},
{"0", "67979g60f5428010"},
{"0", "2d3fgb0b9cg4bd2f"},
{"0", "141c8786h1ccaagg"},
{"0", "b53bjh07be4dj0f"},
{"0", "5e8g4ggg7g56dif"},
{"0", "2l4lf104353j8kf"},
{"0", "1ddh88h2782i515"},
{"0", "l12ee5fn0ji1if"},
{"0", "c9c336o0mlb7ef"},
{"0", "7b7n2pcniokcgf"},
{"0", "4eo8hfam6fllmo"},
{"0", "2nc6j26l66rhof"},
{"0", "1n3rsh11f098rn"},
{"0", "14l9lkmo30o40f"},
{"0", "nd075ib45k86f"},
{"0", "fvvvvvvvvvvvv"},
{"0", "b1w8p7j5q9r6f"},
{"0", "7orp63sh4dphh"},
{"0", "5g24a25twkwff"}
};
}
