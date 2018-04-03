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

package org.vesalainen.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * Utility for creating scales. For example if samples are between 0 - 30 the 
 * 0-level scale is 0, 10, 20, 30. 0.5 level scale is 0, 5, 10, 15,... 
 * 1-level 0, 1, 2, 3, .... 
 * @author Timo Vesalainen
 */
public class Scaler
{
    private double min;
    private double max;
    private boolean updated;
    private int exp;

    public Scaler(double min, double max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("min > max");
        }
        this.min = min;
        this.max = max;
        updated = true;
    }

    void calc()
    {
        if (updated)
        {
            if (max < min)
            {
                throw new IllegalArgumentException("max="+max+" < min="+min);
            }
            exp = 0;
            double delta = max-min;
            while (delta >= 10.0 || delta < 1.0)
            {
                if (delta >= 10.0)
                {
                    delta /= 10;
                    exp++;
                }
                else
                {
                    delta *= 10;
                    exp--;
                }
            }
            updated = false;
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
     * @return 
     */
    public List<String> getLabels(double level)
    {
        return getLabels(Locale.getDefault(), level);
    }
    /**
     * Returns labels for 0-level
     * @param locale
     * @return 
     */
    public List<String> getLabels(Locale locale, double level)
    {
        List<String> labels = new ArrayList<>();
        Spliterator.OfDouble i0 = spliterator(level);
        String format = getFormat(level);
        i0.forEachRemaining((double d) -> labels.add(String.format(locale, format, d)));
        return labels;
    }
    /**
     * Return format string for formatting 0-level labels
     * @return 
     * @see java.lang.String#format(java.lang.String, java.lang.Object...) 
     */
    public String getFormat(double level)
    {
        double signum = exp >= 0 ? 1 : -1;
        int e = (int) (exp-signum*Math.ceil(level));
        return String.format("%%.%df", e < 0 ? -e : 0);
    }
    /**
     * Returns distance between two markers using default level.
     * @return 
     */
    public double step()
    {
        return step(level());
    }
    /**
     * Returns distance between two markers.
     * @param level
     * @return 
     */
    public double step(double level)
    {
        calc();
        int l = (int) Math.floor(level);
        double step = Math.pow(10, exp-l);
        double rem = level - (double)l;
        if (rem > 0)
        {
            step *= rem;
        }
        return step;
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
    public double level(int minMarkers, int maxMarkers)
    {
        double level = 0;
        double count = count(level);
        while (count < minMarkers)
        {
            level += 0.5;
            count = count(level);
        }
        if (count > maxMarkers)
        {
            level -= 0.5;
        }
        return level;
    }
    /**
     * Returns number of markers for given level.
     * @param level
     * @return 
     */
    public double count(double level)
    {
        calc();
        int l = (int) Math.floor(level);
        double rem = level - (double)l;
        double stepMultiplier = 1.0;
        if (rem > 0)
        {
            stepMultiplier = rem;
        }
        double signum = exp >= 0 ? 1 : -1;
        double el = exp-signum*l;
        double step = Math.pow(10, el);
        double np = Math.pow(10, -el);
        double begin = Math.ceil(min*np)*step;
        double end = Math.floor(max*np)*step;
        step *= stepMultiplier;
        if (begin-step >= min)
        {
            begin -= step;
        }
        if (end+step <=max)
        {
            end += step;
        }
        return (end-begin)/step+1;
    }
    /**
     * Returns stream for markers between min and max. 0-level returns less
     * @param level
     * @return 
     */
    public DoubleStream stream(double level)
    {
        return StreamSupport.doubleStream(Scaler.this.spliterator(level), false);
    }
    /**
     * Returns Spliterator for markers between min and max. 0-level returns less
     * than 10.
     * @param level >= 0 
     * @return 
     */
    public Spliterator.OfDouble spliterator(double level)
    {
        calc();
        int l = (int) Math.floor(level);
        double rem = level - (double)l;
        double stepMultiplier = 1.0;
        if (rem > 0)
        {
            stepMultiplier = rem;
        }
        double signum = exp >= 0 ? 1 : -1;
        double el = exp-signum*l;
        double step = Math.pow(10, el);
        double np = Math.pow(10, -el);
        double begin = Math.ceil(min*np)*step;
        double end = Math.floor(max*np)*step;
        if (stepMultiplier != 1.0)
        {
            step *= stepMultiplier;
            if (begin-step >= min)
            {
                begin -= step;
            }
            if (end+step <=max)
            {
                end += step;
            }
            return new Iter(begin, end, step);
        }
        else
        {
            return new Iter(begin, end, step);
        }
    }
    
    public double getMin()
    {
        return min;
    }

    public void setMin(double min)
    {
        this.min = min;
        updated = true;
    }

    public double getMax()
    {
        return max;
    }

    public void setMax(double max)
    {
        this.max = max;
        updated = true;
    }

    private class Iter implements Spliterator.OfDouble
    {
        private final double end;
        private final double step;
        private double next;

        public Iter(double begin, double end, double step)
        {
            this.end = end;
            this.step = step;
            this.next = begin;
        }
        
        @Override
        public void forEachRemaining(DoubleConsumer action)
        {
            while (next <= end)
            {
                action.accept(next);
                next += step;
            }
        }

        @Override
        public OfDouble trySplit()
        {
            return null;
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action)
        {
            if (next <= end)
            {
                action.accept(next);
                next += step;
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Double> action)
        {
            if (next <= end)
            {
                action.accept(next);
                next += step;
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public Comparator<? super Double> getComparator()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return (long) ((end-next)/step);
        }

        @Override
        public int characteristics()
        {
            return ORDERED|SORTED|SIZED;
        }
        
    }
}
