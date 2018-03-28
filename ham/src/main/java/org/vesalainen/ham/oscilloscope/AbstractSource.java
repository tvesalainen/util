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

import java.util.ArrayList;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.vesalainen.ham.SampleBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSource implements Source
{
    protected List<SourceListener> listeners = new ArrayList<>();
    protected BoundedRangeModel model;
    protected Document document;

    public void fireUpdate()
    {
        for (SourceListener l : listeners)
        {
            l.update();
        }
    }
    public void fireUpdate(SampleBuffer samples)
    {
        for (SourceListener l : listeners)
        {
            l.update(samples);
        }
    }
    @Override
    public void stop()
    {
        listeners.clear();
    }

    @Override
    public void addListener(SourceListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void play()
    {
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void back()
    {
    }

    @Override
    public void forward()
    {
    }

    protected void setText(String text)
    {
        try
        {
            int length = document.getLength();
            document.remove(0, length);
            document.insertString(0, text, null);
        }
        catch (BadLocationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void setDocument(Document document)
    {
        this.document = document;
    }

    @Override
    public void setBoundedRangeModel(BoundedRangeModel model)
    {
        this.model = model;
    }
    
}
