/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ui;

import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeScale extends AbstractScale
{

    public TimeScale()
    {
        addLevel(DAYS.toSeconds(1), "%.0fd");
        addLevel(HOURS.toSeconds(1), "%.0fh");
        addLevel(MINUTES.toSeconds(1), "%.0fm");
        addTail(1, "s", 1.0, 5.0);
    }
    
}
