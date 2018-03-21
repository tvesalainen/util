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

import java.awt.Color;
import static java.awt.Color.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.vesalainen.ham.fft.TimeDomain;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimePanel extends JPanel implements ChangeListener
{
    private Map<Integer,Double> timeValueMap = new HashMap<>(); 
    private Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
    private JSlider timeSlider;
    private double sweepTime;
    private int trigger;
    private int[] x;
    private int[] y;
    private int n;
    
    public TimePanel()
    {
        initTime();
    }

    public void update(double sampleFrequency, int bitCount, IntArray array)
    {
        int length = array.length();
        // raising trigger
        int start = trigger(array);
        n = (int) (sweepTime*sampleFrequency);
        if (x == null || x.length < n)
        {
            x = new int[n];
            y = new int[n];
        }
        int maxAmplitude = maxAmplitude(bitCount);
        Rectangle bounds = getBounds();
        int halfHeight = bounds.height/2;
        for (int ii=0;ii<n;ii++)
        {
            int s = array.get(start+ii);
            x[ii] = ii*bounds.height/n;
            y[ii] = s*halfHeight/maxAmplitude+halfHeight;
        }
        repaint();
    }
    private int maxAmplitude(int bitCount)
    {
        switch (bitCount)
        {
            case 8:
                return Byte.MAX_VALUE;
            case 16:
                return Short.MAX_VALUE;
            case 32:
                return Integer.MAX_VALUE;
            default:
                throw new UnsupportedOperationException(bitCount+" not supported");
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
                return ii;
            }
            prev = cur;
        }
        throw new IllegalArgumentException("trigger failed");
    }
    @Override
    protected void paintComponent(Graphics graphics)
    {
        Graphics2D g = (Graphics2D) graphics;
        Rectangle b = getBounds();
        g.setBackground(BLACK);
        g.clearRect(0, 0, b.width, b.height);
        // coordinates
        g.setColor(LIGHT_GRAY);
        int cw = b.height/10;
        int ch = b.width/10;
        for (int ii=0;ii<10;ii++)
        {
            g.drawLine(0, ii*cw, b.width, ii*cw);
            g.drawLine(ii*ch, 0, ii*ch, b.height);
        }
        g.setColor(GREEN);
        if (n > 0)
        {
            g.drawPolyline(x, y, n);
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
        s.setName("time");
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
            TimeDomain sample = Waves.createSample(8000, 0, 1, TimeUnit.SECONDS, Waves.of(440, 100000, 0));
            update(8000, 32, sample.getSamples());
            repaint();
        }
    }
}
