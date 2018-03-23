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

import java.awt.Color;
import java.awt.Component;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractListCellRenderer<E> extends JLabel implements ListCellRenderer<E>
{
    private Function<E,String> formatter;
    public AbstractListCellRenderer(Function<E,String> formatter)
    {
        setOpaque(true);
        this.formatter = formatter;
    }

    protected String format(E value)
    {
        return formatter.apply(value);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus)
    {
        setText(format(value));

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index)
        {

            background = Color.BLUE;
            foreground = Color.WHITE;

            // check if this cell is selected
        }
        else 
        {
            if (isSelected)
            {
                background = Color.BLUE;
                foreground = Color.WHITE;

                // unselected, and not the DnD drop location
            }
            else
            {
                background = Color.WHITE;
                foreground = Color.BLACK;
            }
        }
        ;

        setBackground(background);
        setForeground(foreground);

        return this;
    }

}
