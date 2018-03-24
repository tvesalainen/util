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
public enum Info
{
    /**
     * Archival Location. Indicates where the subject of the file is archived.
     */
    IARL,
    /**
     * Artist. Lists the artist of the original subject of the file. For example,
    “Michaelangelo.”
     */
    IART, 
    /**
     * Commissioned. Lists the name of the person or organization that
    commissioned the subject of the file. For example, “ Pope Julian II.”
     */
    ICMS, 
    /**
     * Comments. Provides general comments about the file or the subject of the
    file. If the comment is several sentences long, end each sentence with a
    period. Do not include newline characters.
     */
    ICMT, 
    /**
     * Copyright. Records the copyright information for the file. For example,
    “Copyright Encyclopedia International 1991.” If there are multiple
    copyrights, separate them by a semicolon followed by a space.
     */
    ICOP, 
    /**
     * Creation date. Specifies the date the subject of the file was created. List dates
    in year-month-day format, padding one-digit months and days with a zero on
    the left. For example, “ 1553-05-03” for May 3, 1553.
     */
    ICRD, 
    /**
     * Cropped. Describes whether an image has been cropped and, if so, how it was
    cropped. For example, “ lower right corner.”
     */
    ICRP, 
    /**
     * Dimensions. Specifies the size of the original subject of the file. For example,
    “ 8.5 in h, 11 in w.”
     */
    IDIM, 
    /**
     * Dots Per Inch. Stores dots per inch setting of the digitizer used to produce the
    file, such as “ 300.”
     */
    IDPI, 
    /**
     * Engineer. Stores the name of the engineer who worked on the file. If there are
    multiple engineers, separate the names by a semicolon and a blank. For
    example, “ Smith, John; Adams, Joe.”
     */
    IENG, 
    /**
     * Genre. Describes the original work, such as, “ landscape,” “ portrait,” “ still
    life,” etc.
     */
    IGNR, 
    /**
     * Keywords. Provides a list of keywords that refer to the file or subject of the
    file. Separate multiple keywords with a semicolon and a blank. For example,
    “ Seattle; aerial view; scenery.”
     */
    IKEY, 
    /**
     * Lightness. Describes the changes in lightness settings on the digitizer required
    to produce the file. Note that the format of this information depends on
    hardware used.
     */
    ILGT, 
    /**
     * Medium. Describes the original subject of the file, such as, “ computer
    image,” “ drawing,” “ lithograph,” and so forth.
     */
    IMED, 
    /**
     * Name. Stores the title of the subject of the file, such as, “ Seattle From
    Above.”
     */
    INAM, 
    /**
     * Palette Setting. Specifies the number of colors requested when digitizing an
    image, such as “ 256.”
     */
    IPLT, 
    /**
     * Product. Specifies the name of the title the file was originally intended for,
    such as “Encyclopedia of Pacific Northwest Geography.”
     */
    IPRD, 
    /**
     * Subject. Describes the conbittents of the file, such as “Aerial view of
    Seattle.”
     */
    ISBJ, 
    /**
     * Software. Identifies the name of the software package used to create the file,
    such as “Microsoft WaveEdit.”
     */
    ISFT, 
    /**
     * Sharpness. Identifies the changes in sharpness for the digitizer required to
    produce the file (the format depends on the hardware used).
     */
    ISHP, 
    /**
     * Source. Identifies the name of the person or organization who supplied the
    original subject of the file. For example, “ Trey Research.”
     */
    ISRC, 
    /**
     * Source Form. Identifies the original form of the material that was digitized,
    such as “ slide,” “ paper,” “map,” and so forth. This is not necessarily the
    same as IMED.
     */
    ISRF, 
    /**
     * Technician. Identifies the technician who digitized the subject file. For
    example, “ Smith, John.
     */
    ITCH 
}
