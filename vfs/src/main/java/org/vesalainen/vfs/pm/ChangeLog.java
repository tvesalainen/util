/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs.pm;

import java.nio.file.attribute.FileTime;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface ChangeLog extends Comparable<ChangeLog>
{
    /**
     * Returns time of change log
     * @return 
     */
    FileTime getTime();
    /**
     * Sets time of change log
     * @param time 
     */
    void setTime(FileTime time);
    /**
     * Returns maintainer
     * @return 
     */
    String getMaintainer();
    /**
     * Sets maintainer
     * @param maintainer 
     */
    void setMaintainer(String maintainer);
    /**
     * Returns log detail lines
     * @return 
     */
    String getText();
    /**
     * Sets log detail lines.
     * @param text 
     */
    void setText(String text);
    /**
     * Compares this log to given. Default implementation sorts logs in reverse 
     * order using time.
     * @param o
     * @return 
     */
    @Override
    public default int compareTo(ChangeLog o)
    {
        return -getTime().compareTo(o.getTime());
    }
    
}
