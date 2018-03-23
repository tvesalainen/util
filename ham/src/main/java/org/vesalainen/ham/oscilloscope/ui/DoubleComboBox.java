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
public class DoubleComboBox extends AbstractComboBox<Double>
{
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;

    public DoubleComboBox(ComboBoxModel<Double> aModel)
    {
        super(aModel);
    }

    public DoubleComboBox(Double... items)
    {
        super(items);
    }

    public DoubleComboBox()
    {
    }

    public void setMin(double min)
    {
        this.min = min;
    }

    public void setMax(double max)
    {
        this.max = max;
    }
    
    @Override
    protected boolean verify(String text)
    {
        try
        {
            double v = Double.parseDouble(text);
            return v >= min && v <= max;
        }
        catch (NumberFormatException ex)
        {
            return false;
        }
    }
    
}
