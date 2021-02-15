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
package org.vesalainen.navi;

import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Tank
{

    private final LimitedCorner[] corners;
    private final double volumeFull;
    
    public Tank(LimitedCorner... corners)
    {
        this.corners = corners;
        double v = 0;
        for (LimitedCorner corner : corners)
        {
            v += corner.volumeFull();
        }
        this.volumeFull = v;
    }
    
    public double volume(double z)
    {
        double v = 0;
        for (LimitedCorner corner : corners)
        {
            v += corner.volume(z);
        }
        return v;
    }
 
    public double volumeFull()
    {
        return volumeFull;
    }
 
    public static class LimitedCorner
    {
        private double width;
        private double length;
        private double level1;
        private double level2;
        private double level3;
        private double volume3;
        private double volumeFull;

        private Corner corner;
        
        public LimitedCorner(double width, double length, double... depth)
        {
            if (depth.length != 4)
            {
                throw new IllegalArgumentException("should have 4 depths");
            }
            this.width = width;
            this.length = length;
            Arrays.sort(depth);
            this.level3 = depth[3]-depth[0];
            this.level2 = depth[3]-depth[1];
            this.level1 = depth[3]-depth[2];
            this.corner = new Corner(width, length, depth);
            this.volume3 = volume0(level3);
            this.volumeFull = volume(depth[3]);
        }
        private double volume(double z)
        {
            if (z <= level3)
            {
                return volume0(z);
            }
            else
            {
                return width*length*(z-level3)+volume3;
            }
        }

        public double volumeFull()
        {
            return volumeFull;
        }
        
        private double volume0(double z)
        {
            assert z <= level3;
            double v = corner.volume(z);
            if (z > level1)
            {
                v -= corner.volume(z-level1);
            }
            if (z > level2)
            {
                v -= corner.volume(z-level2);
            }
            return v;
        }
        
    }
    private static class Corner
    {
        private double a;
        private double b;
        private double coef;

        private Corner(double width, double length, double... depth)
        {
            if (depth.length != 4)
            {
                throw new IllegalArgumentException("should have 4 depths");
            }
            Arrays.sort(depth);
            double l2 = depth[3]-depth[1];
            double l1 = depth[3]-depth[2];
            this.a = l1/length;
            this.b = l2/width;
            coef = 1.0/(6*a*b);
        }
        
        private double volume(double z)
        {
            return Math.pow(z, 3)*coef;
        }
    }
}
