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
package org.vesalainen.math;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.Plotter;
import org.vesalainen.ui.PolarTransform;
import org.vesalainen.ui.scale.MergeScale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSplineTest
{
    
    public PolarCubicSplineTest()
    {
    }

    @Test
    public void testEval() throws IOException
    {
        PolarCubicSpline pcs = new PolarCubicSpline(0, 100, 90, 150, 180, 50, 270, 150);
        ParameterizedOperator c0 = pcs.getCurve(0);
        ParameterizedOperator c360 = pcs.getCurve(360);
    }
    
}
