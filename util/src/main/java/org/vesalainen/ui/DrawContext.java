/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Locale;
import java.util.function.IntBinaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public interface DrawContext<T extends DrawContext>
{
    default void copy(DrawContext o)
    {
        setColor(o.getColor());
        setFont(o.getFont());
        setLocale(o.getLocale());
        setPaint(o.getPaint());
        setPattern(o.getPattern());
        setStroke(o.getStroke());
    }
    Color getColor();

    Font getFont();

    Locale getLocale();

    Paint getPaint();

    IntBinaryOperator getPattern();

    BasicStroke getStroke();

    T setColor(Color color);

    T setFont(Font font);

    T setLocale(Locale locale);

    T setPaint(Paint paint);

    T setPattern(IntBinaryOperator pattern);

    T setStroke(BasicStroke stroke);
    
}
