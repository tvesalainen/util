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
import java.nio.file.Paths;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.vesalainen.ham.oscilloscope.ui.SimpleAction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager
{
    private Source source;
    private Frame frame;
    private SourceListener[] listeners;
    private BoundedRangeModel boundedRangeModel = new DefaultBoundedRangeModel();
    private Document document = new PlainDocument();
    private OpenTestSource openTestSource = new OpenTestSource();
    private OpenLineSource openLineSource = new OpenLineSource();
    private OpenFileSource openFileSource = new OpenFileSource();
    private Action stop;
    private Action play;
    private Action pause;
    private Action back;
    private Action forward;

    public SourceManager(Frame frame, SourceListener... listeners)
    {
        this.frame = frame;
        this.listeners = listeners;
        this.stop = new SimpleAction("Stop", this::stop);
        this.play = new SimpleAction("Play", this::play);
        this.pause = new SimpleAction("Pause", this::pause);
        this.back = new SimpleAction("Back", this::back);
        this.forward = new SimpleAction("Forward", this::forward);
    }

    private void stop()
    {
        if (source != null)
        {
            source.stop();
        }
    }
    private void play()
    {
        if (source != null)
        {
            source.play();
        }
    }
    private void pause()
    {
        if (source != null)
        {
            source.pause();
        }
    }
    private void back()
    {
        if (source != null)
        {
            source.back();
        }
    }
    private void forward()
    {
        if (source != null)
        {
            source.forward();
        }
    }

    public Action getStop()
    {
        return stop;
    }

    public Action getPlay()
    {
        return play;
    }

    public Action getPause()
    {
        return pause;
    }

    public Action getBack()
    {
        return back;
    }

    public Action getForward()
    {
        return forward;
    }
    
    public OpenFileSource getOpenFileSource()
    {
        return openFileSource;
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
        source.setBoundedRangeModel(boundedRangeModel);
        source.setDocument(document);
        source.start();
        frame.setTitle(source.toString());
    }

    public BoundedRangeModel getBoundedRangeModel()
    {
        return boundedRangeModel;
    }

    public Document getDocument()
    {
        return document;
    }
    
    public class OpenFileSource extends AbstractAction
    {
        private FileDialog dia = new FileDialog(frame, "Open File Source");

        public OpenFileSource()
        {
            super("Open File Source");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (dia.edit())
            {
                setSource(new FileSource(Paths.get(dia.getFilename()), dia.getRefreshInterval()));
            }
        }
        
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
                setSource(new LineSource(dia.getAudioFormat(), dia.getMixerInfo(), dia.getRefreshInterval(), dia.getAGCPort()));
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
