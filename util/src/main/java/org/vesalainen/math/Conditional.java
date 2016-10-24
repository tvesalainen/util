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

import org.vesalainen.util.NoNeedToContinueException;


/**
 *
 * @author tkv
 */
public interface Conditional
{
    /**
     * Push(pop() == pop())
     * @throws Exception 
     */
    void eq() throws Exception;
    /**
     * Push(pop() != pop())
     * @throws Exception 
     */
    void ne() throws Exception;
    /**
     * Push(pop()>pop())
     * @throws Exception 
     */
    void lt() throws Exception;
    /**
     * Push(pop()>=pop())
     * @throws Exception 
     */
    void le() throws Exception;
    /**
     * Push(pop()<pop())
     * @throws Exception 
     */
    void gt() throws Exception;
    /**
     * Push(pop()<=pop())
     * @throws Exception 
     */
    void ge() throws Exception;
    /**
     * Push(!pop())
     * @throws Exception 
     */
    void not() throws Exception;
    /**
     * Push(pop() && pop())
     * @throws Exception 
     */
    void and() throws Exception;
    /**
     * Push(pop() || pop())
     * @throws Exception 
     */
    void or() throws Exception;
    /**
     * Checks if there is FALSE at the bottom of stack in and statement. If
     * there is throws NoNeedToContinueException.
     * @throws NoNeedToContinueException 
     */
    void checkAnd() throws NoNeedToContinueException;
    /**
     * Checks if there is TRUE at the bottom of stack in or statement. If
     * there is throws NoNeedToContinueException.
     * @throws NoNeedToContinueException 
     */
    void checkOr() throws NoNeedToContinueException;
}
