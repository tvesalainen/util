/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.util.code128;

/**
 * @author Timo Vesalainen
 */
public class Code128Constants 
{
    public static final char NUL =	0;
    public static final char SOH =	1;
    public static final char STX =	2;
    public static final char ETX =	3;
    public static final char EOT =	4;
    public static final char ENQ =	5;
    public static final char ACK =	6;
    public static final char BEL =	7;
    public static final char BS =	8;
    public static final char HT =	9;
    public static final char LF =	10;
    public static final char VT =	11;
    public static final char FF =	12;
    public static final char CR =	13;
    public static final char SO =	14;
    public static final char SI =	15;
    public static final char DLE =	16;
    public static final char DC1 =	17;
    public static final char DC2 =	18;
    public static final char DC3 =	19;
    public static final char DC4 =	20;
    public static final char NAK =	21;
    public static final char SYN =	22;
    public static final char ETB =	23;
    public static final char CAN =	24;
    public static final char EM =	25;
    public static final char SUB =	26;
    public static final char ESC =	27;
    public static final char FS =	28;
    public static final char GS =	29;
    public static final char RS =	30;
    public static final char US =	31;
    public static final char DEL =	255;

    public static final char[] tabA = new char[]
    {
            ' ',	/* 000 */
            '!',	/* 001 */
            '"',	/* 002 */
            '#',	/* 003 */
            '$',	/* 004 */
            '%',	/* 005 */
            '&',	/* 006 */
            '\'',	/* 007 */
            '(',	/* 008 */
            ')',	/* 009 */
            '*',	/* 010 */
            '+',	/* 011 */
            '.',	/* 012 */
            '-',	/* 013 */
            ',',	/* 014 */
            ';',	/* 015 */
            '0',	/* 016 */
            '1',	/* 017 */
            '2',	/* 018 */
            '3',	/* 019 */
            '4',	/* 020 */
            '5',	/* 021 */
            '6',	/* 022 */
            '7',	/* 023 */
            '8',	/* 024 */
            '9',	/* 025 */
            ':',	/* 026 */
            ';',	/* 027 */
            '<',	/* 028 */
            '=',	/* 029 */
            '>',	/* 030 */
            '?',	/* 031 */
            '@',	/* 032 */
            'A',	/* 033 */
            'B',	/* 034 */
            'C',	/* 035 */
            'D',	/* 036 */
            'E',	/* 037 */
            'F',	/* 038 */
            'G',	/* 039 */
            'H',	/* 040 */
            'I',	/* 041 */
            'J',	/* 042 */
            'K',	/* 043 */
            'L',	/* 044 */
            'M',	/* 045 */
            'N',	/* 046 */
            'O',	/* 047 */
            'P',	/* 048 */
            'Q',	/* 049 */
            'R',	/* 050 */
            'S',	/* 051 */
            'T',	/* 052 */
            'U',	/* 053 */
            'V',	/* 054 */
            'W',	/* 055 */
            'X',	/* 056 */
            'Y',	/* 057 */
            'Z',	/* 058 */
            '[',	/* 059 */
            '\\',	/* 060 */
            ']',	/* 061 */
            '^',	/* 062 */
            '_',	/* 063 */
            NUL,	/* 064 */
            SOH,	/* 065 */
            STX,	/* 066 */
            ETX,	/* 067 */
            EOT,	/* 068 */
            ENQ,	/* 069 */
            ACK,	/* 070 */
            BEL,	/* 071 */
            BS,		/* 072 */
            HT,		/* 073 */
            LF,		/* 074 */
            VT,		/* 075 */
            FF,		/* 076 */
            CR,		/* 077 */
            SO,		/* 078 */
            SI,		/* 079 */
            DLE,	/* 080 */
            DC1,	/* 081 */
            DC2,	/* 082 */
            DC3,	/* 083 */
            DC4,	/* 084 */
            NAK,	/* 085 */
            SYN,	/* 086 */
            ETB,	/* 087 */
            CAN,	/* 088 */
            EM,		/* 089 */
            SUB,	/* 090 */
            ESC,	/* 091 */
            FS,		/* 092 */
            GS,		/* 093 */
            RS,		/* 094 */
            US		/* 095 */
    };	/*  */

