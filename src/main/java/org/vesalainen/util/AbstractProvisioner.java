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

package org.vesalainen.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AbstractProvisioner is a feature to provision values to @Setting annotated methods. When 
 * class T is attached, it's public methods are searched to find methods annotated
 * with @Setting annotation. Method has to have one parameters and it is 
 * practical to have return type void. In attach phase each annotated method is 
 * called with a value returned from getValue method if that value is other than
 * null.
 * 
 * <p>After attach phase calls to setValue method will provision named values
 * to all attached methods.
 * 
 * @author Timo Vesalainen
 * @param <T> Type of annotated class
 * @param <K> Annotation key type
 * @param <V> Provisioned type
 */
public abstract class AbstractProvisioner<T,K,V>
{
    private MapList<K,InstanceMethod> map;
    /**
     * Return true if attached settings exists.
     * @return 
     */
    public boolean isEmpty()
    {
        return map.isEmpty();
    }
    /**
     * Attach ob
     * @param ob 
     */
    public void attach(T ob)
    {
        if (map == null)
        {
            map = new HashMapList<>();
        }
        provision(ob);
    }
    /**
     * Provision ob without attaching it.
     * @param ob 
     */
    public void provision(T ob)
    {
        for (Method method : ob.getClass().getMethods())
        {
            K key = getKey(method);
            if (key != null)
            {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1)
                {
                    throw new IllegalArgumentException("@"+key.getClass().getSimpleName()+"Setting("+key+") argument count != 1");
                }
                InstanceMethod instanceMethod = new InstanceMethod(ob, method);
                V value = getValue(key);
                if (value != null)
                {
                    instanceMethod.invoke(value);
                }
                if (map != null)
                {
                    map.add(key, instanceMethod);
                }
            }
        }
    }
    /**
     * Detach ob
     * @param ob 
     */
    public void detach(T ob)
    {
        if (map == null)
        {
            throw new IllegalStateException("attach not called");
        }
        Iterator<Map.Entry<K, List<InstanceMethod>>> ki = map.entrySet().iterator();
        while (ki.hasNext())
        {
            Map.Entry<K, List<InstanceMethod>> entry = ki.next();
            Iterator<InstanceMethod> li = entry.getValue().iterator();
            while (li.hasNext())
            {
                InstanceMethod im = li.next();
                if (im.instance.equals(ob))
                {
                    li.remove();
                }
            }
            if (entry.getValue().isEmpty())
            {
                ki.remove();
            }
        }
    }

    public abstract V getValue(K key);
    
    public abstract K getKey(Method method);
    
    public void setValue(K name, V value)
    {
        for (InstanceMethod im : map.get(name))
        {
            im.invoke(value);
        }
    }

    private class InstanceMethod
    {
        T instance;
        Method method;

        private InstanceMethod(T instance, Method method)
        {
            this.instance = instance;
            this.method = method;
        }
        
        private void invoke(Object arg)
        {
            try
            {
                method.invoke(instance, arg);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
}
