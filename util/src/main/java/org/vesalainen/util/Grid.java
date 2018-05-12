/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Grid<T>
{
    int width();
    int heigth();
    /**
     * Return color at x,y
     * @param x
     * @param y
     * @return 
     */
    T getColor(int x, int y);
    /**
     * Returns true if color at x,y is given. Otherwise returns false, also if
     * point is outside grid.
     * @param x
     * @param y
     * @param color
     * @return 
     */
    boolean hit(int x, int y, T color);
    /**
     * Sets color at x,y
     * @param x
     * @param y
     * @param color 
     */
    void setColor(int x, int y, T color);
    
}
