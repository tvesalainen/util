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

import java.util.function.Predicate;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractComboBoxEditor extends BasicComboBoxEditor
{
    private Predicate<String> predicate;

    public AbstractComboBoxEditor(Predicate<String> predicate)
    {
        this.predicate = predicate;
    }
    
    @Override
    protected JTextField createEditorComponent()
    {
        JTextField tf = super.createEditorComponent();
        tf.setInputVerifier(new Verifier());
        return tf;
    }

    private class Verifier extends InputVerifier
    {

        @Override
        public boolean verify(JComponent input)
        {
            JTextField tf = (JTextField) input;
            return predicate.test(tf.getText());
        }
        
    }
}
