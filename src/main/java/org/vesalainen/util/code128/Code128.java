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


public class Code128 extends Code128Constants
{

    private char[] mybars = null;

    public Code128(String text)
    {
        String encoded = null;

        mybars = encode(text);
        encoded = decode(mybars);

        if (!text.equals(encoded))
        {
            throw new IllegalArgumentException("'" + text + "' <> '" + encoded + "'");
        }
    }

    public char[] getBars()
    {
        return mybars;
    }

    public static char[] encode(String txt)
    {
        int codeset;	/*
         * Code 0 = A ,...
         */
        int inidx = 0;		/*
         * Input index. Starting at 0
         */
        int digits;
        char[] bars = null;
        int iSum;
        int iChk;
        int iBar;
        int iPos;
        int iBars = 0;
        StringBuffer sb = new StringBuffer();
        StringBuffer text = new StringBuffer(txt);

        /*
         * Determine the start character
         */
        /*
         * a1) If the data begins with 4 or more numeric data characters,
         */
        /*
         * use start character C;
         */
        if (countDigits(text) >= 4)
        {
            codeset = CODEC;
            sb.append(STARTC);
        }
        else
        {
            /*
             * a2) If an ASCII control character occurs in the data before any
             */
            /*
             * lower case character, use start character A.
             */
            if (ctrlBeforeLower(text))
            {
                codeset = CODEA;
                sb.append(STARTA);
            }
            else
            {
                /*
                 * a3) Otherwise, use start character B.
                 */

                codeset = CODEB;
                sb.append(STARTB);
            }
        }
        /*
         * b)	If Start character C is used and the data begins with an odd
         */
        /*
         * number of numeric data characters, insert a code set A or code
         */
        /*
         * set B before the last digit, following rules a2) and a3) above
         */
        /*
         * to determine between code subsets A and B.
         */
        if (codeset == CODEC)
        {
            inidx = packDigits(sb, text);
            if (inidx < text.length())
            {
                if (isdigit(text.charAt(inidx)))
                {
                    if (ctrlBeforeLower(text.substring(inidx)))
                    {
                        sb.append(tabFunc[SHIFTA][codeset]);
                        codeset = CODEA;
                    }
                    else
                    {
                        sb.append(tabFunc[SHIFTB][codeset]);
                        codeset = CODEB;
                    }
                }
            }
        }

        while (inidx < text.length())
        {
            if (text.charAt(inidx) > 127)
            {
                sb.append(tabFunc[FNC4][codeset]);
                text.setCharAt(inidx, (char) (text.charAt(inidx) - 128));
            }
            /*
             * c)	If 4 or more numeric data characters occur together when in
             */
            /*
             * code subsets A or B:
             */
            if (codeset == CODEA || codeset == CODEB)
            {
                digits = countDigits(text.substring(inidx));
                if (digits >= 4)
                {

                    /*
                     * c1)	If there is an even number of numeric data
                     * characters, insert
                     */
                    /*
                     * a code set C character before the first digit to change
                     * to code
                     */
                    /*
                     * subset C;
                     */
                    if (even(digits))
                    {
                        sb.append(tabFunc[SHIFTC][codeset]);
                        codeset = CODEC;
                    }
                    else /*
                     * c2)	If there is an odd number of numeric data characters,
                     * insert
                     */ /*
                     * a code set C character immediately after the first
                     * numeric digit
                     */ /*
                     * to change to code subset C.
                     */ {
                        sb.append(codeIn(text.charAt(inidx++), codeset));
                        sb.append(tabFunc[SHIFTC][codeset]);
                        codeset = CODEC;
                    }
                }
            }
            /*
             * d)	When in code subset B and an ASCII control character occurs in
             * the data:
             */
            if (codeset == CODEB)
            {
                if (control(text.charAt(inidx)))
                {
                    /*
                     * d1)	If there is a lower case character immediately
                     * following the control
                     */
                    /*
                     * character, insert a shift character before the control
                     * character;
                     */
                    if (lower(text.charAt(inidx + 1)))
                    {
                        sb.append(tabFunc[SHIFT][codeset]);
                        sb.append(codeIn(text.charAt(inidx++), CODEA));
                    }
                    /*
                     * d2)	Otherwise, insert a code set A character before the
                     * control
                     */
                    /*
                     * character to change to code subset A.
                     */
                    else
                    {
                        sb.append(tabFunc[SHIFTA][codeset]);
                        codeset = CODEA;
                        sb.append(codeIn(text.charAt(inidx++), codeset));
                    }
                }
                else
                {
                    sb.append(codeIn(text.charAt(inidx++), codeset));
                }
            }
            /*
             * e)	When in code subset A and a lower case character occurs in the
             * data:
             */
            if (codeset == CODEA)
            {
                if (lower(text.charAt(inidx)))
                {
                    /*
                     * e1)	If following that character, a control character
                     * occurs in the
                     */
                    /*
                     * data before the occurrence of another lower case
                     * character,
                     */
                    /*
                     * insert a shift character before the lower case character;
                     */
                    if (control(text.charAt(inidx + 1)))
                    {
                        sb.append(tabFunc[SHIFT][codeset]);
                        sb.append(codeIn(text.charAt(inidx++), CODEB));
                    }
                    /*
                     * e2)	Otherwise, insert a code set B character before the
                     * lower case
                     */
                    /*
                     * character to change to code subset B.
                     */
                    else
                    {
                        sb.append(tabFunc[SHIFTB][codeset]);
                        codeset = CODEB;
                        sb.append(codeIn(text.charAt(inidx++), codeset));
                    }
                }
                else
                {
                    sb.append(codeIn(text.charAt(inidx++), codeset));
                }
            }

            /*
             * f)	When in code subset C and a non-numeric character occurs in
             * the data,
             */
            /*
             * insert a code set A or code set B before that character,
             * following rules
             */
            /*
             * a2) and a3) to determine between code subsets A and B.
             */
            if (codeset == CODEC)
            {
                if (!isdigit(text.charAt(inidx)))
                {
                    if (ctrlBeforeLower(text.substring(inidx)))
                    {
                        sb.append(tabFunc[SHIFTA][codeset]);
                        codeset = CODEA;
                        sb.append(codeIn(text.charAt(inidx++), codeset));
                    }
                    else
                    {
                        sb.append(tabFunc[SHIFTB][codeset]);
                        codeset = CODEB;
                        sb.append(codeIn(text.charAt(inidx++), codeset));
                    }
                }
                else
                {
                    inidx += packDigits(sb, text.substring(inidx));
                }
            }
        }

        /*
         * Symbol check character
         */
        iSum = sb.charAt(0);	/*
         * add start character
         */
        for (iChk = 1; iChk < sb.length(); iChk++)
        {
            iSum += sb.charAt(iChk) * iChk;
        }
        sb.append((char) (iSum % 103));

        iBars = 0;
        bars = new char[sb.length() * 6 + 7];
        for (iPos = 0; iPos < sb.length(); iPos++)
        {
            for (iBar = 0; iBar < 6; iBar++)
            {
                bars[iBars++] = tabEncode[sb.charAt(iPos)][iBar];
            }
        }
        /*
         * stop character
         */
        bars[iBars++] = 2;
        bars[iBars++] = 3;
        bars[iBars++] = 3;
        bars[iBars++] = 1;
        bars[iBars++] = 1;
        bars[iBars++] = 1;
        bars[iBars++] = 2;

        return bars;
    }

