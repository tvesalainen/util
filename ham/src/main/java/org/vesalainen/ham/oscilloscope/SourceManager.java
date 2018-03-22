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

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager
{
    private Source source;
    private Consumer<String> title;
    private SourceListener[] listeners;
    private OpenTestSource openTestSource = new OpenTestSource();

    public SourceManager(Consumer<String> title, SourceListener... listeners)
    {
        this.title = title;
        this.listeners = listeners;
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
        title.accept(source.toString());
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
