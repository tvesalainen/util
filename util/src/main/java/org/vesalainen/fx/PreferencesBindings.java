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

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.util.StringConverter;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PreferencesBindings
{
    private Preferences preferences;
    private Map<String,PreferenceBase<?>> properties = new HashMap<>();

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
        return Bindings.createStringBinding(()->preferences.get(key, def), getStringProperty(key, def));
    }
    public DoubleBinding createDoubleBinding(String key, double def)
    {
        return Bindings.createDoubleBinding(()->preferences.getDouble(key, def), getDoubleProperty(key, def));
    }
    public BooleanBinding createBooleanBinding(String key, boolean def)
    {
        return Bindings.createBooleanBinding(()->preferences.getBoolean(key, def), getBooleanProperty(key, def));
    }
    public FloatBinding createFloatBinding(String key, float def, Observable... dependencies)
    {
        return Bindings.createFloatBinding(()->preferences.getFloat(key, def), ArrayHelp.concat(dependencies, getFloatProperty(key, def)));
    }
    public IntegerBinding createIntegerBinding(String key, int def)
    {
        return Bindings.createIntegerBinding(()->preferences.getInt(key, def), getIntegerProperty(key, def));
    }
    public LongBinding createLongBinding(String key, long def)
    {
        return Bindings.createLongBinding(()->preferences.getLong(key, def), getLongProperty(key, def));
    }
    public <E extends Enum<E>> ObjectBinding<E> createEnumBinding(String key, E def)
    {
        Property<E> enumProperty = getEnumProperty(key, def);
        return Bindings.createObjectBinding(()->enumProperty.getValue(), enumProperty);
    }
    public <T> ObjectBinding<T> createObjectBinding(String key, T def, StringConverter<T> converter)
    {
        Property<T> property = getObjectProperty(key, def, converter);
        return Bindings.createObjectBinding(()->property.getValue(), property);
    }
    public <T> void bindBiDirectional(String key, T def, Property<T> property, StringConverter<T> converter)
    {
        Bindings.bindBidirectional(property, getObjectProperty(key, def, converter));
    }
    public void bindStringBiDirectional(String key, String def, Property<String> property)
    {
        Bindings.bindBidirectional(property, getStringProperty(key, def));
    }
    public void bindDoubleBiDirectional(String key, double def, Property<Double> property)
    {
        Bindings.bindBidirectional(property, getDoubleProperty(key, def));
    }
    public void bindBooleanBiDirectional(String key, boolean def, Property<Boolean> property)
    {
        Bindings.bindBidirectional(property, getBooleanProperty(key, def));
    }
    public void bindFloatBiDirectional(String key, float def, Property<Float> property)
    {
        Bindings.bindBidirectional(property, getFloatProperty(key, def));
    }
    public void bindIntegerBiDirectional(String key, int def, Property<Integer> property)
    {
        Bindings.bindBidirectional(property, getIntegerProperty(key, def));
    }
    public void bindLongBiDirectional(String key, long def, Property<Long> property)
    {
        Bindings.bindBidirectional(property, getLongProperty(key, def));
    }
    public <E extends Enum<E>> void bindEnumBiDirectional(String key, E def, Property<E> property)
    {
        Bindings.bindBidirectional(property, getEnumProperty(key, def));
    }
    public <T> Property<T> getProperty(String key)
    {
        return (Property<T>) properties.get(key);
    }
    private <T> Property<T> getObjectProperty(String key, T def, StringConverter<T> converter)
    {
        ObjectPreference property = (ObjectPreference) properties.get(key);
        if (property == null)
        {
            property = new ObjectPreference(preferences, key, def, converter);
            properties.put(key, property);
        }
        return property;
    }
    private Property<String> getStringProperty(String key, String def)
    {
        StringPreference property = (StringPreference) properties.get(key);
        if (property == null)
        {
            property = new StringPreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private Property<Boolean> getBooleanProperty(String key, Boolean def)
    {
        BooleanPreference property = (BooleanPreference) properties.get(key);
        if (property == null)
        {
            property = new BooleanPreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private Property<Double> getDoubleProperty(String key, Double def)
    {
        DoublePreference property = (DoublePreference) properties.get(key);
        if (property == null)
        {
            property = new DoublePreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private Property<Float> getFloatProperty(String key, Float def)
    {
        FloatPreference property = (FloatPreference) properties.get(key);
        if (property == null)
        {
            property = new FloatPreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private Property<Integer> getIntegerProperty(String key, Integer def)
    {
        IntegerPreference property = (IntegerPreference) properties.get(key);
        if (property == null)
        {
            property = new IntegerPreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private Property<Long> getLongProperty(String key, Long def)
    {
        LongPreference property = (LongPreference) properties.get(key);
        if (property == null)
        {
            property = new LongPreference(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }
    private <E extends Enum<E>> Property<E> getEnumProperty(String key, E def)
    {
        EnumPreference<E> property = (EnumPreference<E>) properties.get(key);
        if (property == null)
        {
            property = new EnumPreference<>(preferences, key, def);
            properties.put(key, property);
        }
        return property;
    }

    public void clear()
    {
        properties.values().forEach((p)->p.setValue(null));
    }
    
}