    public static final char[] tabB = new char[]
    {
            ' ',	/* 000 */
            '!',	/* 001 */
            '"',	/* 002 */
            '#',	/* 003 */
            '$',	/* 004 */
            '%',	/* 005 */
            '&',	/* 006 */
            '\'',	/* 007 */
            '(',	/* 008 */
            ')',	/* 009 */
            '*',	/* 010 */
            '+',	/* 011 */
            '.',	/* 012 */
            '-',	/* 013 */
            ',',	/* 014 */
            ';',	/* 015 */
            '0',	/* 016 */
            '1',	/* 017 */
            '2',	/* 018 */
            '3',	/* 019 */
            '4',	/* 020 */
            '5',	/* 021 */
            '6',	/* 022 */
            '7',	/* 023 */
            '8',	/* 024 */
            '9',	/* 025 */
            ':',	/* 026 */
            ';',	/* 027 */
            '<',	/* 028 */
            '=',	/* 029 */
            '>',	/* 030 */
            '?',	/* 031 */
            '@',	/* 032 */
            'A',	/* 033 */
            'B',	/* 034 */
            'C',	/* 035 */
            'D',	/* 036 */
            'E',	/* 037 */
            'F',	/* 038 */
            'G',	/* 039 */
            'H',	/* 040 */
            'I',	/* 041 */
            'J',	/* 042 */
            'K',	/* 043 */
            'L',	/* 044 */
            'M',	/* 045 */
            'N',	/* 046 */
            'O',	/* 047 */
            'P',	/* 048 */
            'Q',	/* 049 */
            'R',	/* 050 */
            'S',	/* 051 */
            'T',	/* 052 */
            'U',	/* 053 */
            'V',	/* 054 */
            'W',	/* 055 */
            'X',	/* 056 */
            'Y',	/* 057 */
            'Z',	/* 058 */
            '[',	/* 059 */
            '\\',	/* 060 */
            ']',	/* 061 */
            '^',	/* 062 */
            '_',	/* 063 */
            '`',	/* 064 */
            'a',	/* 065 */
            'b',	/* 066 */
            'c',	/* 067 */
            'd',	/* 068 */
            'e',	/* 069 */
            'f',	/* 070 */
            'g',	/* 071 */
            'h',	/* 072 */
            'i',	/* 073 */
            'j',	/* 074 */
            'k',	/* 075 */
            'l',	/* 076 */
            'm',	/* 077 */
            'n',	/* 078 */
            'o',	/* 079 */
            'p',	/* 080 */
            'q',	/* 081 */
            'r',	/* 082 */
            's',	/* 083 */
            't',	/* 084 */
            'u',	/* 085 */
            'v',	/* 086 */
            'w',	/* 087 */
            'x',	/* 088 */
            'y',	/* 089 */
            'z',	/* 090 */
            '{',	/* 091 */
            '|',	/* 092 */
            '}',	/* 093 */
            '~',	/* 094 */
            DEL		/* 095 */
    };	 

