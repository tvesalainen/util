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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.Circle;
import org.vesalainen.math.ConvexPolygon;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.navi.AnchorWatch.Watcher;
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

    @Test
    public void testSimulate() throws InterruptedException
    {
        try
        {
            Plotter plotter = new Plotter(1000, 1000);
            plotter.setDir(dir);
            AnchorWatch aw = new AnchorWatch();
            Watcher testWatcher = new TestWatcher();
            aw.addWatcher(testWatcher);
            SimulatorLocationSource simulator = new SimulatorLocationSource(null, 10, false);
            LocationSource.register("simu", simulator);
            LocationSource.addObserver(aw);
            LocationSource.activate("simu");
        }
        catch (Exception ex)
        {
            Logger.getLogger(AnchorWatchTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Test of update method, of class AnchorWatch.
     */
    @Test
    public void testUpdate1()
    {
        URL url = AnchorWatchTest.class.getResource("/data.ser");
        Plotter plotter = new Plotter(1000, 1000);
        plotter.setDir(dir);
        AnchorWatch aw = new AnchorWatch();
        Watcher testWatcher = new TestWatcher();
        aw.addWatcher(testWatcher);
        int count=0;
        try (DataInputStream dis = new DataInputStream(url.openStream()))
        {
            while (true)
            {
                if (count==152)
                {
                    System.err.println();
                }
                float lon = dis.readFloat();
                float lat = dis.readFloat();
                aw.update(lon, lat, System.currentTimeMillis(), 1);
                count++;
            }
        }
        catch (EOFException ex)
        {
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void testSerialize()
    {
        AnchorWatch aw = new AnchorWatch();
        aw.update(25.1, 60.1, System.currentTimeMillis(), 1);
        aw.update(25.2, 60.0, System.currentTimeMillis(), 1);
        aw.update(25.3, 60.2, System.currentTimeMillis(), 1);
        aw.update(25.3, 60.1, System.currentTimeMillis(), 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oas = new ObjectOutputStream(baos))
        {
            oas.writeObject(aw);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais))
        {
            Object ob = ois.readObject();
            assertTrue(ob instanceof AnchorWatch);
            AnchorWatch aw2 = (AnchorWatch) ob;
        }
        catch (IOException | ClassNotFoundException ex)
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
        public void location(double x, double y, long time, double accuracy, double speed)
        {
        }

        @Override
        public void area(Polygon area)
        {
        }

        @Override
        public void outer(Polygon path)
        {
        }

        @Override
        public void estimated(Circle estimated)
        {
        }

        @Override
        public void safeSector(SafeSector safe)
        {
        }
        
        @Override
        public void suggestNextUpdateIn(double seconds, double meters)
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
        public void location(double x, double y, long time, double accuracy, double speed)
        {
            System.err.println("location("+x+", "+y+")");
            plotter.setColor(Color.BLACK);
            plotter.drawPoint(x, y);
        }

        @Override
        public void area(Polygon area)
        {
            for (int r=0;r<area.count();r++)
            {
                System.err.println("area("+area.getX(r)+", "+area.getY(r)+")");
            }
            plotter.setColor(Color.BLUE);
            plotter.drawPolygon(area);
        }

        @Override
        public void outer(Polygon area)
        {
            for (int r=0;r<area.count();r++)
            {
                System.err.println("outer("+area.getX(r)+", "+area.getY(r)+")");
            }
            plotter.setColor(Color.ORANGE);
            plotter.drawLines(area);
        }

        @Override
        public void estimated(Circle estimated)
        {
            plotter.setColor(Color.GREEN);
            plotter.drawCircle(estimated);
            plot();
        }

        @Override
        public void safeSector(SafeSector safe)
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

        @Override
        public void suggestNextUpdateIn(double seconds, double meters)
        {
            System.err.println("suggestNextUpdateIn("+seconds+", "+meters+")");
        }

    }
}
