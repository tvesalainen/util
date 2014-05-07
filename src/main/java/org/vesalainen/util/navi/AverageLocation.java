/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util.navi;


/**
 *
 * @author tkv
 */
public class AverageLocation
{
    private Average x = new Average();
    private Average y = new Average();
    private int count;

    public AverageLocation()
    {
    }

    public AverageLocation(AverageLocation a)
    {
        x = new Average(a.x);
        y = new Average(a.y);
    }

    public AverageLocation(AverageLocation a1, AverageLocation a2)
    {
        x = new Average(a1.x, a2.x);
        y = new Average(a1.y, a2.y);
        count = a1.count + a2.count;
    }

    public void add(Location... location)
    {
        for (Location l : location)
        {
            add(l);
        }
    }
    public void add(Location location)
    {
        x.add(location.getX());
        y.add(location.getY());
        count++;
    }
    public Location getAverage()
    {
        return new Location(y.getAverage(), x.getAverage());
    }

    public double getDeviation()
    {
        return x.getDeviation()+y.getDeviation();
    }

    public void reset()
    {
        x.reset();
        y.reset();
        count = 0;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return getAverage().toString();
    }

}