    public static final char tabEncode[][] = new char[][]
            {
            new char[] {2,1,2,2,2,2},	/* 000 */
            new char[] {2,2,2,1,2,2},	/* 001 */
            new char[] {2,2,2,2,2,1},	/* 002 */
            new char[] {1,2,1,2,2,3},	/* 003 */
            new char[] {1,2,1,3,2,2},	/* 004 */
            new char[] {1,3,1,2,2,2},	/* 005 */
            new char[] {1,2,2,2,1,3},	/* 006 */
            new char[] {1,2,2,3,1,2},	/* 007 */
            new char[] {1,3,2,2,1,2},	/* 008 */
            new char[] {2,2,1,2,1,3},	/* 009 */
            new char[] {2,2,1,3,1,2},	/* 010 */
            new char[] {2,3,1,2,1,2},	/* 011 */
            new char[] {1,1,2,2,3,2},	/* 012 */
            new char[] {1,2,2,1,3,2},	/* 013 */
            new char[] {1,2,2,2,3,1},	/* 014 */
            new char[] {1,1,3,2,2,2},	/* 015 */
            new char[] {1,2,3,1,2,2},	/* 016 */
            new char[] {1,2,3,2,2,1},	/* 017 */
            new char[] {2,2,3,2,1,1},	/* 018 */
            new char[] {2,2,1,1,3,2},	/* 019 */
            new char[] {2,2,1,2,3,1},	/* 020 */
            new char[] {2,1,3,2,1,2},	/* 021 */
            new char[] {2,2,3,1,1,2},	/* 022 */
            new char[] {3,1,2,1,3,1},	/* 023 */
            new char[] {3,1,1,2,2,2},	/* 024 */
            new char[] {3,2,1,1,2,2},	/* 025 */
            new char[] {3,2,1,2,2,1},	/* 026 */
            new char[] {3,1,2,2,1,2},	/* 027 */
            new char[] {3,2,2,1,1,2},	/* 028 */
            new char[] {3,2,2,2,1,1},	/* 029 */
            new char[] {2,1,2,1,2,3},	/* 030 */
            new char[] {2,1,2,3,2,1},	/* 031 */
            new char[] {2,3,2,1,2,1},	/* 032 */
            new char[] {1,1,1,3,2,3},	/* 033 */
            new char[] {1,3,1,1,2,3},	/* 034 */
            new char[] {1,3,1,3,2,1},	/* 035 */
            new char[] {1,1,2,3,1,3},	/* 036 */
            new char[] {1,3,2,1,1,3},	/* 037 */
            new char[] {1,3,2,3,1,1},	/* 038 */
            new char[] {2,1,1,3,1,3},	/* 039 */
            new char[] {2,3,1,1,1,3},	/* 040 */
            new char[] {2,3,1,3,1,1},	/* 041 */
            new char[] {1,1,2,1,3,3},	/* 042 */
            new char[] {1,1,2,3,3,1},	/* 043 */
            new char[] {1,3,2,1,3,1},	/* 044 */
            new char[] {1,1,3,1,2,3},	/* 045 */
            new char[] {1,1,3,3,2,1},	/* 046 */
            new char[] {1,3,3,1,2,1},	/* 047 */
            new char[] {3,1,3,1,2,1},	/* 048 */
            new char[] {2,1,1,3,3,1},	/* 049 */
            new char[] {2,3,1,1,3,1},	/* 050 */
            new char[] {2,1,3,1,1,3},	/* 051 */
            new char[] {2,1,3,3,1,1},	/* 052 */
            new char[] {2,1,3,1,3,1},	/* 053 */
            new char[] {3,1,1,1,2,3},	/* 054 */
            new char[] {3,1,1,3,2,1},	/* 055 */
            new char[] {3,3,1,1,2,1},	/* 056 */
            new char[] {3,1,2,1,1,3},	/* 057 */
            new char[] {3,1,2,3,1,1},	/* 058 */
            new char[] {3,3,2,1,1,1},	/* 059 */
            new char[] {3,1,4,1,1,1},	/* 060 */
            new char[] {2,2,1,4,1,1},	/* 061 */
            new char[] {4,3,1,1,1,1},	/* 062 */
            new char[] {1,1,1,2,2,4},	/* 063 */
            new char[] {1,1,1,4,2,2},	/* 064 */
            new char[] {1,2,1,1,2,4},	/* 065 */
            new char[] {1,2,1,4,2,1},	/* 066 */
            new char[] {1,4,1,1,2,2},	/* 067 */
            new char[] {1,4,1,2,2,1},	/* 068 */
            new char[] {1,1,2,2,1,4},	/* 069 */
            new char[] {1,1,2,4,1,2},	/* 070 */
            new char[] {1,2,2,1,1,4},	/* 071 */
            new char[] {1,2,2,4,1,1},	/* 072 */
            new char[] {1,4,2,1,1,2},	/* 073 */
            new char[] {1,4,2,2,1,1},	/* 074 */
            new char[] {2,4,1,2,1,1},	/* 075 */
            new char[] {2,2,1,1,1,4},	/* 076 */
            new char[] {4,1,3,1,1,1},	/* 077 */
            new char[] {2,4,1,1,1,2},	/* 078 */
            new char[] {1,3,4,1,1,1},	/* 079 */
            new char[] {1,1,1,2,4,2},	/* 080 */
            new char[] {1,2,1,1,4,2},	/* 081 */
            new char[] {1,2,1,2,4,1},	/* 082 */
            new char[] {1,1,4,2,1,2},	/* 083 */
            new char[] {1,2,4,1,1,2},	/* 084 */
            new char[] {1,2,4,2,1,1},	/* 085 */
            new char[] {4,1,1,2,1,2},	/* 086 */
            new char[] {4,2,1,1,1,2},	/* 087 */
            new char[] {4,2,1,2,1,1},	/* 088 */
            new char[] {2,1,2,1,4,1},	/* 089 */
            new char[] {2,1,4,1,2,1},	/* 090 */
            new char[] {4,1,2,1,2,1},	/* 091 */
            new char[] {1,1,1,1,4,3},	/* 092 */
            new char[] {1,1,1,3,4,1},	/* 093 */
            new char[] {1,3,1,1,4,1},	/* 094 */
            new char[] {1,1,4,1,1,3},	/* 095	Code A	Code B	Code C */
            new char[] {1,1,4,3,1,1},	/* 096	FNC3	FNC3 */
            new char[] {4,1,1,1,1,3},	/* 097	FNC2	FNC2 */
            new char[] {4,1,1,3,1,1},	/* 098	SHIFT	SHIFT */
            new char[] {1,1,3,1,4,1},	/* 099	CODE C	CODE C */
            new char[] {1,1,4,1,3,1},	/* 100	CODE B	FNC4	CODE B */
            new char[] {3,1,1,1,4,1},	/* 101	FNC4	CODE A	CODE A */
            new char[] {4,1,1,1,3,1},	/* 102	FNC1	FNC1	FNC1 */
            new char[] {2,1,1,4,1,2},	/* 103	START (CODE A) */
            new char[] {2,1,1,2,1,4},	/* 104	START (CODE B) */
            new char[] {2,1,1,2,3,2}	/* 105	START (CODE C) */
            };	

