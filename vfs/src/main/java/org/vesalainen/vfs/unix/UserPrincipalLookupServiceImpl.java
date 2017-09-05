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
package org.vesalainen.vfs.unix;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UserPrincipalLookupServiceImpl extends UserPrincipalLookupService
{

    @Override
    public UserPrincipal lookupPrincipalByName(String name) throws IOException
    {
        return new UnixUser(name, 0);
    }

    @Override
    public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException
    {
        return new UnixGroup(group, 0);
    }
    
}
