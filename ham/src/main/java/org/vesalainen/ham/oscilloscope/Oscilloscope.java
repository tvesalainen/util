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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import static javax.swing.SwingConstants.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Oscilloscope extends WindowAdapter
{

    private JFrame frame;
    private JPanel panel;
    private JMenuBar menuBar;
    private JSlider cueSlider;
    private JSlider timeDivisionSlider;
    private JSlider verticalSensitivitySlider;
    private JSlider triggerSlider;
    private TimePanel timePanel;
    private FrequencyPanel frequencyPanel;

    public Oscilloscope()
    {
        initFrame();
    }
    
    private void initFrame()
    {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ExceptionHandler());
        frame = new JFrame("Oscilloscope");
        frame.addWindowListener(this);
        panel = new JPanel();
        
        timePanel = new TimePanel();
        panel.add(timePanel);
        
        frequencyPanel = new FrequencyPanel();
        panel.add(frequencyPanel);
        
        cueSlider = new JSlider(HORIZONTAL);
        panel.add(cueSlider);
        
        timeDivisionSlider = timePanel.createTimeSlider(VERTICAL);
        panel.add(timeDivisionSlider);
        timeDivisionSlider.setMajorTickSpacing(1);
        timeDivisionSlider.setPaintTicks(true);
        timeDivisionSlider.setPaintLabels(true);
        
        verticalSensitivitySlider = new JSlider(VERTICAL);
        panel.add(verticalSensitivitySlider);
        
        triggerSlider = new JSlider(VERTICAL);
        panel.add(triggerSlider);
        
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                .addComponent(cueSlider)
                .addGroup(
                        layout.createSequentialGroup()
                        .addComponent(timePanel)
                        .addComponent(timeDivisionSlider)
                        .addComponent(verticalSensitivitySlider)
                        .addComponent(triggerSlider)
                )
                .addComponent(frequencyPanel)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(cueSlider)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(timePanel)
                        .addComponent(timeDivisionSlider)
                        .addComponent(verticalSensitivitySlider)
                        .addComponent(triggerSlider)
                )
                .addComponent(frequencyPanel)
        );
        triggerSlider = new JSlider(VERTICAL);
        frame.add(panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public class ExceptionHandler extends EventQueue
    {

        @Override
        protected void dispatchEvent(AWTEvent event)
        {
            try
            {
                super.dispatchEvent(event);
            }
            catch (Throwable thr)
            {
                thr.printStackTrace();  // TODO logging
                String message = thr.getMessage();
                if (message == null || message.isEmpty())
                {
                    message = "Fatal: "+thr.getClass();
                }
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }
}
