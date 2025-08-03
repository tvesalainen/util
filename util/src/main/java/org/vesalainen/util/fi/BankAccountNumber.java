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
package org.vesalainen.util.fi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vesalainen.util.BankingCode;

public class BankAccountNumber
{

    private static final char[] mask = new char[]
    {
        '2', '1', '2', '1', '2', '1', '2', '1', '2', '1', '2', '1', '2'
    };
    private char[] bban = new char[]
    {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
    };
    private String country = "FI";

    public BankAccountNumber(String bank, String account) throws NumberFormatException
    {
        init(bank, account);
    }

    public BankAccountNumber(int bank, int account)
    {
        init(bank, account);
    }

    public BankAccountNumber(String account) throws NumberFormatException
    {
        init(account.replace(" ", ""));
    }

    private static final Pattern BBAN = Pattern.compile("[0-9]+\\-[0-9]+");
    private static final Pattern IBAN = Pattern.compile("[A-Z]{2}[0-9]{16}");
    
    private void init(String account) throws NumberFormatException
    {
        Matcher matcher = IBAN.matcher(account);
        if (matcher.matches())
        {
            String bbanString = account.substring(4);
            country = account.substring(0,2);
            bban = bbanString.toCharArray();
            String iban = BankingCode.create(country, bbanString);
            if (!account.equals(iban))
            {
                throw new IllegalArgumentException(account + " illegal format");
            }
        }
        else
        {
            int idx = account.indexOf("-");
            if (idx == -1)
            {
                throw new IllegalArgumentException(account + " illegal format");
            }
            init(account.substring(0, idx), account.substring(idx + 1));
        }
    }
    /**
     * TODO formatting
     * @return 
     */
    public String getIBAN()
    {
        return BankingCode.format(BankingCode.create(country, new String(bban)));
    }
    private void init(String bank, String account) throws NumberFormatException
    {
        int b = Integer.parseInt(bank);
        int a = Integer.parseInt(account);
        init(b, a);
    }

    private void init(int bank, int account)
    {
        int ii = 0;
        String b = String.valueOf(bank);
        String a = String.valueOf(account);

        if (b.length() != 6)
        {
            throw new IllegalArgumentException(bank + " illegal length");
        }
        if (a.length() < 2)
        {
            throw new IllegalArgumentException(account + " too short");
        }
        if (a.length() > 8)
        {
            throw new IllegalArgumentException(account + " too long");
        }
        b.getChars(0, b.length(), bban, 0);
        if (b.startsWith("4") || b.startsWith("5"))
        {
            a.getChars(1, a.length(), bban, bban.length - a.length() + 1);
            a.getChars(0, 1, bban, 6);
        }
        else
        {
            a.getChars(0, a.length(), bban, bban.length - a.length());
        }
        checkDigit();
    }

    public void checkDigit()
    {
        int ii = 0;
        int chk = 0;
        int cd = 0;
        StringBuilder sb = new StringBuilder();
        for (ii = 0; ii < 13; ii++)
        {
            sb.append((bban[ii] - '0') * (mask[ii] - '0'));
        }
        for (ii = 0; ii < sb.length(); ii++)
        {
            chk += sb.charAt(ii) - '0';
        }
        cd = ((10 - (chk % 10)) % 10);
        if (cd != (bban[13] - '0'))
        {
            throw new IllegalArgumentException(this + " wrong check digit");
        }
    }

    public String toBankingBarcodeString()
    {
        int ii = 0;
        StringBuilder sb = new StringBuilder();
        for (ii = 0; ii < 14; ii++)
        {
            sb.append(bban[ii]);
        }
        return sb.toString();
    }

    public String toString()
    {
        int ii = 0;
        boolean zeros = false;
        StringBuilder sb = new StringBuilder();
        for (ii = 0; ii < 6; ii++)
        {
            sb.append(bban[ii]);
        }
        sb.append("-");
        if (bban[0] == '4' || bban[0] == '5')
        {
            sb.append(bban[6]);
            for (ii = 7; ii < 14; ii++)
            {
                if (bban[ii] == '0')
                {
                    if (zeros)
                    {
                        sb.append(bban[ii]);
                    }
                }
                else
                {
                    sb.append(bban[ii]);
                    zeros = true;
                }
            }
        }
        else
        {
            for (ii = 6; ii < 14; ii++)
            {
                if (bban[ii] == '0')
                {
                    if (zeros)
                    {
                        sb.append(bban[ii]);
                    }
                }
                else
                {
                    sb.append(bban[ii]);
                    zeros = true;
                }
            }
        }
        return sb.toString();
    }

    public int fill(char version, int start, char[] buf)
    {
        switch (version)
        {
            case '2':
                for (int ii = 0; ii < bban.length; ii++)
                {
                    buf[start + ii] = bban[ii];
                }
                return start + bban.length;
            case '4':
            case '5':
                String iban = BankingCode.create(country, new String(bban));
                char[] cb = iban.substring(2).toCharArray();
                for (int ii = 0; ii < cb.length; ii++)
                {
                    buf[start + ii] = cb[ii];
                }
                return start + cb.length;
            default:
                throw new IllegalArgumentException("wrong version "+version);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            BankAccountNumber ptn = new BankAccountNumber("FI4250001510000023");
            if (!"50001510000023".equals(ptn.toBankingBarcodeString()))
            {
                System.err.println("error");
            }
            ptn = new BankAccountNumber("123456-785");
            if (!"12345600000785".equals(ptn.toBankingBarcodeString()))
            {
                System.err.println("error");
            }
            ptn = new BankAccountNumber("423456-781");
            if (!"42345670000081".equals(ptn.toBankingBarcodeString()))
            {
                System.err.println("error");
            }
            System.err.println(ptn.getIBAN());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

}
