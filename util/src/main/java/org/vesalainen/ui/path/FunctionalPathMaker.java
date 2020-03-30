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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionalPathMaker<C> implements PathMaker<C>
{
    private BeginPath begin;
    private MoveTo move;
    private LineTo line;
    private QuadTo quad;
    private CubicTo cubic;
    private ClosePath close;
    private Consumer<C> lineColor;
    private Consumer<C> fillColor;
    private Function<String,C> colorSupplier;

    public FunctionalPathMaker(PathMaker<C> oth)
    {
        this(oth::beginPath, oth::moveTo, oth::lineTo, oth::quadTo, oth::cubicTo, oth::closePath, oth::fillColor, oth::getColor);
    }

    public FunctionalPathMaker(BeginPath begin, MoveTo move, LineTo line, QuadTo quad, CubicTo cubic, ClosePath close, Consumer<C> fillColor, Function<String,C>  colorSupplier)
    {
        this.begin = begin;
        this.move = move;
        this.line = line;
        this.quad = quad;
        this.cubic = cubic;
        this.close = close;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void beginPath()
    {
        begin.beginPath();
    }

    @Override
    public void moveTo(double x, double y)
    {
        move.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y)
    {
        line.lineTo(x, y);
    }

    @Override
    public void quadTo(double x1, double y1, double x, double y)
    {
        quad.quadTo(x1, y1, x, y);
    }

    @Override
    public void cubicTo(double x1, double y1, double x2, double y2, double x, double y)
    {
        cubic.cubicTo(x1, y1, x2, y2, x, y);
    }

    @Override
    public void closePath()
    {
        close.closePath();
    }

    @Override
    public void fillColor(C color)
    {
        fillColor.accept(color);
    }

    @Override
    public C getColor(String color)
    {
        return colorSupplier.apply(color);
    }

}
