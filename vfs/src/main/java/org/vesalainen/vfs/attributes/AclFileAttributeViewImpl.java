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
package org.vesalainen.vfs.attributes;

import java.io.IOException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.List;
import static org.vesalainen.vfs.attributes.FileAttributeName.ACL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AclFileAttributeViewImpl extends FileOwnerAttributeViewImpl implements AclFileAttributeView
{

    public AclFileAttributeViewImpl(FileAttributeAccess access)
    {
        super("acl", access);
    }

    @Override
    public List<AclEntry> getAcl() throws IOException
    {
        return (List<AclEntry>) get(ACL);
    }

    @Override
    public void setAcl(List<AclEntry> acl) throws IOException
    {
        put(ACL, acl);
    }
    
}