    public static int countDigits(StringBuffer text)
    {
        return countDigits(text.toString());
    }

    public static int countDigits(String text)
    {
        int cnt = 0;
        int ii;

        for (ii = 0; ii < text.length(); ii++)
        {
            if (isdigit(text.charAt(ii)))
            {
                cnt++;
            }
            else
            {
                break;
            }
        }
        return cnt;
    }

    public static boolean ctrlBeforeLower(StringBuffer text)
    {
        return ctrlBeforeLower(text.toString());
    }

    public static boolean ctrlBeforeLower(String text)
    {
        int ii = 0;
        for (ii = 0; ii < text.length(); ii++)
        {
            if (control(text.charAt(ii)))
            {
                return true;
            }
            if (lower(text.charAt(ii)))
            {
                return false;
            }
        }
        return false;
    }

    public static int packDigits(StringBuffer sb, StringBuffer txt)
    {
        return packDigits(sb, txt.toString());
    }

    public static int packDigits(StringBuffer sb, String txt)
    {
        int iCount = 0;
        int ii = 0;
        while (ii + 1 < txt.length())
        {
            if (isdigit(txt.charAt(ii)) && isdigit(txt.charAt(ii + 1)))
            {
                sb.append((char) (10 * (txt.charAt(ii) - '0') + (txt.charAt(ii + 1) - '0')));
                iCount += 2;
                ii += 2;
            }
            else
            {
                break;
            }
        }
        return iCount;
    }

