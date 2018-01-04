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
package org.vesalainen.ham.hffax;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyDetector
{
    private final AudioInputStream ais;
    private float sampleRate;
    private int frameSize;
    private boolean bigEndian;
    private int value;
    private int runLength;
    private float frequency;

    public FrequencyDetector(TargetDataLine line)
    {
        this(new AudioInputStream(line));
    }

    public FrequencyDetector(AudioInputStream ais)
    {
        this(ais, ais.getFormat());
    }
    public FrequencyDetector(AudioInputStream ais, AudioFormat format)
    {
        this(ais, format.getSampleRate(), format.getFrameSize(), format.isBigEndian());
    }
    public FrequencyDetector(AudioInputStream ais, float sampleRate, int frameSize, boolean bigEndian)
    {
        this.ais = ais;
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.bigEndian = bigEndian;
    }
    
}
