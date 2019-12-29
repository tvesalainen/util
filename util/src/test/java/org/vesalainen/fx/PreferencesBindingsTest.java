/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PreferencesBindingsTest
{
    PreferencesBindings bindings = PreferencesBindings.userNodeForPackage(PreferencesBindingsTest.class);
    
    @After
    public void after()
    {
        bindings.clear();
    }
    public PreferencesBindingsTest()
    {
        StringBinding sp = bindings.createStringBinding("foo", "def");
        SimpleStringProperty ssp = new SimpleStringProperty();
        bindings.bindBiDirectional("foo", "def", ssp);
        ssp.setValue("bar");
        assertEquals("bar", sp.getValue());
        assertEquals("bar", ssp.getValue());
    }

    @Test
    public void test()
    {
    }
    
}
