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
package org.vesalainen.nio;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author tkv
 */
public class FileUtil
{
    public static DirectoryDeletor FILE_DELETOR = new DirectoryDeletor();
    /**
     * Delete directory and all files in it.
     * @param dir
     * @throws IOException 
     */
    public static void deleteDirectory(File dir) throws IOException
    {
        deleteDirectory(dir.toPath());
    }
    /**
     * Delete directory and all files in it.
     * @param dir
     * @throws IOException 
     */
    public static void deleteDirectory(Path dir) throws IOException
    {
        Files.walkFileTree(dir, FILE_DELETOR);
    }
    
    public static class DirectoryDeletor extends SimpleFileVisitor<Path>
    {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            Files.delete(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            Files.delete(file);
            return CONTINUE;
        }
        
    }
    
}
