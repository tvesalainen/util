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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PreferencesBindings
{
    private static final DoubleStringConverter DOUBLE_STRING_CONVERTER = new DoubleStringConverter();
    private static final BooleanStringConverter BOOLEAN_STRING_CONVERTER = new BooleanStringConverter();
    private static final FloatStringConverter FLOAT_STRING_CONVERTER = new FloatStringConverter();
    private static final IntegerStringConverter INTEGER_STRING_CONVERTER = new IntegerStringConverter();
    private static final LongStringConverter LONG_STRING_CONVERTER = new LongStringConverter();
    
    private Preferences preferences;
    private Map<String,PropertyImpl> properties = new HashMap<>();
    private Map<ObservableValue<?>,String> observables = new WeakHashMap<>();

    private PreferencesBindings(Preferences preferences)
    {
        this.preferences = preferences;
    }
    
    public static PreferencesBindings systemNodeForPackage(Class<?> cls)
    {
        return new PreferencesBindings(Preferences.systemNodeForPackage(cls));
    }
    public static PreferencesBindings systemRoot()
    {
        return new PreferencesBindings(Preferences.systemRoot());
    }
    public static PreferencesBindings userNodeForPackage(Class<?> cls)
    {
        return new PreferencesBindings(Preferences.systemNodeForPackage(cls));
    }
    public static PreferencesBindings userRoot()
    {
        return new PreferencesBindings(Preferences.systemRoot());
    }

    public StringBinding createStringBinding(String key, String def)
    {
        return Bindings.createStringBinding(()->preferences.get(key, def), getProperty(key, def));
    }
    public DoubleBinding createDoubleBinding(String key, double def)
    {
        String sDef = DOUBLE_STRING_CONVERTER.toString(def);
        return Bindings.createDoubleBinding(()->DOUBLE_STRING_CONVERTER.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public BooleanBinding createBooleanBinding(String key, boolean def)
    {
        String sDef = BOOLEAN_STRING_CONVERTER.toString(def);
        return Bindings.createBooleanBinding(()->BOOLEAN_STRING_CONVERTER.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public FloatBinding createFloatBinding(String key, float def)
    {
        String sDef = FLOAT_STRING_CONVERTER.toString(def);
        return Bindings.createFloatBinding(()->FLOAT_STRING_CONVERTER.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public IntegerBinding createIntegerBinding(String key, int def)
    {
        String sDef = INTEGER_STRING_CONVERTER.toString(def);
        return Bindings.createIntegerBinding(()->INTEGER_STRING_CONVERTER.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public LongBinding createLongBinding(String key, long def)
    {
        String sDef = LONG_STRING_CONVERTER.toString(def);
        return Bindings.createLongBinding(()->LONG_STRING_CONVERTER.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public <T> ObjectBinding<T> createObjectBinding(String key, T def, StringConverter<T> converter)
    {
        String sDef = converter.toString(def);
        return Bindings.createObjectBinding(()->converter.fromString(preferences.get(key, sDef)), getProperty(key, sDef));
    }
    public void bindBiDirectional(String key, String def, Property<String> property)
    {
        Bindings.bindBidirectional(getProperty(key, def), property);
    }
    private Property<String> getProperty(String key, String def)
    {
        PropertyImpl property = properties.get(key);
        if (property == null)
        {
            property = new PropertyImpl(key, def);
            properties.put(key, property);
        }
        return property;
    }

    public void clear()
    {
        try
        {
            preferences.clear();
        }
        catch (BackingStoreException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private class PropertyImpl implements Property<String>
    {
        private String key;
        private String def;
        private List<InvalidationListener> invalidationListeners = new ArrayList<>();

        public PropertyImpl(String key, String def)
        {
            this.key = key;
            this.def = def;
        }
        
        @Override
        public void bind(ObservableValue<? extends String> observable)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void unbind()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isBound()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void bindBidirectional(Property<String> other)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void unbindBidirectional(Property<String> other)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getBean()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return key;
        }

        @Override
        public void addListener(ChangeListener<? super String> listener)
        {
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener)
        {
        }

        @Override
        public String getValue()
        {
            return preferences.get(key, def);
        }

        @Override
        public void addListener(InvalidationListener listener)
        {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener)
        {
            invalidationListeners.remove(listener);
        }

        @Override
        public void setValue(String value)
        {
            if (value != null)
            {
                preferences.put(key, value);
            }
            else
            {
                preferences.remove(key);
            }
            invalidationListeners.forEach((obs) ->
            {
                obs.invalidated(this);
            });
        }
        
    }
}
