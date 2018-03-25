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
import org.vesalainen.nio.channels.BufferedFileBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class InfoChunk extends Chunk
{
    private Info info;
    private String text;

    public InfoChunk(Info info, String text)
    {
        super(info.name());
        this.info = info;
        this.text = text;
    }
    
    
    public InfoChunk(Chunk other)
    {
        super(other);
        this.info = Info.valueOf(other.id);
        this.text = getSZ(0, size);
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        bb.put(text, ISO_8859_1);
        bb.put((byte)0);
    }

    public Info getInfo()
    {
        return info;
    }

    public String getText()
    {
        return text;
    }
    
}
