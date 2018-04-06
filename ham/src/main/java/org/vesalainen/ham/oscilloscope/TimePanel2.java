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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.fft.FFTFrequencyDetector;
import org.vesalainen.ham.hffax.FrequencyCounter;
import org.vesalainen.ui.AbstractView;
import org.vesalainen.util.IntRangeMap;
import org.vesalainen.util.IntRangeMapBuilder;
import org.vesalainen.util.SimpleIntRange;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimePanel2 extends JPanel implements ChangeListener, SourceListener
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
    private int maxAmplitude;
    private SampleBuffer samples;
    private FrequencyCounter counter;
    private FFTFrequencyDetector fd;
    private IntRangeMap<Color> colorMap;
    private List<Col> cols = new ArrayList<>();
    
    public TimePanel2()
    {
        initTime();
        IntRangeMapBuilder<Color> b = new IntRangeMapBuilder<>();
        b.put(new SimpleIntRange(0, 1100), orange);
        b.put(new SimpleIntRange(1100, 1900), red);
        b.put(new SimpleIntRange(1900, 2700), blue);
        b.put(new SimpleIntRange(2700, 48000), green);
        colorMap = b.build();
            
    }

    @Override
    public void update(SampleBuffer samples)
    {
        this.sampleFrequency = samples.getSampleFrequency();
        this.maxAmplitude = samples.getMaxAmplitude();
        if (this.samples == null || this.samples.getViewLength() != samples.getViewLength())
        {
            x = new int[samples.getViewLength()];
            y = new int[samples.getViewLength()];
        }
        this.samples = samples;
        updateTimeModel(sampleFrequency, samples.getViewLength());
        counter = new FrequencyCounter((int) sampleFrequency);
        fd = new FFTFrequencyDetector(64, (int) sampleFrequency);
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
            double maxGrid = length/sampleFrequency/10;
            double minGrid = 1.0/sampleFrequency/10;
            int min = timeValueMap.lastKey();
            int max = timeValueMap.firstKey();
            for (Entry<Integer, Double> e : timeValueMap.entrySet())
            {
                if (e.getValue() >= minGrid)
                {
                    min = Math.min(min, e.getKey());
                }
                if (e.getValue() < maxGrid)
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
        if (samples != null)
        {
            int start = trigger();
            n = (int) (sweepTime*sampleFrequency*10);   // sweepTime for rid square
            view.setRect(0, n, -maxAmplitude, maxAmplitude);
            Rectangle bounds = getBounds();
            view.setScreen(bounds.width, bounds.height);
            Color p = null;
            Col col = null;
            cols.clear();
            for (int ii=0;ii<n;ii++)
            {
                int s = samples.get(start+ii, 0);   // 0 = channel
                fd.update(s);
                float f = (float) fd.getFrequency();
                Color c = colorMap.get((int) (Float.isFinite(f) ? f : 0));
                if (c != null)
                {
                    if (!c.equals(p) || col.count == 100)
                    {
                        if (col != null)
                        {
                            col.color = c;
                        }
                        col = new Col();
                        cols.add(col);
                    }
                    col.add((int) view.toScreenX(ii), (int) view.toScreenY(s));
                    p = c;
                }
            }
        }
    }
    private int trigger()
    {
        int half = samples.getViewLength()/2;
        int prev = samples.get(half, 0);
        for (int ii=1;ii<half;ii++)
        {
            int cur = samples.get(half+ii, 0);
            if (prev < trigger && cur >= trigger)
            {
                return Math.abs(prev - trigger) > Math.abs(cur - trigger) ? ii : ii-1;
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
        g.setColor(GREEN);
        updateXY();
        for (Col col : cols)
        {
            col.paint(g);
        }
        painCoordinates(g);
    }
    private void painCoordinates(Graphics2D g)
    {
        g.setColor(new Color(255, 255, 255, 50));
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
    private class Col
    {
        Color color = black;
        int[] x;
        int[] y;
        int count;

        public Col()
        {
            this.x = new int[100];
            this.y = new int[100];
        }
        void add(int x, int y)
        {
            this.x[count] = x;
            this.y[count++] = y;
        }
        void paint(Graphics2D g)
        {
            g.setColor(color);
            g.drawPolyline(x, y, count);
        }
    }
}
