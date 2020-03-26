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
package org.vesalainen.fx;

import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import org.vesalainen.ui.Transforms;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class FXTransforms
{
    /**
     * Creates translation that translates userBounds in Cartesian coordinates 
     * to screenBound in screen coordinates.
     * <p>In Cartesian y grows up while in screen y grows down
     * @param width
     * @param height
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @param keepAspectRatio
     * @param transform 
     */
    public static void createScreenTransform(
            double width,
            double height,
            double xMin,
            double yMin,
            double xMax,
            double yMax,
            boolean keepAspectRatio, 
            Affine transform)
    {
        Transforms.createScreenTransform(
                width, 
                height, 
                xMin, 
                yMin, 
                xMax, 
                yMax, 
                keepAspectRatio, (double mxx, double mxy, double tx, double myx, double myy, double ty)->
                {
                    transform.setToTransform(mxx, mxy, tx, myx, myy, ty);
                });
    }
}
