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

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Source
{
    void start();
    void stop();
    void play();
    void pause();
    void back();
    void forward();
    void addListener(SourceListener listener);
    void setDocument(Document document);
    void setBoundedRangeModel(BoundedRangeModel model);
}
