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

import java.util.EnumMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WaveFile extends RIFFFile
{
    private Fmt fmt;
    private Map<Info,String> info = new EnumMap<>(Info.class);
    
    public WaveFile(Map<String, Chunk> chunkMap)
    {
        super(chunkMap);
        this.fmt = Fmt.getInstance(chunkMap);
        if (chunkMap.containsKey("LIST"))
        {
            Chunk list = chunkMap.get("LIST");
            ContainerChunk clist = new ContainerChunk(list);
            Map<String, Chunk> map = clist.readAll();
            for (Chunk chunk : map.values())
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
    }

    public AudioFormat getAudioFormat()
    {
        return fmt.getAudioFormat();
    }
}
