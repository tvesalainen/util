/*
 * Copyright (C) 2010 Timo Vesalainen
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

public class PankkiTiliNumero
{

    private char[] _chk = new char[]
    {
        '2', '1', '2', '1', '2', '1', '2', '1', '2', '1', '2', '1', '2'
    };
    private char[] _buf = new char[]
    {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
    };

    public PankkiTiliNumero(String bank, String account) throws NumberFormatException
    {
        init(bank, account);
    }

    public PankkiTiliNumero(int bank, int account)
    {
        init(bank, account);
    }

    public PankkiTiliNumero(String account) throws NumberFormatException
    {
        init(account);
    }

    private void init(String account) throws NumberFormatException
    {
        int idx = account.indexOf("-");
        if (idx == -1)
        {
            throw new IllegalArgumentException(account + " illegal format");
        }
        init(account.substring(0, idx), account.substring(idx + 1));
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
        b.getChars(0, b.length(), _buf, 0);
        if (b.startsWith("4") || b.startsWith("5"))
        {
            a.getChars(1, a.length(), _buf, _buf.length - a.length() + 1);
            a.getChars(0, 1, _buf, 6);
        }
        else
        {
            a.getChars(0, a.length(), _buf, _buf.length - a.length());
        }
        checkDigit();
    }

    public void checkDigit()
    {
        int ii = 0;
        int chk = 0;
        int cd = 0;
        StringBuffer sb = new StringBuffer();
        for (ii = 0; ii < 13; ii++)
        {
            sb.append((_buf[ii] - '0') * (_chk[ii] - '0'));
        }
        for (ii = 0; ii < sb.length(); ii++)
        {
            chk += sb.charAt(ii) - '0';
        }
        cd = ((10 - (chk % 10)) % 10);
        if (cd != (_buf[13] - '0'))
        {
            throw new IllegalArgumentException(this + " wrong check digit");
        }
    }

    public String toPankkiViivaKoodiString()
    {
        int ii = 0;
        StringBuffer sb = new StringBuffer();
        for (ii = 0; ii < 14; ii++)
        {
            sb.append(_buf[ii]);
        }
        return sb.toString();
    }

    public String toString()
    {
        int ii = 0;
        boolean zeros = false;
        StringBuffer sb = new StringBuffer();
        for (ii = 0; ii < 6; ii++)
        {
            sb.append(_buf[ii]);
        }
        sb.append("-");
        if (_buf[0] == '4' || _buf[0] == '5')
        {
            sb.append(_buf[6]);
            for (ii = 7; ii < 14; ii++)
            {
                if (_buf[ii] == '0')
                {
                    if (zeros)
                    {
                        sb.append(_buf[ii]);
                    }
                }
                else
                {
                    sb.append(_buf[ii]);
                    zeros = true;
                }
            }
        }
        else
        {
            for (ii = 6; ii < 14; ii++)
            {
                if (_buf[ii] == '0')
                {
                    if (zeros)
                    {
                        sb.append(_buf[ii]);
                    }
                }
                else
                {
                    sb.append(_buf[ii]);
                    zeros = true;
                }
            }
        }
        return sb.toString();
    }

    public int fill(int start, char[] buf)
    {
        int ii;
        for (ii = 0; ii < _buf.length; ii++)
        {
            buf[start + ii] = _buf[ii];
        }
        return start + _buf.length;
    }

    public static void main(String[] args)
    {
        try
        {
            PankkiTiliNumero ptn = new PankkiTiliNumero(args[0]);
            System.out.println(ptn + "  " + ptn.toPankkiViivaKoodiString());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
