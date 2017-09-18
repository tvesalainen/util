/*
 * COPYRIGHT (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public LICENSE as published by
 * the Free Software Foundation, either version 3 of the LICENSE, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public LICENSE for more details.
 *
 * You should have received a copy of the GNU General Public LICENSE
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.vfs.pm.deb;

import org.vesalainen.util.CamelCase;
import static org.vesalainen.vfs.pm.deb.FieldStatus.*;
import static org.vesalainen.vfs.pm.deb.FieldType.*;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum Field
{
    PACKAGE(MANDATORY),
    VERSION,
    ARCHITECTURE(MANDATORY),
    SECTION(RECOMMENDED),
    PRIORITY(RECOMMENDED),
    ESSENTIAL,
    DESCRIPTION(MANDATORY, MULTILINE),
    BUILT_USING,
    PACKAGE_TYPE,
    SOURCE(MANDATORY),
    MAINTAINER(MANDATORY),
    UPLOADERS,
    DEPENDS,
    CONFLICTS,
    PROVIDES,
    STANDARDS_VERSION(RECOMMENDED),
    HOMEPAGE,
    FORMAT_SPECIFICATION,
    NAME,
    COPYRIGHT,
    LICENSE,
    FILES(MULTILINE),
    CHANGES(MULTILINE),
    BINARY(FOLDED),
    INSTALLED_SIZE,
    CLOSES,
    CHECKSUMS_SHA1(MULTILINE),
    CHECKSUMS_SHA256(MULTILINE);
    private FieldType type = SIMPLE;
    private FieldStatus status = OPTIONAL;

    private Field()
    {
    }

    private Field(FieldStatus status)
    {
        this.status = status;
    }

    private Field(FieldType type)
    {
        this.type = type;
    }
    
    private Field(FieldStatus status, FieldType type)
    {
        this.status = status;
        this.type = type;
    }

    public FieldType getType()
    {
        return type;
    }

    public FieldStatus getStatus()
    {
        return status;
    }

    public static Field get(String text)
    {
        return Field.valueOf(text.trim().replace('-', '_').toUpperCase());
    }

    @Override
    public String toString()
    {
        return CamelCase.delimited(super.toString(), "-");
    }
    
}
