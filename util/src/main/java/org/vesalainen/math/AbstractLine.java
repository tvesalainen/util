/*
 * Line2D.java
 *
 * Created on 15. tammikuuta 2005, 9:55
 */

package org.vesalainen.math;

/**
 * Line implementation
 * 
 * <p>If line is vertical slope = infinity, a = constant x
 * @author  tkv
 */
public class AbstractLine implements Line
{
    protected double a;
    protected double slope;
    /**
     * Creates AbstractLine with slope and going through p
     * @param slope
     * @param p 
     */
    public AbstractLine(double slope, Point p)
    {
        this(slope, p.getX(), p.getY());
    }
    /**
     * Creates AbstractLine with slope and going through (x, y)
     * @param slope
     * @param x
     * @param y 
     */
    public AbstractLine(double slope, double x, double y)
    {
        set(slope, x, y);
    }
    
    /**
     * Creates AbstractLine going through p1 and p2
     * @param p1
     * @param p2 
     */
    public AbstractLine(Point p1, Point p2)
    {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    /**
     * Creates AbstractLine going through (x1, y1) and (x2, y2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2 
     */
    public AbstractLine(double x1, double y1, double x2, double y2)
    {
        set(x1, y1, x2, y2);
    }
    
    /**
     * Populates line
     * @param slope
     * @param p 
     */
    public final void set(double slope, Point p)
    {
        set(slope, p.getX(), p.getY());
    }
    public final void set(double slope, double x, double y)
    {
        this.slope = slope;
        if (Double.isInfinite(slope))
        {
            this.a = x;
        }
        else
        {
            this.a = y - slope*x;
        }
    }
    /**
     * Populates line
     * @param p1
     * @param p2 
     */
    public final void set(Point p1, Point p2)
    {
        set(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    public final void set(double x1, double y1, double x2, double y2)
    {
        if (x2 != x1)
        {
            this.slope = (y2 - y1) / (x2 - x1);
            this.a = y1 - slope*x1;
        }
        else
        {
            this.slope = Double.POSITIVE_INFINITY;
            this.a = x1;
        }
    }

    /**
     * <p>Returns y for x If slope != infinity
     * <p>Returns infinity If slope == infinity and x == constant x
     * <p>Returns nan If slope == infinity and x != constant x
     * @param x
     * @return 
     */
    @Override
    public double getY(double x)
    {
        if (!Double.isInfinite(slope))
        {
            return slope*x + a;
        }
        else
        {
            if (x == a)
            {
                return Double.POSITIVE_INFINITY;
            }
            else
            {
                return Double.NaN;
            }
        }
    }
    /**
     * <p>Returns a If slope != infinity
     * <p>Returns constant x If slope == infinity
     * @return 
     */
    @Override
    public double getA()
    {
        return a;
    }
    /**
     * Returns slope. For vertical lines infinity
     * @return 
     */
    @Override
    public double getSlope()
    {
        return slope;
    }
    /**
     * Returns the crosspoint of lines l1 and l2. If lines don't cross returns null.
     * 
     * <p>Note returns null if lines are parallel and also when lines ate equal.!
     * @param l1
     * @param l2
     * @return 
     */
    public static Point crossPoint(Line l1, Line l2)
    {
        return crossPoint(l1, l2, null);
    }
    /**
     * Returns the crosspoint of lines l1 and l2. If lines don't cross returns null.
     * 
     * <p>Note returns null if lines are parallel and also when lines ate equal.!
     * 
     * <p>If p != null returns populated p. 
     * <p>If returns null p is not updated.
     * 
     * @param l1
     * @param l2
     * @param p
     * @return 
     */
    public static Point crossPoint(Line l1, Line l2, AbstractPoint p)
    {
        if (Double.isInfinite(l1.getSlope()))
        {
            if (Double.isInfinite(l2.getSlope()))
            {
                return null;
            }
            else
            {
                return cyclePoint(p, l1.getA(), l2.getY(l1.getA()));
            }
        }
        if (Double.isInfinite(l2.getSlope()))
        {
            return cyclePoint(p, l2.getA(), l1.getY(l2.getA()));
        }
        double x1 = 0;
        double y1 = l1.getY(x1);
        
        double x2 = 10;
        double y2 = l1.getY(x2);
        
        double x3 = 0;
        double y3 = l2.getY(x3);
        
        double x4 = 10;
        double y4 = l2.getY(x4);
        
        double dd = det(x1-x2, y1-y2, x3-x4, y3-y4);
        if (dd == 0)
        {
            return null;
        }
        double x1y1x2y2 = det(x1, y1, x2, y2);
        double x3y3x4y4 = det(x3, y3, x4, y4);
        double xu = det(x1y1x2y2, x1-x2, x3y3x4y4, x3-x4);
        double yu = det(x1y1x2y2, y1-y2, x3y3x4y4, y3-y4);
        return cyclePoint(p, xu/dd, yu/dd);
        
    }
    private static Point cyclePoint(AbstractPoint p, double x, double y)
    {
        if (p == null)
        {
            return new AbstractPoint(x, y);
        }
        else
        {
            p.set(x, y);
            return p;
        }
    }
            
    private static double det(double a11, double a12, double a21, double a22)
    {
        return  a11*a22-a12*a21;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.a) ^ (Double.doubleToLongBits(this.a) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.slope) ^ (Double.doubleToLongBits(this.slope) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AbstractLine other = (AbstractLine) obj;
        if (Double.doubleToLongBits(this.a) != Double.doubleToLongBits(other.a))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.slope) != Double.doubleToLongBits(other.slope))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "AbstractLine{" + "a=" + a + ", slope=" + slope + '}';
    }
    
}
