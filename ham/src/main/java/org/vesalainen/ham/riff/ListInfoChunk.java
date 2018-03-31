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

import java.io.IOException;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import static org.vesalainen.ham.riff.Info.*;
import org.vesalainen.nio.channels.BufferedFileBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ListInfoChunk extends ContainerChunk
{
    protected Map<Info, String> info;

    public ListInfoChunk(ContainerChunk listInfoChunk)
    {
        super(listInfoChunk);
        if (!"INFO".equals(listInfoChunk.getType()))
        {
            throw new IllegalArgumentException("not a INFO Chunk");
        }
        this.info = new EnumMap<>(Info.class);
        for (Chunk chunk : subChunks.valueSet())
        {
            try
            {
                InfoChunk ic = new InfoChunk(chunk);
                info.put(ic.getInfo(), ic.getText());
            }
            catch (IllegalArgumentException ex)
            {
                warning("unknown %s", chunk);
            }
        }
    }

    public ListInfoChunk()
    {
        this(new EnumMap<>(Info.class));
    }
    public ListInfoChunk(Map<Info, String> info)
    {
        super("LIST", "INFO");
        this.info = info;
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        bb.put(type, ISO_8859_1);
        for (Map.Entry<Info, String> e : info.entrySet())
        {
            InfoChunk infoChunk = new InfoChunk(e.getKey(), e.getValue());
            infoChunk.store(bb);
        }
    }

    public boolean isEmpty()
    {
        return info.isEmpty();
    }
    @Override
    public ListInfoChunk clone()
    {
        return new ListInfoChunk(new EnumMap<>(info));
    }
    /**
     * Add given meta-data
     * @param listInfoChunk 
     */
    public void addInfo(ListInfoChunk listInfoChunk)
    {
        this.info.putAll(listInfoChunk.info);
    }
    /**
     * Clears this meta-data and add all given meta-data.
     * @param listInfoChunk 
     */
    public void setInfo(ListInfoChunk listInfoChunk)
    {
        this.info.clear();
        this.info.putAll(listInfoChunk.info);
    }

    /**
     * Archival Location. Indicates where the subject of the file is archived.
     *
     * @param text
     */
    public void setArchivalLocation(String text)
    {
        info.put(IARL, text);
    }

    /**
     * Archival Location. Indicates where the subject of the file is archived.
     *
     * @return
     */
    public String getArchivalLocation()
    {
        return info.get(IARL);
    }

    /**
     * Artist. Lists the artist of the original subject of the file. For
     * example, “Michaelangelo.”
     *
     * @param text
     */
    public void setArtist(String text)
    {
        info.put(IART, text);
    }

    /**
     * Artist. Lists the artist of the original subject of the file. For
     * example, “Michaelangelo.”
     *
     * @return
     */
    public String getArtist()
    {
        return info.get(IART);
    }

    /**
     * Commissioned. Lists the name of the person or organization that
     * commissioned the subject of the file. For example, “ Pope Julian II.”
     *
     * @param text
     */
    public void setCommissioned(String text)
    {
        info.put(ICMS, text);
    }

    /**
     * Commissioned. Lists the name of the person or organization that
     * commissioned the subject of the file. For example, “ Pope Julian II.”
     *
     * @return
     */
    public String getCommissioned()
    {
        return info.get(ICMS);
    }

    /**
     * Comments. Provides general comments about the file or the subject of the
     * file. If the comment is several sentences long, end each sentence with a
     * period. Do not include newline characters.
     *
     * @param text
     */
    public void setComments(String text)
    {
        info.put(ICMT, text);
    }

    /**
     * Comments. Provides general comments about the file or the subject of the
     * file. If the comment is several sentences long, end each sentence with a
     * period. Do not include newline characters.
     *
     * @return
     */
    public String getComments()
    {
        return info.get(ICMT);
    }

    /**
     * Copyright. Records the copyright information for the file. For example,
     * “Copyright Encyclopedia International 1991.” If there are multiple
     * copyrights, separate them by a semicolon followed by a space. * @param
     * text
     *
     * @param text
     */
    public void setCopyright(String text)
    {
        info.put(ICOP, text);
    }

    /**
     * Copyright. Records the copyright information for the file. For example,
     * “Copyright Encyclopedia International 1991.” If there are multiple
     * copyrights, separate them by a semicolon followed by a space. * @param
     * text
     *
     * @return
     */
    public String getCopyright()
    {
        return info.get(ICOP);
    }

    /**
     * Creation date. Specifies the date the subject of the file was created.
     * List dates in year-month-day format, padding one-digit months and days
     * with a zero on the left. For example, “ 1553-05-03” for May 3, 1553.
     *
     * @param date
     */
    public void setCreation(LocalDate date)
    {
        info.put(ICRD, date.toString());
    }

    /**
     * Creation date. Specifies the date the subject of the file was created.
     * List dates in year-month-day format, padding one-digit months and days
     * with a zero on the left. For example, “ 1553-05-03” for May 3, 1553.
     *
     * @return
     */
    public String getCreation()
    {
        return info.get(ICRD);
    }

    /**
     * Cropped. Describes whether an image has been cropped and, if so, how it
     * was cropped. For example, “ lower right corner.”
     *
     * @param text
     */
    public void setCropped(String text)
    {
        info.put(ICRP, text);
    }

    /**
     * Cropped. Describes whether an image has been cropped and, if so, how it
     * was cropped. For example, “ lower right corner.”
     *
     * @return
     */
    public String getCropped()
    {
        return info.get(ICRP);
    }

    /**
     * Dimensions. Specifies the size of the original subject of the file. For
     * example, “ 8.5 in h, 11 in w.”
     *
     * @param text
     */
    public void setDimensions(String text)
    {
        info.put(IDIM, text);
    }

    /**
     * Dimensions. Specifies the size of the original subject of the file. For
     * example, “ 8.5 in h, 11 in w.”
     *
     * @return
     */
    public String getDimensions()
    {
        return info.get(IDIM);
    }

    /**
     * Dots Per Inch. Stores dots per inch setting of the digitizer used to
     * produce the file, such as “ 300.”
     *
     * @param text
     */
    public void setDotsPerInch(String text)
    {
        info.put(IDPI, text);
    }

    /**
     * Dots Per Inch. Stores dots per inch setting of the digitizer used to
     * produce the file, such as “ 300.”
     *
     * @return
     */
    public String getDotsPerInch()
    {
        return info.get(IDPI);
    }

    /**
     * Engineer. Stores the name of the engineer who worked on the file. If
     * there are multiple engineers, separate the names by a semicolon and a
     * blank. For example, “ Smith, John; Adams, Joe.”
     *
     * @param text
     */
    public void setEngineer(String text)
    {
        info.put(IENG, text);
    }

    /**
     * Engineer. Stores the name of the engineer who worked on the file. If
     * there are multiple engineers, separate the names by a semicolon and a
     * blank. For example, “ Smith, John; Adams, Joe.”
     *
     * @return
     */
    public String getEngineer()
    {
        return info.get(IENG);
    }

    /**
     * Genre. Describes the original work, such as, “ landscape,” “ portrait,” “
     * still life,” etc.
     *
     * @param text
     */
    public void setGenre(String text)
    {
        info.put(IGNR, text);
    }

    /**
     * Genre. Describes the original work, such as, “ landscape,” “ portrait,” “
     * still life,” etc.
     *
     * @return
     */
    public String getGenre()
    {
        return info.get(IGNR);
    }

    /**
     * Keywords. Provides a list of keywords that refer to the file or subject
     * of the file. Separate multiple keywords with a semicolon and a blank. For
     * example, “ Seattle; aerial view; scenery.”
     *
     * @param text
     */
    public void setKeywords(String text)
    {
        info.put(IKEY, text);
    }

    /**
     * Keywords. Provides a list of keywords that refer to the file or subject
     * of the file. Separate multiple keywords with a semicolon and a blank. For
     * example, “ Seattle; aerial view; scenery.”
     *
     * @return
     */
    public String getKeywords()
    {
        return info.get(IKEY);
    }

    /**
     * Lightness. Describes the changes in lightness settings on the digitizer
     * required to produce the file. Note that the format of this information
     * depends on hardware used.
     *
     * @param text
     */
    public void setLightness(String text)
    {
        info.put(ILGT, text);
    }

    /**
     * Lightness. Describes the changes in lightness settings on the digitizer
     * required to produce the file. Note that the format of this information
     * depends on hardware used.
     *
     * @return
     */
    public String getLightness()
    {
        return info.get(ILGT);
    }

    /**
     * Medium. Describes the original subject of the file, such as, “ computer
     * image,” “ drawing,” “ lithograph,” and so forth.
     *
     * @param text
     */
    public void setMedium(String text)
    {
        info.put(IMED, text);
    }

    /**
     * Medium. Describes the original subject of the file, such as, “ computer
     * image,” “ drawing,” “ lithograph,” and so forth.
     *
     * @return
     */
    public String getMedium()
    {
        return info.get(IMED);
    }

    /**
     * Name. Stores the title of the subject of the file, such as, “ Seattle
     * From Above.”
     *
     * @param text
     */
    public void setName(String text)
    {
        info.put(INAM, text);
    }

    /**
     * Name. Stores the title of the subject of the file, such as, “ Seattle
     * From Above.”
     *
     * @return
     */
    public String getName()
    {
        return info.get(INAM);
    }

    /**
     * Palette Setting. Specifies the number of colors requested when digitizing
     * an image, such as “ 256.”
     *
     * @param text
     */
    public void setPalette(String text)
    {
        info.put(IPLT, text);
    }

    /**
     * Palette Setting. Specifies the number of colors requested when digitizing
     * an image, such as “ 256.”
     *
     * @return
     */
    public String getPalette()
    {
        return info.get(IPLT);
    }

    /**
     * Product. Specifies the name of the title the file was originally intended
     * for, such as “Encyclopedia of Pacific Northwest Geography.” ISBJ Subject.
     * Describes the conbittents of the file, such as “Aerial view of Seattle.”
     *
     * @param text
     */
    public void setProduct(String text)
    {
        info.put(IPRD, text);
    }

    /**
     * Product. Specifies the name of the title the file was originally intended
     * for, such as “Encyclopedia of Pacific Northwest Geography.” ISBJ Subject.
     * Describes the conbittents of the file, such as “Aerial view of Seattle.”
     *
     * @return
     */
    public String getProduct()
    {
        return info.get(IPRD);
    }

    /**
     * Software. Identifies the name of the software package used to create the
     * file, such as “Microsoft WaveEdit.”
     *
     * @param text
     */
    public void setSoftware(String text)
    {
        info.put(ISFT, text);
    }

    /**
     * Software. Identifies the name of the software package used to create the
     * file, such as “Microsoft WaveEdit.”
     *
     * @return
     */
    public String getSoftware()
    {
        return info.get(ISFT);
    }

    /**
     * Sharpness. Identifies the changes in sharpness for the digitizer required
     * to produce the file (the format depends on the hardware used).
     *
     * @param text
     */
    public void setSharpness(String text)
    {
        info.put(ISHP, text);
    }

    /**
     * Sharpness. Identifies the changes in sharpness for the digitizer required
     * to produce the file (the format depends on the hardware used).
     *
     * @return
     */
    public String getSharpness()
    {
        return info.get(ISHP);
    }

    /**
     * Source. Identifies the name of the person or organization who supplied
     * the original subject of the file. For example, “ Trey Research.”
     *
     * @param text
     */
    public void setSource(String text)
    {
        info.put(ISRC, text);
    }

    /**
     * Source. Identifies the name of the person or organization who supplied
     * the original subject of the file. For example, “ Trey Research.”
     *
     * @return
     */
    public String getSource()
    {
        return info.get(ISRC);
    }

    /**
     * Source Form. Identifies the original form of the material that was
     * digitized, such as “ slide,” “ paper,” “map,” and so forth. This is not
     * necessarily the same as IMED.
     *
     * @param text
     */
    public void setSourceForm(String text)
    {
        info.put(ISRF, text);
    }

    /**
     * Source Form. Identifies the original form of the material that was
     * digitized, such as “ slide,” “ paper,” “map,” and so forth. This is not
     * necessarily the same as IMED.
     *
     * @return
     */
    public String getSourceForm()
    {
        return info.get(ISRF);
    }

    /**
     * Technician. Identifies the technician who digitized the subject file. For
     * example, “ Smith, John.}
     *
     * @param text
     */
    public void setTechnician(String text)
    {
        info.put(ITCH, text);
    }

    /**
     * Technician. Identifies the technician who digitized the subject file. For
     * example, “ Smith, John.}
     *
     * @return
     */
    public String getTechnician()
    {
        return info.get(ITCH);
    }
}
