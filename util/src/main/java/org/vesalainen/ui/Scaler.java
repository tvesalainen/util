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

import org.vesalainen.ui.scale.ScalerOperator;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.vesalainen.ui.scale.MergeScale;
import org.vesalainen.ui.scale.Scale;
import org.vesalainen.ui.scale.ScaleLevel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Scaler
{
    private static final FontRenderContext DEFAULT_FONTRENDERCONTEXT = new FontRenderContext(null, false, true);
    protected double min;
    protected double max;
    protected Scale scale;

    public Scaler()
    {
        this(0, 0, MergeScale.BASIC15);
    }

    public Scaler(Scale scale)
    {
        this(0, 0, scale);
    }

    public Scaler(double min, double max)
    {
        this(min, max, MergeScale.BASIC15);
    }
    public Scaler(double min, double max, Scale scale)
    {
        set(min, max);
        this.scale = scale;
    }
    public Iterator<ScaleLevel> iterator()
    {
        return scale.iterator(min, max);
    }
    public void forEach(ScaleLevel level, ScalerOperator op)
    {
        forEach(Locale.getDefault(), level, op);
    }
    public void forEach(Locale locale, ScaleLevel level, ScalerOperator op)
    {
        level.forEach(min, max, locale, op);
    }
    public final void set(double min, double max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("min > max");
        }
        this.min = min;
        this.max = max;
    }
    /**
     * Returns highest level where drawn labels don't overlap using identity
     * transformer and FontRenderContext with identity AffineTransform, no
     * anti-aliasing and fractional metrics
     * @param font
     * @param horizontal
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, boolean horizontal)
    {
        return getLevelFor(font, horizontal, 0);
    }
    /**
     * Returns highest level where drawn labels don't overlap using identity
     * transformer and FontRenderContext with identity AffineTransform, no
     * anti-aliasing and fractional metrics
     * @param font
     * @param horizontal
     * @param xy Lines constant value
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, boolean horizontal, double xy)
    {
        return getLevelFor(font, DEFAULT_FONTRENDERCONTEXT, horizontal, xy);
    }
    /**
     * Returns highest level where drawn labels don't overlap using identity
     * transformer
     * @param font
     * @param frc
     * @param horizontal
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, FontRenderContext frc, boolean horizontal)
    {
        return getLevelFor(font, frc, horizontal, 0);
    }
    /**
     * Returns highest level where drawn labels don't overlap using identity
     * transformer
     * @param font
     * @param frc
     * @param horizontal
     * @param xy Lines constant value
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, FontRenderContext frc, boolean horizontal, double xy)
    {
        return getLevelFor(font, frc, DoubleTransform.identity(), horizontal, xy);
    }
    /**
     * Returns highest level where drawn labels don't overlap
     * @param font
     * @param frc 
     * @param transformer
     * @param horizontal
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal)
    {
        return getLevelFor(font, frc, transformer, horizontal, 0);
    }
    /**
     * Returns highest level where drawn labels don't overlap
     * @param font
     * @param frc 
     * @param transformer
     * @param horizontal
     * @param xy Lines constant value
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal, double xy)
    {
        return getLevelFor(font, frc, transformer, horizontal, xy, null);
    }
    /**
     * Returns highest level where drawn labels don't overlap
     * @param font
     * @param frc
     * @param transformer
     * @param horizontal
     * @param xy Lines x/y constant value
     * @param bounds Accumulates label bounds here if not null.
     * @return 
     */
    public ScaleLevel getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal, double xy, Rectangle2D bounds)
    {
        return getXYLevel(font, frc, horizontal ? transformer : DoubleTransform.swap().andThen(transformer), xy, bounds);
    }
    private ScaleLevel getXYLevel(Font font, FontRenderContext frc, DoubleTransform transformer, double xy, Rectangle2D bounds)
    {
        Rectangle2D bnds = null;
        if (bounds != null)
        {
            bnds = new Rectangle2D.Double();
        }
        Iterator<ScaleLevel> si = scale.iterator(min, max);
        ScaleLevel stack = null;
        while (si.hasNext())
        {
            ScaleLevel level = si.next();
            OfDouble vi = iterator(level);
            double value = vi.nextDouble();
            Iterator<String> li = getLabels(level).iterator();
            String label = li.next();
            Rectangle2D first = font.getStringBounds(label, frc);
            if (bounds != null)
            {
                bnds.setRect(first);
            }
            Rectangle2D prev = first;
            transformer.transform(value, xy, (x,y)->first.setRect(x, y, first.getWidth(), first.getHeight()));
            while (vi.hasNext())
            {
                value = vi.nextDouble();
                label = li.next();
                Rectangle2D cur = font.getStringBounds(label, frc);
                if (bounds != null)
                {
                    bnds.add(cur);
                }
                transformer.transform(value, xy, (x,y)->cur.setRect(x, y, cur.getWidth(), cur.getHeight()));
                if (cur.intersects(prev))
                {
                    if (stack == null)
                    {
                        throw new IllegalArgumentException("Font "+font+" is too big for coordinate");
                    }
                    return stack;
                }
                prev = cur;
            }
            if (bounds != null)
            {
                bounds.setRect(bnds);
            }
            stack = level;
        }
        assert false;   // can't come here
        return null;
    }

    
    /**
     * Returns labels for default level using default locale.
     * @return
     */
    public List<String> getLabels()
    {
        return getLabels(level());
    }

    /**
     * Returns labels for level using default locale
     * @param level
     * @return
     */
    public List<String> getLabels(ScaleLevel level)
    {
        return getLabels(Locale.getDefault(), level);
    }

    /**
     * Returns labels for level
     * @param locale
     * @param level
     * @return
     */
    public List<String> getLabels(Locale locale, ScaleLevel level)
    {
        return level.stream(min, max).mapToObj((d)->level.label(locale, d)).collect(Collectors.toList());
    }

    /**
     * Returns stream for markers between min and max. Step is selected so that
     * number of markers is greater than 5.
     * @return
     */
    public DoubleStream stream()
    {
        return stream(level());
    }

    /**
     * Returns minimum level where number of markers is not less than 5 and less
     * than 15.
     * @return
     */
    public ScaleLevel level()
    {
        return level(5, 15);
    }

    /**
     * Returns minimum level where number of markers is not less that minMarkers
     * and less than maxMarkers. If both cannot be met, maxMarkers is stronger.
     * @param minMarkers
     * @param maxMarkers
     * @return
     */
    public ScaleLevel level(int minMarkers, int maxMarkers)
    {
        Iterator<ScaleLevel> iterator = scale.iterator(min, max);
        ScaleLevel level = iterator.next();
        ScaleLevel prev = null;
        while (iterator.hasNext() && minMarkers > level.count(min, max))
        {
            prev = level;
            level = iterator.next();
        }
        if (maxMarkers < level.count(min, max))
        {
            return prev;
        }
        else
        {
            return level;
        }
    }

    /**
     * Returns number of markers for given level.
     * @param level
     * @return
     */
    public double count(ScaleLevel level)
    {
        return level.count(min, max);
    }

    /**
     * Returns stream for markers between min and max. 0-level returns less
     * @param level
     * @return 
     */
    public DoubleStream stream(ScaleLevel level)
    {
        return level.stream(min, max);
    }
    

    /**
     * Returns Spliterator for markers between min and max. 0-level returns less
     * than 10.
     * @param level >= 0
     * @return
     */
    public Spliterator.OfDouble spliterator(ScaleLevel level)
    {
        return level.spliterator(min, max);
    }
    /**
     * Returns Iterator for markers between min and max. 0-level returns less
     * @param level
     * @return 
     */
    public PrimitiveIterator.OfDouble iterator(ScaleLevel level)
    {
        return level.iterator(min, max);
    }

    public double getMin()
    {
        return min;
    }

    public void setMin(double min)
    {
        this.min = min;
    }

    public void setMax(double max)
    {
        this.max = max;
    }
}
