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
package org.vesalainen.pm.deb;

import static org.vesalainen.pm.deb.FieldStatus.*;

/**
 *
 * @author tkv
 */
public enum Field
{
    Package(Mandatory),
    Architecture(Mandatory),
    Section(Recommended),
    Priority(Recommended),
    Essential,
    Description(Mandatory),
    Built_Using,
    Package_Type,
    Source(Mandatory),
    Maintainer(Mandatory),
    Uploaders,
    Depends,
    Conflicts,
    Provides,
    Standards_Version(Recommended),
    Homepage,
    Format_Specification,
    Name,
    Copyright,
    License,
    Files;
    private FieldStatus status = Optional;

    private Field()
    {
    }

    private Field(FieldStatus status)
    {
        this.status = status;
    }

}
