/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.oscilloscope;

import static java.awt.Color.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import static java.nio.ByteOrder.*;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.vesalainen.ham.fft.TimeDomain;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;
import org.vesalainen.ui.AbstractView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimePanel extends JPanel implements ChangeListener, SourceListener
{
    private SortedMap<Integer,Double> timeValueMap = new TreeMap<>(); 
    private Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
    private JSlider timeSlider;
    private double sweepTime;
    private int trigger;
    private int[] x;
    private int[] y;
    private int n;
    private AbstractView view = new AbstractView(false);
    private double sampleFrequency;
    private int bitCount;
    private IntArray array;
    
    public TimePanel()
    {
        initTime();
    }

    @Override
    public void update(double sampleFrequency, int bitCount, IntArray array, String channel)
    {
        this.sampleFrequency = sampleFrequency;
        this.bitCount = bitCount;
        if (this.array == null || this.array.length() != array.length())
        {
            this.array = IntArray.getInstance(array.length(), bitCount, BIG_ENDIAN);
            x = new int[array.length()];
            y = new int[array.length()];
        }
        this.array.copy(array);
        updateTimeModel(sampleFrequency, array.length());
    }

    @Override
    public void update()
    {
        repaint();
    }
    
    private void updateTimeModel(double sampleFrequency, double length)
    {
        if (timeSlider != null)
        {
            double maxFreq = length/sampleFrequency;
            double minFreq = 1.0/sampleFrequency;
            int min = timeValueMap.lastKey();
            int max = timeValueMap.firstKey();
            for (Entry<Integer, Double> e : timeValueMap.entrySet())
            {
                if (e.getValue() >= minFreq)
                {
                    min = Math.min(min, e.getKey());
                }
                if (e.getValue() <= maxFreq)
                {
                    max = Math.max(max, e.getKey());
                }
            }
            timeSlider.setMaximum(max);
            timeSlider.setMinimum(min);
        }
    }
    private void updateXY()
    {
        if (array != null)
        {
            int start = trigger(array);
            n = (int) (sweepTime*sampleFrequency);
            int maxAmplitude = Waves.maxAmplitude(bitCount);
            view.setRect(0, n, -maxAmplitude, maxAmplitude);
            Rectangle bounds = getBounds();
            view.setScreen(bounds.width, bounds.height);
            for (int ii=0;ii<n;ii++)
            {
                int s = array.get(start+ii);
                x[ii] = (int) view.toScreenX(ii);
                y[ii] = (int) view.toScreenY(s);
            }
        }
    }
    private int trigger(IntArray array)
    {
        int length = array.length();
        int prev = array.get(0);
        for (int ii=1;ii<length;ii++)
        {
            int cur = array.get(ii);
            if (prev < trigger && cur >= trigger)
            {
                return Math.abs(prev - trigger) < Math.abs(cur - trigger) ? ii : ii-1;
            }
            prev = cur;
        }
        return 0;
    }
    
    @Override
    protected void paintComponent(Graphics graphics)
    {
        Graphics2D g = (Graphics2D) graphics;
        Rectangle b = getBounds();
        g.setBackground(BLACK);
        g.clearRect(0, 0, b.width, b.height);
        painCoordinates(g);
        g.setColor(GREEN);
        updateXY();
        if (n > 0)
        {
            g.drawPolyline(x, y, n);
        }
    }
    private void painCoordinates(Graphics2D g)
    {
        g.setColor(LIGHT_GRAY);
        Rectangle b = getBounds();
        view.setScreen(b.width, b.height);
        view.setRect(0, 10, 0, 10);
        for (int ii=0;ii<10;ii++)
        {
            g.drawLine((int)view.toScreenX(0), (int)view.toScreenY(ii), (int)view.toScreenX(10), (int)view.toScreenY(ii));
            g.drawLine((int)view.toScreenX(ii), (int)view.toScreenY(0), (int)view.toScreenX(ii), (int)view.toScreenY(10));
        }
    }
    
    private void initTime()
    {
        setTime(18, "1 s", 1.0);
        setTime(17, "500 ms", 0.5);
        setTime(16, "250 ms", 0.25);
        setTime(15, "100 ms", 0.1);
        setTime(14, "50 ms", 0.05);
        setTime(13, "25 ms", 0.025);
        setTime(12, "10 ms", 0.010);
        setTime(11, "5 ms", 0.005);
        setTime(10, "2.5 ms", 0.0025);
        setTime(9, "1 ms", 0.001);
        setTime(8, "500 μs", 0.000500);
        setTime(7, "250 μs", 0.000250);
        setTime(6, "100 μs", 0.000100);
        setTime(5, "50 μs", 0.000050);
        setTime(4, "25 μs", 0.000025);
        setTime(3, "10 μs", 0.000010);
        setTime(2, "5 μs", 0.000005);
        setTime(1, "2.5 μs", 0.0000025);
        setTime(0, "1 μs", 0.000001);
    }
    private void setTime(int index, String label, double value)
    {
        timeValueMap.put(index, value);
        labelTable.put(index, new JLabel(label));
    }
    public JSlider createTimeSlider(int orientation)
    {
        JSlider s = new JSlider(orientation, 0, 18, 15);
        s.setLabelTable(labelTable);
        s.addChangeListener(this);
        timeSlider = s;
        return s;
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == timeSlider)
        {
            sweepTime = timeValueMap.get(timeSlider.getValue());
            repaint();
        }
    }
}
