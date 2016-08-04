/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.code;

/**
 *
 * @author tkv
 */
public class BackgroundPropertySetterDispatcher implements PropertySetterDispatcher
{
    private final SimplePropertySetterDispatcher dispatcher;
    private final PropertyBuffer buffer;

    public BackgroundPropertySetterDispatcher(int capacity)
    {
        dispatcher = new SimplePropertySetterDispatcher();
        buffer = new PropertyBuffer(dispatcher, capacity);
    }

    public void start()
    {
        buffer.start();
    }

    public void stop()
    {
        buffer.stop();
    }

    @Override
    public void set(String property, boolean arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, byte arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, char arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, short arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, int arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, long arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, float arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, double arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public void set(String property, Object arg)
    {
        buffer.set(property, arg);
    }

    @Override
    public String[] getPrefixes()
    {
        return dispatcher.getPrefixes();
    }

    @Override
    public void addObserver(String key, PropertySetter ps)
    {
        dispatcher.addObserver(key, ps);
    }

    @Override
    public void removeObserver(String key, PropertySetter ps)
    {
        dispatcher.removeObserver(key, ps);
    }

    @Override
    public boolean isEmpty()
    {
        return dispatcher.isEmpty();
    }

    @Override
    public boolean containsProperty(String property)
    {
        return dispatcher.containsProperty(property);
    }
    
}
