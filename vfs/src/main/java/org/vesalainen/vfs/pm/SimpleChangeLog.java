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
public class SimpleChangeLog implements ChangeLog
{
    protected String maintainer;
    protected FileTime time;
    protected String text;

    protected SimpleChangeLog()
    {
    }

    public SimpleChangeLog(String maintainer, FileTime time, String text)
    {
        this.maintainer = maintainer;
        this.time = time;
        this.text = text;
    }

    @Override
    public String getMaintainer()
    {
        return maintainer;
    }

    @Override
    public void setMaintainer(String maintainer)
    {
        this.maintainer = maintainer;
    }

    @Override
    public FileTime getTime()
    {
        return time;
    }

    @Override
    public void setTime(FileTime time)
    {
        this.time = time;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public void setText(String text)
    {
        this.text = text;
    }
    
}
