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
 */
public class SimpleDrawContext implements DrawContext<SimpleDrawContext>
{
    protected Locale locale;
    protected Color color;
    protected Font font;
    protected BasicStroke stroke;
    protected Paint paint;
    protected IntBinaryOperator pattern;

    public SimpleDrawContext(DrawContext ctx)
    {
        this.locale = ctx.getLocale();
        this.color = ctx.getColor();
        this.font = ctx.getFont();
        this.stroke = ctx.getStroke();
        this.paint = ctx.getPaint();
        this.pattern = ctx.getPattern();
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public Color getColor()
    {
        return color;
    }

    @Override
    public Font getFont()
    {
        return font;
    }

    @Override
    public BasicStroke getStroke()
    {
        return stroke;
    }

    @Override
    public Paint getPaint()
    {
        return paint;
    }

    @Override
    public IntBinaryOperator getPattern()
    {
        return pattern;
    }

    @Override
    public SimpleDrawContext setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    @Override
    public SimpleDrawContext setColor(Color color)
    {
        this.color = color;
        return this;
    }

    @Override
    public SimpleDrawContext setFont(Font font)
    {
        this.font = font;
        return this;
    }

    @Override
    public SimpleDrawContext setStroke(BasicStroke stroke)
    {
        this.stroke = stroke;
        return this;
    }

    @Override
    public SimpleDrawContext setPaint(Paint paint)
    {
        this.paint = paint;
        return this;
    }

    @Override
    public SimpleDrawContext setPattern(IntBinaryOperator pattern)
    {
        this.pattern = pattern;
        return this;
    }
    
}