    public static final char STARTA	= 103;
    public static final char STARTB	= 104;
    public static final char STARTC	= 105;

    public static final char CODEA	= 0;	/* Code A Column */
    public static final char CODEB	= 1;	/* Code B Column */
    public static final char CODEC	= 2;	/* Code C Column */

    public static final char FNC1	= 0;	/*  */
    public static final char FNC2	= 1;
    public static final char FNC3	= 2;
    public static final char FNC4	= 3;	/* Add 128 to following character */
    public static final char SHIFT	= 4;	/* Temporary Shift A <-> B */
    public static final char SHIFTA	= 5;	/* Shift to A */
    public static final char SHIFTB	= 6;	/* Shift to B */
    public static final char SHIFTC	= 7;	/* Shift to C */

    public static final char tabFunc[][] = new char[][]
    {
        new char[] { 102, 102, 102 },	/* FNC1 */
        new char[] { 97, 97, 0 },		/* FNC2 */
        new char[] { 96, 96, 0 },		/* FNC3 */
        new char[] { 101, 100, 0 },	/* FNC4 */
        new char[] { 98, 98, 0 },		/* SHIFT */
        new char[] { 0, 101, 101 },	/* SHIFTA */
        new char[] { 100, 0, 100 },	/* SHIFTB */
        new char[] { 99, 99, 0 }		/* SHIFTC */
    };

    public static final char tabStop[] = {2,3,3,1,1,1,2};

