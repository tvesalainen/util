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

import java.time.Duration;
import org.vesalainen.nio.IntArray;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface SampleBuffer
{
    double getSampleFrequency();
    int getMaxAmplitude();
    int getChannels();
    default void goTo(int minutes, int seconds)
    {
        goTo(Duration.ofSeconds(60*minutes+seconds));
    }
    /**
     * Go to duration from sample start
     * @param duration 
     */
    void goTo(Duration duration);
    Duration getDuration();
    int getViewLength();
    int get(int channel, int offset);
    Duration remaining();
}
