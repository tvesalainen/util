/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.math;

import java.io.IOException;

/**
 *
 * @author tkv
 */
public interface Conditional
{
    /**
     * Push(pop() == pop())
     * @throws IOException 
     */
    void eq() throws IOException;
    /**
     * Push(pop() != pop())
     * @throws IOException 
     */
    void ne() throws IOException;
    /**
     * Push(pop()>pop())
     * @throws IOException 
     */
    void lt() throws IOException;
    /**
     * Push(pop()>=pop())
     * @throws IOException 
     */
    void le() throws IOException;
    /**
     * Push(pop()<pop())
     * @throws IOException 
     */
    void gt() throws IOException;
    /**
     * Push(pop()<=pop())
     * @throws IOException 
     */
    void ge() throws IOException;
    /**
     * Push(!pop())
     * @throws IOException 
     */
    void not() throws IOException;
    /**
     * Push(pop() && pop())
     * @throws IOException 
     */
    void and() throws IOException;
    /**
     * Push(pop() || pop())
     * @throws IOException 
     */
    void or() throws IOException;
}
