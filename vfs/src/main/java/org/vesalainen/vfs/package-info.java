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
package org.vesalainen.vfs;
/**
 * Virtual File System provides memory based customizable file system utility.
 * Usage example:
 * <code>
        FileSystem fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.vfs:///", null, null));
        Files.createDirectories(fileSystem.getPath("/etc/default/java"));
* </code>
* Features currently not supported: file mapping/locking
 */