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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager
{
    private Source source;
    private Frame frame;
    private SourceListener[] listeners;
    private OpenTestSource openTestSource = new OpenTestSource();
    private OpenLineSource openLineSource = new OpenLineSource();

    public SourceManager(Frame frame, SourceListener... listeners)
    {
        this.frame = frame;
        this.listeners = listeners;
    }

    public OpenLineSource getOpenLineSource()
    {
        return openLineSource;
    }

    public OpenTestSource getOpenTestSource()
    {
        return openTestSource;
    }
    
    public void setSource(Source source)
    {
        if (this.source != null)
        {
            this.source.stop();
        }
        this.source = source;
        for (SourceListener l : listeners)
        {
            source.addListener(l);
        }
        source.start();
        frame.setTitle(source.toString());
    }
    
    public class OpenLineSource extends AbstractAction
    {
        private LineDialog dia = new LineDialog(frame, "Open Line Source");
        public OpenLineSource()
        {
            super("Open Line Source");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (dia.edit())
            {
                setSource(new LineSource(dia.getAudioFormat(), dia.getMixerInfo(), dia.getRefreshInterval()));
            }
        }
        
    }
    public class OpenTestSource extends AbstractAction
    {

        public OpenTestSource()
        {
            super("Open Test Source");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            setSource(new TestSource(8000, 16));
        }
        
    }
}
