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
package org.vesalainen.ham;

import java.io.IOException;
import java.nio.file.Path;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.vesalainen.ham.fft.FilterAudioInputStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AudioFileFilter
{
    public static final void filter(Path in, Path out) throws IOException, UnsupportedAudioFileException
    {
        try (   AudioInputStream ais = AudioSystem.getAudioInputStream(in.toFile());
                FilterAudioInputStream fais = new FilterAudioInputStream(ais, 512, 1000, 3000)
                )
        {
            AudioSystem.write(fais, AudioFileFormat.Type.WAVE, out.toFile());
        }
    }
}