    public static final char tabDecode[][] = new char[][]
    {
        new char[] {3,3,4,4,6},	/* 000 */
        new char[] {4,4,3,3,6},	/* 001 */
        new char[] {4,4,4,4,6},	/* 002 */
        new char[] {3,3,3,4,4},	/* 003 */
        new char[] {3,3,4,5,4},	/* 004 */
        new char[] {4,4,3,4,4},	/* 005 */
        new char[] {3,4,4,3,4},	/* 006 */
        new char[] {3,4,5,4,4},	/* 007 */
        new char[] {4,5,4,3,4},	/* 008 */
        new char[] {4,3,3,3,4},	/* 009 */
        new char[] {4,3,4,4,4},	/* 010 */
        new char[] {5,4,3,3,4},	/* 011 */
        new char[] {2,3,4,5,6},	/* 012 */
        new char[] {3,4,3,4,6},	/* 013 */
        new char[] {3,4,4,5,6},	/* 014 */
        new char[] {2,4,5,4,6},	/* 015 */
        new char[] {3,5,4,3,6},	/* 016 */
        new char[] {3,5,5,4,6},	/* 017 */
        new char[] {4,5,5,3,6},	/* 018 */
        new char[] {4,3,2,4,6},	/* 019 */
        new char[] {4,3,3,5,6},	/* 020 */
        new char[] {3,4,5,3,6},	/* 021 */
        new char[] {4,5,4,2,6},	/* 022 */
        new char[] {4,3,3,4,8},	/* 023 */
        new char[] {4,2,3,4,6},	/* 024 */
        new char[] {5,3,2,3,6},	/* 025 */
        new char[] {5,3,3,4,6},	/* 026 */
        new char[] {4,3,4,3,6},	/* 027 */
        new char[] {5,4,3,2,6},	/* 028 */
        new char[] {5,4,4,3,6},	/* 029 */
        new char[] {3,3,3,3,6},	/* 030 */
        new char[] {3,3,5,5,6},	/* 031 */
        new char[] {5,5,3,3,6},	/* 032 */
        new char[] {2,2,4,5,4},	/* 033 */
        new char[] {4,4,2,3,4},	/* 034 */
        new char[] {4,4,4,5,4},	/* 035 */
        new char[] {2,3,5,4,4},	/* 036 */
        new char[] {4,5,3,2,4},	/* 037 */
        new char[] {4,5,5,4,4},	/* 038 */
        new char[] {3,2,4,4,4},	/* 039 */
        new char[] {5,4,2,2,4},	/* 040 */
        new char[] {5,4,4,4,4},	/* 041 */
        new char[] {2,3,3,4,6},	/* 042 */
        new char[] {2,3,5,6,6},	/* 043 */
        new char[] {4,5,3,4,6},	/* 044 */
        new char[] {2,4,4,3,6},	/* 045 */
        new char[] {2,4,6,5,6},	/* 046 */
        new char[] {4,6,4,3,6},	/* 047 */
        new char[] {4,4,4,3,8},	/* 048 */
        new char[] {3,2,4,6,6},	/* 049 */
        new char[] {5,4,2,4,6},	/* 050 */
        new char[] {3,4,4,2,6},	/* 051 */
        new char[] {3,4,6,4,6},	/* 052 */
        new char[] {3,4,4,4,8},	/* 053 */
        new char[] {4,2,2,3,6},	/* 054 */
        new char[] {4,2,4,5,6},	/* 055 */
        new char[] {6,4,2,3,6},	/* 056 */
        new char[] {4,3,3,2,6},	/* 057 */
        new char[] {4,3,5,4,6},	/* 058 */
        new char[] {6,5,3,2,6},	/* 059 */
        new char[] {4,5,5,2,8},	/* 060 */
        new char[] {4,3,5,5,4},	/* 061 */
        new char[] {7,4,2,2,6},	/* 062 */
        new char[] {2,2,3,4,4},	/* 063 */
        new char[] {2,2,5,6,4},	/* 064 */
        new char[] {3,3,2,3,4},	/* 065 */
        new char[] {3,3,5,6,4},	/* 066 */
        new char[] {5,5,2,3,4},	/* 067 */
        new char[] {5,5,3,4,4},	/* 068 */
        new char[] {2,3,4,3,4},	/* 069 */
        new char[] {2,3,6,5,4},	/* 070 */
        new char[] {3,4,3,2,4},	/* 071 */
        new char[] {3,4,6,5,4},	/* 072 */
        new char[] {5,6,3,2,4},	/* 073 */
        new char[] {5,6,4,3,4},	/* 074 */
        new char[] {6,5,3,3,4},	/* 075 */
        new char[] {4,3,2,2,4},	/* 076 */
        new char[] {5,4,4,2,8},	/* 077 */
        new char[] {6,5,2,2,4},	/* 078 */
        new char[] {4,7,5,2,6},	/* 079 */
        new char[] {2,2,3,6,6},	/* 080 */
        new char[] {3,3,2,5,6},	/* 081 */
        new char[] {3,3,3,6,6},	/* 082 */
        new char[] {2,5,6,3,6},	/* 083 */
        new char[] {3,6,5,2,6},	/* 084 */
        new char[] {3,6,6,3,6},	/* 085 */
        new char[] {5,2,3,3,6},	/* 086 */
        new char[] {6,3,2,2,6},	/* 087 */
        new char[] {6,3,3,3,6},	/* 088 */
        new char[] {3,3,3,5,8},	/* 089 */
        new char[] {3,5,5,3,8},	/* 090 */
        new char[] {5,3,3,3,8},	/* 091 */
        new char[] {2,2,2,5,6},	/* 092 */
        new char[] {2,2,4,7,6},	/* 093 */
        new char[] {4,4,2,5,6},	/* 094 */
        new char[] {2,5,5,2,6},	/* 095 */
        new char[] {2,5,7,4,6},	/* 096 */
        new char[] {5,2,2,2,6},	/* 097 */
        new char[] {5,2,4,4,6},	/* 098 */
        new char[] {2,4,4,5,8},	/* 099 */
        new char[] {2,5,5,4,8},	/* 100 */
        new char[] {4,2,2,5,8},	/* 101 */
        new char[] {5,2,2,4,8},	/* 102 */
        new char[] {3,2,5,5,4},	/* 103 */
        new char[] {3,2,3,3,4},	/* 104 */
        new char[] {3,2,3,5,6},	/* 105 */
        new char[] {5,6,4,2,6},	/* 106 */
        new char[] {3,2,2,4,6}		/* 107 */
    };


}
