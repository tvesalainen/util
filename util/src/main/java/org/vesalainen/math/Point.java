/*
 * Copyright (C) 2014 Timo Vesalainen
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

/**
 * DONT TRY TO REPLACE WITH Point2D. Point2D is not interface and it's poor
 * choice to name subclass as Double will cause LOTS OF PROBLEMS!
 * @author Timo Vesalainen
 */
public interface Point
{
    /**
     * Returns the x-coordinate.
     * @return 
     */
    double getX();
    /**
     * Returns the y-coordinate.
     * @return 
     */
    double getY();
}
