/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of3
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham.oscilloscope;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.fft.FFT;
import org.vesalainen.ham.fft.FrequencyDomain;
import org.vesalainen.ham.fft.FrequencyDomainImpl;
import org.vesalainen.ui.ScreenPlotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyPanel extends JPanel implements SourceListener
{
    private FFT fft;
    private double sampleFrequency;
    private int maxAmplitude;
    private SampleBuffer samples;
    private FrequencyDomain frequencyDomain;
    
    public FrequencyPanel(int n)
    {
        this.fft = new FFT(n);
    }
    
    @Override
    public void update(SampleBuffer samples)
    {
        this.sampleFrequency = samples.getSampleFrequency();
        this.maxAmplitude = samples.getMaxAmplitude();
        this.frequencyDomain = new FrequencyDomainImpl(sampleFrequency, fft);
        this.samples = samples;
    }

    @Override
    public void update()
    {
        fft.forward(samples, 0);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics graphics)
    {
        if (frequencyDomain != null)
        {
            Graphics2D g = (Graphics2D) graphics;
            ScreenPlotter plotter = new ScreenPlotter(this);
            plotter.setColor(Color.yellow);
            frequencyDomain.stream(0.0)
                    .forEach((f)->plotter.drawLine(f.getFrequency(), 0, f.getFrequency(), f.getMagnitude()));
            plotter.setFont("Arial", BOLD, 5000);
            plotter.setColor(new Color(255, 255, 255, 50));
            plotter.drawCoordinateX();
            plotter.plot(g);
        }
    }
}
