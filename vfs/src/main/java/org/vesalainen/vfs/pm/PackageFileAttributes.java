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

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import org.vesalainen.nio.file.attribute.UserAttrs;
import org.vesalainen.util.EnumSetFlagger;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PackageFileAttributes
{
    public static final String USAGE = "user:usage";
    public static final String LANGUAGE = "user:language";
    public static final String COPYRIGHT = "user:copyright";
    public static final String LICENSE = "user:license";
    
    public static final void setUsage(Path path, FileUse... use) throws IOException
    {
        
    }
    public static final void setUsage(Path path, EnumSet<FileUse> use) throws IOException
    {
        UserAttrs.setIntAttribute(path, USAGE, EnumSetFlagger.getFlag(use));
    }
    public static final EnumSet<FileUse> getUsage(Path path) throws IOException
    {
        int at = UserAttrs.getIntAttribute(path, USAGE, 0);
        return EnumSetFlagger.getSet(FileUse.class, at);
    }
    public static final void setLanguage(Path path, String language) throws IOException
    {
        UserAttrs.setStringAttribute(path, LANGUAGE, language);
    }
    public static final void setCopyright(Path path, String copyright) throws IOException
    {
        UserAttrs.setStringAttribute(path, COPYRIGHT, copyright);
    }
    public static final void setLicense(Path path, String license) throws IOException
    {
        UserAttrs.setStringAttribute(path, LICENSE, license);
    }
    public static final String getLanguage(Path path) throws IOException
    {
        return UserAttrs.getStringAttribute(path, LANGUAGE);
    }
    public static final String getCopyright(Path path) throws IOException
    {
        return UserAttrs.getStringAttribute(path, COPYRIGHT);
    }
    public static final String getLicense(Path path) throws IOException
    {
        return UserAttrs.getStringAttribute(path, LICENSE);
    }
}
