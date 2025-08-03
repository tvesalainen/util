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
package org.vesalainen.ui;

import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.Locale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class SVGHelp
{
    public static String toPath(String font, int size, int margin, double x, double y, String text, TextAlignment... alignments)
    {
        return toPath(new Font(font, 0, size), margin, x, y, text, alignments);
    }
    public static String toPath(Font font, int margin, double x, double y, String text, TextAlignment... alignments)
    {
        return toPath(AbstractPlotter.text2Shape(font, margin, x, y, text, alignments));
    }
    public static String toPath(Shape shape)
    {
        return toPath(shape.getPathIterator(null));
    }
    public static String toPath(PathIterator pi)
    {
        StringBuilder sb = new StringBuilder();
        double[] arr = new double[6];
        while (!pi.isDone())
        {
            switch (pi.currentSegment(arr))
            {
                case PathIterator.SEG_MOVETO:
                    sb.append(String.format(Locale.US, "M%.1f %.1f", arr[0], arr[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    sb.append(String.format(Locale.US, "L%.1f %.1f", arr[0], arr[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    sb.append(String.format(Locale.US, "Q%.1f %.1f %.1f %.1f", arr[0], arr[1], arr[2], arr[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    sb.append(String.format(Locale.US, "C%.1f %.1f %.1f %.1f %.1f %.1f", arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    sb.append("Z");
                    break;
            }
            pi.next();
            if (!pi.isDone())
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
