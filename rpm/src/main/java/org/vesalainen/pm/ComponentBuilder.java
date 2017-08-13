/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm;

import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import org.vesalainen.pm.rpm.FileFlag;

/**
 *
 * @author tkv
 */
public interface ComponentBuilder
{

    ComponentBuilder setFlag(FileFlag... flags);

    ComponentBuilder setGroupname(String groupname);

    ComponentBuilder setLang(String lang);

    /**
     * Set mode in rwxrwxrwx string. rwxr--r-- = 0744
     * @param mode
     * @return
     */
    ComponentBuilder setMode(String mode);
    /**
     * Add file attribute
     * @param attrs
     * @return 
     */
    ComponentBuilder addFileAttributes(FileAttribute<?>... attrs);

    /**
     * Seet file time.
     * @param time
     * @return
     */
    ComponentBuilder setTime(Instant time);

    /**
     * Set file time as seconds from epoch.
     * @param time
     * @return
     */
    ComponentBuilder setTime(int time);

    ComponentBuilder setUsername(String username);
    
}
