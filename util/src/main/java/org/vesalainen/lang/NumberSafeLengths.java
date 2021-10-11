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
 * Provides byte, short, int and long safe length in different radixes. Safeway
 * length is printed max value length - 1.
 * @author Timo Vesalainen
 */
public class NumberSafeLengths
{

    static final int[] ByteSafeLength = new int[]
    {
        0,
        0,
        6,
        4,
        3,
        3,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1
    };
    static final int[] ShortSafeLength = new int[]
    {
        0,
        0,
        14,
        9,
        7,
        6,
        5,
        5,
        4,
        4,
        4,
        4,
        4,
        4,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        2,
        2,
        2,
        2
    };
    static final int[] IntSafeLength = new int[]
    {
        0,
        0,
        30,
        19,
        15,
        13,
        11,
        11,
        10,
        9,
        9,
        8,
        8,
        8,
        8,
        7,
        7,
        7,
        7,
        7,
        7,
        7,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        6
    };
    static final int[] LongSafeLength = new int[]
    {
        0,
        0,
        62,
        39,
        31,
        27,
        24,
        22,
        20,
        19,
        18,
        18,
        17,
        17,
        16,
        16,
        15,
        15,
        15,
        14,
        14,
        14,
        14,
        13,
        13,
        13,
        13,
        13,
        13,
        12,
        12,
        12,
        12,
        12,
        12,
        12
    };
}
