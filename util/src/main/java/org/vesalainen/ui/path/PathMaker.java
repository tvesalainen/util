/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ui.path;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface PathMaker<C>
{
    void beginPath();
    void moveTo(double x, double y);
    void lineTo(double x, double y);
    void quadTo(double x1, double y1, double x, double y);
    void cubicTo(double x1, double y1, double x2, double y2, double x, double y);
    void closePath();
    void fillColor(C color);
    C getColor(String color);
    default void fillColor(String color)
    {
        fillColor(getColor(color));
    }
}
