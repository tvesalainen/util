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

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractComboBox<E> extends JComboBox<E>
{

    public AbstractComboBox(ComboBoxModel<E> aModel)
    {
        super(aModel);
        init();
    }

    public AbstractComboBox(E... items)
    {
        super(items);
        init();
    }

    public AbstractComboBox()
    {
        init();
    }
    private void init()
    {
        setEditor(new AbstractComboBoxEditor(this::verify));
        setRenderer(new AbstractListCellRenderer<E>(this::format));
    }
    
    protected String format(E value)
    {
        return value != null ? value.toString() : "";
    }
    protected abstract boolean verify(String text);
    
    @Override
    public E[] getSelectedObjects()
    {
        return (E[]) super.getSelectedObjects();
    }

    @Override
    public E getSelectedItem()
    {
        return (E) super.getSelectedItem();
    }
    
}
