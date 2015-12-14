/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.util.Transactional;

/**
 * PropertyDispatcher can pass setter methods properties to observers efficiently
 * without auto-boxing.
 * 
 * <p>Class is transactional, meaning that properties are transferred only when
 * commit method is called.
 * 
 * <p>If PropertySetter class implements also Transactional, its commit or 
 * rollback method is called.
 * 
 * <p>Example:
 * Create PropertyDispatcher sub class.
 * <code>
 * &#64;PropertyDispatcherClass("org.vesalainen.code.impl.PDImpl")
 * public abstract class PD extends PropertyDispatcher implements TrIntf
 * {
 * 
 *     protected PD(int[] sizes)
 *     {
 *         super(sizes);
 *     }
 * }
 * </code>
 * Create PropertySetter implementation.
 * <code>
 *    public class PS implements PropertySetter
 *    {
 *    * 
 * </code>
 * Get instance of PropertyDispatcher sublass and add observer(s)
 * <code>
 *        PS ps = new PS();
 *        PD pd = PD.getInstance(PD.class);
 *        
 *        pd.addObserver(ps, "string", "i");
 *        pd.setI(123);
 * </code>
 * @author Timo Vesalainen
 * @see org.vesalainen.util.Transactional
 */
public abstract class PropertyDispatcher extends AbstractDispatcher
{
    protected List<Transactional> transactionalObservers = new ArrayList<>();
    /**
     * Creates a PropertyDispatcher. This is called by generated sub class.
     * @param sizes Defines how many method class per type can be stored. Default
     * implementation allows interfaces all methods being called once. Size per type
     * is in the same order as in JavaType enum.
     * @see org.vesalainen.code.JavaType
     */
    protected PropertyDispatcher(int[] sizes)
    {
        super(sizes);
    }
    /**
     * Adds a PropertySetter observer for properties that have given prefix. As
     * a consequence ALL properties are added if empty prefix is used.
     * @param observer 
     * @param prefixes 
     */
    public abstract void addObserver(PropertySetter observer, String... prefixes);
    protected void addObserver(PropertySetter observer)
    {
        if (observer instanceof Transactional)
        {
            Transactional tr = (Transactional) observer;
            transactionalObservers.add(tr);
        }
    }
    /**
     * Remove a PropertySetter observer for properties that have given prefix. As
     * a consequence ALL properties are removed if empty prefix is used.
     * @param observer 
     * @param prefixes 
     */
    public abstract void removeObserver(PropertySetter observer, String... prefixes);
    protected void removeObserver(PropertySetter observer)
    {
        if (observer instanceof Transactional)
        {
            Transactional tr = (Transactional) observer;
            transactionalObservers.remove(tr);
        }
    }
    /**
     * Return true if no observers
     * @return 
     */
    public abstract boolean isEmpty();
    /**
     * Creates a instance of a class PropertyDispatcher subclass.
     * @param <T> Type of PropertyDispatcher subclass
     * @param cls PropertyDispatcher subclass class
     * @return 
     */
    public static <T extends PropertyDispatcher> T getInstance(Class<T> cls)
    {
        try
        {
            PropertyDispatcherClass annotation = cls.getAnnotation(PropertyDispatcherClass.class);
            if (annotation == null)
            {
                throw new IllegalArgumentException("@"+PropertyDispatcherClass.class.getSimpleName()+" missing in cls");
            }
            Class<?> c = Class.forName(annotation.value());
            T t =(T) c.newInstance();
            return t;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
}
