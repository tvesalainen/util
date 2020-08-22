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
package org.vesalainen.navi.dd1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import static java.lang.Math.*;
import java.nio.file.Path;
import java.util.function.DoubleSupplier;
import org.vesalainen.math.solver.LinearVariable;
import org.vesalainen.math.solver.Solver;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TillerAngleSolver extends DD1 implements Solver
{
    private double a2;
    private final double link;

    public TillerAngleSolver(DD1 oth)
    {
        this(oth.r1, oth.r2, oth.alpha, oth.beta, oth.gamma, oth.a, oth.o);
    }
    
    public TillerAngleSolver(double r1, double r2, double alpha, double beta, double gamma, double a, double o)
    {
        super(r1, r2, alpha, beta, gamma, a, o);
        double x1 = x1(r1, this.gamma, this.alpha);  
        double y1 = y1(r1, this.gamma, this.alpha);
        double x2 = x2(this.gamma, this.beta);
        double y2 = y2(this.gamma, this.beta);
        this.link = hypot(x1-x2, y1-y2);
    }

    public double tillerAngle(double pilotAngle)
    {
        a2 = toRadians(pilotAngle);
        double a1 = alpha*a2/beta;
        LinearVariable a1v = new LinearVariable(a1, -2*beta, 2*beta);
        double[] solve = solve(0.01, a1v);
        return toDegrees(solve[1]);
    }
    @Override
    public double sum(DoubleSupplier... variables)
    {
        double a1 = variables[0].getAsDouble();
        double x1 = x1(r1, gamma, a1);
        double y1 = y1(r1, gamma, a1);
        double x2 = x2(gamma, a2);
        double y2 = y2(gamma, a2);
        return abs(hypot(x1-x2, y1-y2)-link);
    }
    
    public void plot(Path centerPath, Path anglePath) throws IOException 
    {
        Plotter pc = new Plotter(2000, 2000);
        Plotter pa = new Plotter(2000, 2000);
        
        for (int aa=-65;aa<=65;aa+=5)
        {
            pc.setColor(Color.getHSBColor((float) ((65.0+aa)/130.0), 1, 1));
            drawAngle(pc, pa, aa);
        }
        pc.setColor(Color.LIGHT_GRAY);
        pc.drawCoordinates();
        pc.plot(centerPath);
        pa.drawCoordinates();
        pa.plot(anglePath);
    }
    private void drawAngle(Plotter pc, Plotter pa, double pilotAngle)
    {
        double tillerAngle = tillerAngle(pilotAngle);
        pa.lineTo(pilotAngle, tillerAngle);
        double a1 = toRadians(tillerAngle);
        double a2 = toRadians(pilotAngle);
        double x1 = x1(r1, gamma, a1);
        double y1 = y1(r1, gamma, a1);
        double x2 = x2(gamma, a2);
        double y2 = y2(gamma, a2);
        pc.drawLine(0, 0, x1, y1);
        pc.drawLine(x1, y1, x2, y2);
        pc.drawLine(x2, y2, a, o);
    }
}
