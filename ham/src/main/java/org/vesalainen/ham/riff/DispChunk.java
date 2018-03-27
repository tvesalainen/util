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
public class DispChunk extends Chunk
{
    private int clipboardFormat;
    private String text;
    public DispChunk(String text)
    {
        super("DISP");
        this.text = text;
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        bb.putInt(clipboardFormat);
        bb.put(id, ISO_8859_1);
    }
    
}
