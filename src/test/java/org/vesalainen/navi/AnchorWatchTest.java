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

package org.vesalainen.navi;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.ejml.data.DenseMatrix64F;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.vesalainen.math.Circle;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.navi.AnchorWatch.Watcher;
import org.vesalainen.ui.MouldableSector;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatchTest
{
    public static final File dir = new File("target/surefire-reports");
    public AnchorWatchTest()
    {
    }

    /**
     * Test of update method, of class AnchorWatch.
     */
    @Test
    public void testUpdate1()
    {
        URL url = AnchorWatchTest.class.getResource("/20141117091525.nmea");
        CoordinateParser parser = CoordinateParser.newInstance();
        Plotter plotter = new Plotter(1000, 1000);
        plotter.setDir(dir);
        AnchorWatch aw = new AnchorWatch();
        Watcher testWatcher = new TestWatcher();
        aw.addWatcher(testWatcher);
        try
        {
            parser.parse(url, aw);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void testUpdate2()
    {
        URL url = AnchorWatchTest.class.getResource("/20141118090616.nmea");
        CoordinateParser parser = CoordinateParser.newInstance();
        Plotter plotter = new Plotter(1000, 1000);
        plotter.setDir(dir);
        AnchorWatch aw = new AnchorWatch();
        Watcher testWatcher = new TestWatcher();
        aw.addWatcher(testWatcher);
        try
        {
            parser.parse(url, aw);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    private class TestWatcher implements Watcher
    {

        @Override
        public void alarm(double distance)
        {
            fail("alarm="+distance);
        }

        @Override
        public void location(double x, double y)
        {
        }

        @Override
        public void area(ConvexPolygon area)
        {
        }

        @Override
        public void outer(DenseMatrix64F path)
        {
        }

        @Override
        public void estimated(Circle estimated)
        {
        }

        @Override
        public void safeSector(MouldableSector safe)
        {
        }
        
    }
    private class PlottingWatcher implements Watcher
    {
        private Plotter plotter;
        private int plot;

        public PlottingWatcher(Plotter plotter)
        {
            this.plotter = plotter;
        }
        
        @Override
        public void location(double x, double y)
        {
            System.err.println("location("+x+", "+y+")");
            plotter.setColor(Color.BLACK);
            plotter.drawPoint(x, y);
        }

        @Override
        public void area(ConvexPolygon area)
        {
            for (int r=0;r<area.points.numRows;r++)
            {
                System.err.println("area("+area.points.data[2*r]+", "+area.points.data[2*r+1]+")");
            }
            plotter.setColor(Color.BLUE);
            plotter.drawPolygon(area);
        }

        @Override
        public void outer(DenseMatrix64F path)
        {
            for (int r=0;r<path.numRows;r++)
            {
                System.err.println("outer("+path.data[2*r]+", "+path.data[2*r+1]+")");
            }
            plotter.setColor(Color.ORANGE);
            plotter.drawLines(path);
        }

        @Override
        public void estimated(Circle estimated)
        {
            plotter.setColor(Color.GREEN);
            plotter.drawCircle(estimated);
            plot();
        }

        @Override
        public void safeSector(MouldableSector safe)
        {
        }

        @Override
        public void alarm(double distance)
        {
            System.err.println("alarm("+distance+")");
        }

        public void plot()
        {
            try
            {
                if (plot % 50 == 0)
                {
                    double meters = AnchorWatch.toMeters(4.986421120771726E-4);
                    plotter.setColor(Color.GRAY);
                    plotter.drawCircle(-13.602631724919243, 28.13095300208171, 4.986421120771726E-4);
                    plotter.plot("test" + plot, "png");
                }
                plotter.clear();
                plot++;
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }

    }
}
