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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import static javax.swing.SwingConstants.*;
import org.vesalainen.ham.oscilloscope.ui.GroupLayoutBuilder;

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
    private JTextField timeField;
    private JSlider timeDivisionSlider;
    private JSlider verticalSensitivitySlider;
    private JSlider triggerSlider;
    private TimePanel2 timePanel;
    private FrequencyPanel frequencyPanel;
    private SourceManager sourceManager;
    private JToolBar toolBar;
    private JPanel toolBarPanel;

    public Oscilloscope()
    {
        initFrame();
    }

    private void initFrame()
    {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ExceptionHandler());
        frame = new JFrame("Oscilloscope");
        frame.addWindowListener(this);
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        toolBarPanel = new JPanel(new BorderLayout());
        frame.add(toolBarPanel);
        toolBar = new JToolBar();
        toolBarPanel.add(toolBar, BorderLayout.PAGE_START);
        
        panel = new JPanel();
        toolBarPanel.add(panel, BorderLayout.CENTER);
        
        timePanel = new TimePanel2();
        panel.add(timePanel);
        
        frequencyPanel = new FrequencyPanel(256);
        panel.add(frequencyPanel);
        
        sourceManager = new SourceManager(frame, timePanel, frequencyPanel);
        
        sourceMenu();   
        
        cueSlider = new JSlider(sourceManager.getBoundedRangeModel());
        panel.add(cueSlider);

        timeField = new JTextField(sourceManager.getDocument(), "", 10);
        timeField.setEditable(false);
        toolBar.add(timeField);
        toolBar.add(sourceManager.getStop());
        toolBar.add(sourceManager.getBack());
        toolBar.add(sourceManager.getPlay());
        toolBar.add(sourceManager.getPause());
        toolBar.add(sourceManager.getForward());
        
        timeDivisionSlider = timePanel.createTimeSlider(VERTICAL);
        panel.add(timeDivisionSlider);
        timeDivisionSlider.setMajorTickSpacing(1);
        timeDivisionSlider.setPaintTicks(true);
        timeDivisionSlider.setPaintLabels(true);
        
        verticalSensitivitySlider = new JSlider(VERTICAL);
        panel.add(verticalSensitivitySlider);
        
        triggerSlider = new JSlider(VERTICAL);
        panel.add(triggerSlider);
        GroupLayout layout = GroupLayoutBuilder.builder(panel)
                .addLine(cueSlider)
                .addLine(timePanel, timeDivisionSlider, verticalSensitivitySlider, triggerSlider)
                .addLine(frequencyPanel)
                .build();
        panel.setLayout(layout);
        triggerSlider = new JSlider(VERTICAL);
        triggerSlider.getAccessibleContext();
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void sourceMenu()
    {
        JMenu menu = new JMenu("Source");
        menuBar.add(menu);
        menu.add(sourceManager.getOpenTestSource());
        menu.add(sourceManager.getOpenLineSource());
        menu.add(sourceManager.getOpenFileSource());
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
