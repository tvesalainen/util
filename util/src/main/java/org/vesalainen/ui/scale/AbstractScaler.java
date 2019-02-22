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
package org.vesalainen.ui.scale;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;
import org.vesalainen.ui.DoubleTransform;
import org.vesalainen.ui.ScalerOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractScaler
{
    private static final FontRenderContext DEFAULT_FONTRENDERCONTEXT = new FontRenderContext(null, false, true);
    protected double min;
    protected double max;

    public AbstractScaler()
    {
    }

    public AbstractScaler(double min, double max)
    {
        set(min, max);
    }
    public void forEach(double level, ScalerOperator op)
    {
        forEach(Locale.getDefault(), level, op);
    }
    public void forEach(Locale locale, double level, ScalerOperator op)
    {
        Iterator<String> li = getLabels(locale, level).iterator();
        OfDouble vi = iterator(level);
        while (li.hasNext() && vi.hasNext())
        {
            op.apply(vi.nextDouble(), li.next());
        }
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
    public double getLevelFor(Font font, boolean horizontal)
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
    public double getLevelFor(Font font, boolean horizontal, double xy)
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
    public double getLevelFor(Font font, FontRenderContext frc, boolean horizontal)
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
    public double getLevelFor(Font font, FontRenderContext frc, boolean horizontal, double xy)
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
    public double getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal)
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
    public double getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal, double xy)
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
    public double getLevelFor(Font font, FontRenderContext frc, DoubleTransform transformer, boolean horizontal, double xy, Rectangle2D bounds)
    {
        return getXYLevel(font, frc, horizontal ? transformer : DoubleTransform.swap().andThen(transformer), xy, bounds);
    }
    private double getXYLevel(Font font, FontRenderContext frc, DoubleTransform transformer, double xy, Rectangle2D bounds)
    {
        double level = 0;
        while (true)
        {
            OfDouble vi = iterator(level);
            double value = vi.nextDouble();
            Iterator<String> li = getLabels(level).iterator();
            String label = li.next();
            Rectangle2D first = font.getStringBounds(label, frc);
            if (bounds != null)
            {
                bounds.setRect(first);
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
                    bounds.add(cur);
                }
                transformer.transform(value, xy, (x,y)->cur.setRect(x, y, cur.getWidth(), cur.getHeight()));
                if (cur.intersects(prev))
                {
                    return level-1;
                }
                prev = cur;
            }
            level+=0.5;
        }
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
    public List<String> getLabels(double level)
    {
        return getLabels(Locale.getDefault(), level);
    }

    /**
     * Returns labels for level
     * @param locale
     * @param level
     * @return
     */
    public abstract List<String> getLabels(Locale locale, double level);

    /**
     * Return format string for formatting 0-level labels
     * @param level
     * @return
     * @see java.lang.String#format(java.lang.String, java.lang.Object...)
     */
    public abstract String getFormat(double level);


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
    public double level()
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
    public abstract double level(int minMarkers, int maxMarkers);

    /**
     * Returns number of markers for given level.
     * @param level
     * @return
     */
    public abstract double count(double level);

    /**
     * Returns stream for markers between min and max. 0-level returns less
     * @param level
     * @return 
     */
    public DoubleStream stream(double level)
    {
        return StreamSupport.doubleStream(spliterator(level), false);
    }
    

    /**
     * Returns Spliterator for markers between min and max. 0-level returns less
     * than 10.
     * @param level >= 0
     * @return
     */
    public Spliterator.OfDouble spliterator(double level)
    {
        return Spliterators.spliteratorUnknownSize(iterator(level), 0);
    }
    /**
     * Returns Iterator for markers between min and max. 0-level returns less
     * @param level
     * @return 
     */
    public abstract PrimitiveIterator.OfDouble iterator(double level);

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
