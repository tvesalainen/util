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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IntegerComboBox extends AbstractComboBox<Integer>
{
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public IntegerComboBox(ComboBoxModel<Integer> aModel)
    {
        super(aModel);
    }

    public IntegerComboBox(Integer... items)
    {
        super(items);
    }

    public IntegerComboBox()
    {
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public void setMax(int max)
    {
        this.max = max;
    }
    
    @Override
    protected boolean verify(String text)
    {
        try
        {
            int v = Integer.parseInt(text);
            return v >= min && v <= max;
        }
        catch (NumberFormatException ex)
        {
            return false;
        }
    }
    
}