    public static char codeIn(char c, int codeset)
    {
        char i;

        for (i = 0; i < 96; i++)
        {
            if (codeset == CODEA)
            {
                if (c == tabA[i])
                {
                    return i;
                }
            }
            else
            {
                if (c == tabB[i])
                {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("char " + c + " not in codeset " + codeset);
    }

    public static String decode(char[] bars)
    {
        char ii, jj, kk;
        char bar;
        char space;
        char p;
        char[] e = new char[5];
        char[] E = new char[5];
        char b1;
        char b2;
        char b3;
        char eind;
        char S;
        boolean decode_succeeded = false;

        char[] b = new char[6];
        char cDecoded = 0;
        int iMode = CODEA;	/*
         * A, B or C
         */
        boolean bShift = false;
        boolean bAdd128 = false;
        int iTextSize = 0;
        int iChkSum = 0;
        boolean bChkSumOk = false;
        StringBuffer sb = new StringBuffer();
        StringBuffer res = new StringBuffer();

        for (ii = 0; ii <= bars.length - 7; ii += 6)
        {
            for (kk = 0; kk < 6; kk++)
            {
                b[kk] = bars[ii + kk];
            }
            p = (char) (b[0] + b[1] + b[2] + b[3] + b[4] + b[5]);
            for (eind = 0; eind < 6; eind++)
            {
                if (0.5 * p / 11 < b[eind] && b[eind] <= 1.5 * p / 11)
                {
                    b[eind] = 1;
                }
                else if (1.5 * p / 11 < b[eind] && b[eind] <= 2.5 * p / 11)
                {
                    b[eind] = 2;
                }
                else if (2.5 * p / 11 < b[eind] && b[eind] <= 3.5 * p / 11)
                {
                    b[eind] = 3;
                }
                else if (3.5 * p / 11 < b[eind] && b[eind] <= 4.5 * p / 11)
                {
                    b[eind] = 4;
                }
                else if (4.5 * p / 11 < b[eind] && b[eind] <= 5.5 * p / 11)
                {
                    b[eind] = 5;
                }
                else if (5.5 * p / 11 < b[eind] && b[eind] <= 6.5 * p / 11)
                {
                    b[eind] = 6;
                }
                else if (6.5 * p / 11 < b[eind] && b[eind] <= 7.5 * p / 11)
                {
                    b[eind] = 7;
                }
            }
            bar = (char) ((b[0] + b[2] + b[4]));
            space = (char) ((b[1] + b[3] + b[5]));
            if (odd(bar))
            {
                throw new IllegalArgumentException("ii=" + ii + " bar(" + bar + ") is not odd!");
            }
            if (even(space))
            {
                throw new IllegalArgumentException("ii=" + ii + " space(" + space + ") is not even!");
            }
            /*
             * Decode it
             */
            b1 = b[0];
            b2 = b[2];
            b3 = b[4];
            e[1] = (char) (b[0] + b[1]);
            e[2] = (char) (b[1] + b[2]);
            e[3] = (char) (b[2] + b[3]);
            e[4] = (char) (b[3] + b[4]);

            for (eind = 1; eind < 5; eind++)
            {
                if (1.5 < e[eind] && e[eind] <= 2.5)
                {
                    E[eind] = 2;
                }
                else if (2.5 < e[eind] && e[eind] <= 3.5)
                {
                    E[eind] = 3;
                }
                else if (3.5 < e[eind] && e[eind] <= 4.5)
                {
                    E[eind] = 4;
                }
                else if (4.5 < e[eind] && e[eind] <= 5.5)
                {
                    E[eind] = 5;
                }
                else if (5.5 < e[eind] && e[eind] <= 6.5)
                {
                    E[eind] = 6;
                }
                else if (6.5 < e[eind] && e[eind] <= 7.5)
                {
                    E[eind] = 7;
                }
            }
            decode_succeeded = false;
            for (jj = 0; jj < tabDecode.length; jj++)
            {
                if ((E[1] == tabDecode[jj][0]
                        && E[2] == tabDecode[jj][1]
                        && E[3] == tabDecode[jj][2]
                        && E[4] == tabDecode[jj][3]))
                {
                    cDecoded = jj;
                    decode_succeeded = true;
                    break;
                }
            }
            if (decode_succeeded == false)
            {
                throw new IllegalArgumentException("decoding failed ii=" + (int) ii);
            }
            S = tabDecode[cDecoded][4];
            if (bar != S)
            {
                throw new IllegalArgumentException("ii=" + ii + " Bar checksum failed!");
            }
            if (!((S - 1.75) < (b1 + b2 + b3) && (b1 + b2 + b3) < (S + 1.75)))
            {
                throw new IllegalArgumentException("ii=" + ii + " One module edge error!");
            }

            if (cDecoded > 107)
            {
                throw new IllegalArgumentException("Decoded > 107");
            }
            sb.append(cDecoded);
        }


        iChkSum = sb.charAt(0);

        for (ii = 1; ii < sb.length() - 2; ii++)
        {
            iChkSum += sb.charAt(ii) * ii;
        }
        if (sb.charAt(sb.length() - 2) != iChkSum % 103)
        {
            throw new IllegalArgumentException("Symbol checksum of '" + sb.toString() + "' failed! sum = " + (int) sb.charAt(sb.length() - 2) + " should be " + iChkSum % 103);
        }


        for (ii = 0; ii < sb.length() - 2; ii++)
        {
            cDecoded = sb.charAt(ii);
            switch (iMode)
            {
                case CODEA:
                    switch (cDecoded)
                    {
                        case 96:	/*
                             * FNC3
                             */
                            break;
                        case 97:	/*
                             * FNC2
                             */
                            break;
                        case 98:	/*
                             * SHIFT
                             */
                            iMode = CODEB;
                            bShift = true;
                            break;
                        case 99:	/*
                             * CODE C
                             */
                            iMode = CODEC;
                            break;
                        case 100:	/*
                             * CODE B
                             */
                            iMode = CODEB;
                            break;
                        case 101:	/*
                             * FNC4
                             */
                            bAdd128 = true;
                            break;
                        case 102:	/*
                             * FNC1
                             */
                            break;
                        case 103:	/*
                             * START (CODE A)
                             */
                            iMode = CODEA;
                            break;
                        case 104:	/*
                             * START (CODE B)
                             */
                            iMode = CODEB;
                            break;
                        case 105:	/*
                             * START (CODE C)
                             */
                            iMode = CODEC;
                            break;
                        case 106:	/*
                             * STOP FORWARDS
                             */
                            if (bChkSumOk)
                            {
                                return res.toString();
                            }
                            else
                            {
                                throw new IllegalArgumentException("Check Sum failed");
                            }
                        case 107:	/*
                             * STOP BACKWARDS
                             */
                            throw new IllegalArgumentException("STOP BACKWARDS");
                        default:
                            if (bAdd128 == true)
                            {
                                res.append((char) (tabA[ cDecoded] + 128));
                                bAdd128 = false;
                            }
                            else
                            {
                                res.append(tabA[ cDecoded]);
                            }
                            if (bShift)
                            {
                                iMode = CODEB;
                                bShift = false;
                            }
                            break;
                    }
                    break;
                case CODEB:
                    switch (cDecoded)
                    {
                        case 96:	/*
                             * FNC3
                             */
                            break;
                        case 97:	/*
                             * FNC2
                             */
                            break;
                        case 98:	/*
                             * SHIFT
                             */
                            iMode = CODEA;
                            bShift = true;
                            break;
                        case 99:	/*
                             * CODE C
                             */
                            iMode = CODEC;
                            break;
                        case 100:	/*
                             * FNC4
                             */
                            bAdd128 = true;
                            break;
                        case 101:	/*
                             * CODE A
                             */
                            iMode = CODEA;
                            break;
                        case 102:	/*
                             * FNC1
                             */
                            break;
                        case 103:	/*
                             * START (CODE A)
                             */
                            iMode = CODEA;
                            break;
                        case 104:	/*
                             * START (CODE B)
                             */
                            iMode = CODEB;
                            break;
                        case 105:	/*
                             * START (CODE C)
                             */
                            iMode = CODEC;
                            break;
                        case 106:	/*
                             * STOP FORWARDS
                             */
                            if (bChkSumOk)
                            {
                                return res.toString();
                            }
                            else
                            {
                                throw new IllegalArgumentException("Check Sum failed");
                            }
                        case 107:	/*
                             * STOP BACKWARDS
                             */
                            throw new IllegalArgumentException("STOP BACKWARDS");
                        default:
                            if (bAdd128 == true)
                            {
                                res.append((char) (tabB[ cDecoded] + 128));
                                bAdd128 = false;
                            }
                            else
                            {
                                res.append(tabB[ cDecoded]);
                            }
                            if (bShift)
                            {
                                iMode = CODEA;
                                bShift = false;
                            }
                            break;
                    }
                    break;
                case CODEC:
                    switch (cDecoded)
                    {
                        case 100:	/*
                             * CODE B
                             */
                            iMode = CODEB;
                            break;
                        case 101:	/*
                             * CODE A
                             */
                            iMode = CODEA;
                            break;
                        case 102:	/*
                             * FNC1
                             */
                            break;
                        case 103:	/*
                             * START (CODE A)
                             */
                            iMode = CODEA;
                            break;
                        case 104:	/*
                             * START (CODE B)
                             */
                            iMode = CODEB;
                            break;
                        case 105:	/*
                             * START (CODE C)
                             */
                            iMode = CODEC;
                            break;
                        case 106:	/*
                             * STOP FORWARDS
                             */
                            if (bChkSumOk)
                            {
                                return res.toString();
                            }
                            else
                            {
                                throw new IllegalArgumentException("Check Sum failed");
                            }
                        case 107:	/*
                             * STOP BACKWARDS
                             */
                            throw new IllegalArgumentException("STOP BACKWARDS");
                        default:
                            if (cDecoded > 99)
                            {
                                throw new IllegalArgumentException("Decoded > 99");
                            }
                            res.append((int) (cDecoded / 10));
                            res.append((int) (cDecoded % 10));
                            break;
                    }
                    break;
            }
        }
        return res.toString();
    }

    public static boolean odd(int n)
    {
        return ((n) % 2) != 0;
    }

    public static boolean even(int n)
    {
        return !odd(n);
    }

    public static boolean lower(char c)
    {
        return ((c) >= 96 && (c) <= 127);
    }

    public static boolean control(char c)
    {
        return ((c) >= 0 && (c) <= 31);
    }

    public static boolean isdigit(char c)
    {
        return ((c) >= '0' && (c) <= '9');
    }

    public static void main(String[] args)
    {
        int l = 0;
        String sl = null;
        try
        {
            for (l = 0; l < 1000; l++)
            {
                sl = String.valueOf(l);
                Code128 c = new Code128(sl);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
