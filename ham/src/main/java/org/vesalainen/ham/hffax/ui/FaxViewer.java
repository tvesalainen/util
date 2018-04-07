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
package org.vesalainen.ham.hffax.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.vesalainen.ham.oscilloscope.Oscilloscope;
import org.vesalainen.ham.oscilloscope.TimePanel2;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxViewer extends WindowAdapter
{
    private Image image;
    private JFrame frame;
    private JMenuBar menuBar;
    private JPanel toolBarPanel;
    private JToolBar toolBar;
    private JPanel panel;
    private FaxPanel faxPanel;

    public FaxViewer(Image image)
    {
        this.image = image;
        initFrame();
    }
    private void initFrame()
    {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new FaxViewer.ExceptionHandler());
        frame = new JFrame("Fax Viewer");
        frame.addWindowListener(this);
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        toolBarPanel = new JPanel(new BorderLayout());
        frame.add(toolBarPanel);
        toolBar = new JToolBar();
        toolBarPanel.add(toolBar, BorderLayout.PAGE_START);
        
        panel = new JPanel(new BorderLayout());
        toolBarPanel.add(panel, BorderLayout.CENTER);
        
        faxPanel = new FaxPanel();
        panel.add(faxPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }    

    public void repaint()
    {
        frame.repaint();
    }

    public void setImage(Image image)
    {
        this.image = image;
    }
    
    private class FaxPanel extends JPanel
    {

        @Override
        protected void paintComponent(Graphics graphics)
        {
            Graphics2D g = (Graphics2D) graphics;
            g.drawImage(image, 0, 0, frame);
        }

    }
    private class ExceptionHandler extends EventQueue
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
