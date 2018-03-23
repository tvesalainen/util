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
package org.vesalainen.ham.oscilloscope.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GroupLayoutBuilder
{
    private GroupLayout layout;
    private List<Component[]> grid = new ArrayList<>();
    private int maxLength;
    
    private GroupLayoutBuilder(Container host)
    {
        this.layout = new GroupLayout(host);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
    }
    public static GroupLayoutBuilder builder(Container host)
    {
        return new GroupLayoutBuilder(host);
    }
    public GroupLayoutBuilder addLine(Component... components)
    {
        grid.add(components);
        maxLength = Math.max(maxLength, components.length);
        return this;
    }

    public GroupLayout build()
    {
        GroupLayout.SequentialGroup seq = layout.createSequentialGroup();
        layout.setHorizontalGroup(seq);
        for (int ll=0;ll<maxLength;ll++)
        {
            GroupLayout.ParallelGroup par = layout.createParallelGroup();
            for (Component[] line : grid)
            {
                if (line.length > ll)
                {
                    Component c = line[ll];
                    if (c != null)
                    {
                        par.addComponent(c);
                    }
                }
            }
            seq.addGroup(par);
        }
        seq = layout.createSequentialGroup();
        layout.setVerticalGroup(seq);
        for (Component[] line : grid)
        {
            GroupLayout.ParallelGroup par = layout.createParallelGroup();
            for (Component c : line)
            {
                if (c != null)
                {
                    par.addComponent(c);
                }
            }
            seq.addGroup(par);
        }
        return layout;
    }
}
