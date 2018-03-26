/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.riff;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum FormatCode
{
    /**
     * Microsoft Pulse Code Modulation (PCM) format
     */
    WAVE_FORMAT_PCM(0x1),
    WAVE_FORMAT_IEEE_FLOAT(0x3),
    WAVE_FORMAT_ALAW(0x6),
    WAVE_FORMAT_MULAW(0x7),
    WAVE_FORMAT_MPEG(0x0050),
    IBM_FORMAT_MULAW (0x0101),
    IBM_FORMAT_ALAW (0x0102),
    IBM_FORMAT_ADPCM (0x0103),
    WAVE_FORMAT_EXTENSIBLE(0xfffe)
    ;
    private int code;

    private FormatCode(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
    
    public static final FormatCode of(int code)
    {
        switch (code)
        {
            case 0x1:
                return WAVE_FORMAT_PCM;
            case 0x3:
                return WAVE_FORMAT_IEEE_FLOAT;
            case 0x6:
                return WAVE_FORMAT_ALAW;
            case 0x7:
                return WAVE_FORMAT_MULAW;
            case 0x0050:
                return WAVE_FORMAT_MPEG;
            case 0xfffe:
                return WAVE_FORMAT_EXTENSIBLE;
            case 0x0101:
                return IBM_FORMAT_MULAW;
            case 0x0102:
                return IBM_FORMAT_ALAW;
            case 0x0103:
                return IBM_FORMAT_ADPCM;
            default:
                throw new UnsupportedOperationException(code+" not supported format code");
        }
    }
}
